import hashlib
import hmac
import os
import threading
import time
from contextlib import contextmanager

import psycopg
from fastapi import FastAPI, Header, HTTPException
from pydantic import BaseModel, Field, ConfigDict
from sklearn.feature_extraction.text import TfidfVectorizer

app = FastAPI(docs_url=None, redoc_url=None, openapi_url=None)
_lock = threading.RLock()
_snapshot = None

class Candidate(BaseModel):
    id: int
    title: str
    abstract: str = ''

class ScoreRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")
    title: str = Field(min_length=1, max_length=200)
    abstract: str = Field(min_length=1, max_length=4000)
    candidates: list[Candidate] = Field(max_length=50)

class SearchRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")
    title: str = Field(min_length=1, max_length=200)
    abstract: str = Field(min_length=1, max_length=4000)
    allowed_ids: list[int] = Field(max_length=100000)

@contextmanager
def connection():
    with psycopg.connect(os.environ['TFIDF_DATABASE_URL'], connect_timeout=5) as db:
        yield db

def fingerprint(rows):
    digest = hashlib.sha256()
    for record_id, title, abstract in rows:
        for field in (str(record_id), title or '', abstract or ''):
            data = field.encode('utf-8')
            digest.update(len(data).to_bytes(4, 'big'))
            digest.update(data)
    return digest.hexdigest()

def refresh(force=False):
    global _snapshot
    with _lock:
        if not force and _snapshot and time.monotonic() - _snapshot['checked'] < 60:
            return _snapshot
        with connection() as db:
            rows = db.execute('SELECT id,title,abstract FROM research_records ORDER BY id').fetchall()
        version = fingerprint(rows)
        if _snapshot and version == _snapshot['version']:
            _snapshot['checked'] = time.monotonic()
            return _snapshot
        if not rows:
            _snapshot = dict(version=version, checked=time.monotonic(), vectorizer=None, matrix=None, records={})
            return _snapshot
        vectorizer = TfidfVectorizer(ngram_range=(1, 2), sublinear_tf=True)
        try:
            matrix = vectorizer.fit_transform([f'{title or ""} {abstract or ""}' for _, title, abstract in rows])
        except ValueError as error:
            raise HTTPException(503, 'Archive has no usable vocabulary') from error
        _snapshot = dict(version=version, checked=time.monotonic(), vectorizer=vectorizer,
                         matrix=matrix, records={record_id: (index, title or '', abstract or '')
                                                 for index, (record_id, title, abstract) in enumerate(rows)})
        return _snapshot

@app.get('/health')
def health():
    return {'status': 'ok'}

@app.get('/ready')
def ready():
    try:
        snapshot = refresh()
        if snapshot['vectorizer'] is None:
            raise HTTPException(503, 'Archive is empty')
        return {'status': 'ready', 'model_version': snapshot['version']}
    except psycopg.Error as error:
        raise HTTPException(503, 'Archive unavailable') from error

@app.post('/score')
def score(request: ScoreRequest, authorization: str | None = Header(default=None)):
    expected = 'Bearer ' + os.environ['TFIDF_SERVICE_TOKEN']
    if not authorization or not hmac.compare_digest(authorization, expected):
        raise HTTPException(401, 'Unauthorized')
    try:
        snapshot = refresh()
        if any((candidate.id not in snapshot['records'] or
                snapshot['records'][candidate.id][1:] != (candidate.title, candidate.abstract))
               for candidate in request.candidates):
            snapshot = refresh(force=True)
        if any((candidate.id not in snapshot['records'] or
                snapshot['records'][candidate.id][1:] != (candidate.title, candidate.abstract))
               for candidate in request.candidates):
            raise HTTPException(503, 'Archive changed during scoring')
        if not request.candidates:
            return {'model_version': snapshot['version'], 'scores': []}
        if snapshot['vectorizer'] is None:
            raise HTTPException(503, 'Archive is empty')
        query = snapshot['vectorizer'].transform([request.title + ' ' + request.abstract])
        indices = [snapshot['records'][candidate.id][0] for candidate in request.candidates]
        values = (snapshot['matrix'][indices] @ query.T).toarray().ravel()
        return {'model_version': snapshot['version'], 'scores': [
            {'id': candidate.id, 'score': float(value)}
            for candidate, value in zip(request.candidates, values)]}
    except psycopg.Error as error:
        raise HTTPException(503, 'Archive unavailable') from error


@app.post('/search')
def search(request: SearchRequest, authorization: str | None = Header(default=None)):
    """Exact cosine search over the caller's entire authorized archive scope.

    The Node service derives IDs using its authenticated RLS context. Only IDs
    and scores cross this private boundary; Node rechecks access before returning
    current metadata. There is no title-overlap predicate in this path.
    """
    expected = 'Bearer ' + os.environ['TFIDF_SERVICE_TOKEN']
    if not authorization or not hmac.compare_digest(authorization, expected):
        raise HTTPException(401, 'Unauthorized')
    ids = sorted(set(request.allowed_ids))
    if any(record_id < 1 for record_id in ids):
        raise HTTPException(422, 'Invalid archive scope')
    try:
        snapshot = refresh(force=True)
        if any(record_id not in snapshot['records'] for record_id in ids):
            snapshot = refresh(force=True)
        if any(record_id not in snapshot['records'] for record_id in ids):
            raise HTTPException(503, 'Archive changed during retrieval')
        if not ids:
            return {'model_version': snapshot['version'], 'scores': [], 'scored_count': 0}
        if snapshot['vectorizer'] is None:
            raise HTTPException(503, 'Archive is empty')
        query = snapshot['vectorizer'].transform([request.title + ' ' + request.abstract])
        indices = [snapshot['records'][record_id][0] for record_id in ids]
        values = (snapshot['matrix'][indices] @ query.T).toarray().ravel()
        ranked = sorted(zip(ids, values), key=lambda item: (-float(item[1]), item[0]))[:5]
        return {'model_version': snapshot['version'], 'scored_count': len(ids),
                'scores': [{'id': record_id, 'score': min(1.0, max(0.0, float(value)))}
                           for record_id, value in ranked]}
    except psycopg.Error as error:
        raise HTTPException(503, 'Archive unavailable') from error
