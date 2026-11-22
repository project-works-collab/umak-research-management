from contextlib import contextmanager

import numpy as np
from fastapi.testclient import TestClient
from sklearn.feature_extraction.text import TfidfVectorizer

import app as service


class FakeDatabase:
    def execute(self, statement):
        assert statement == 'SELECT id,title,abstract FROM research_records ORDER BY id'
        return self

    def fetchall(self):
        return [(1, 'Shared Title', 'alpha beta study'),
                (2, 'Shared Title', 'unrelated gamma topic')]


@contextmanager
def fake_connection():
    yield FakeDatabase()


def test_scoring_matches_sklearn_and_rejects_stale_records(monkeypatch):
    monkeypatch.setenv('TFIDF_SERVICE_TOKEN', 'test-secret')
    monkeypatch.setattr(service, 'connection', fake_connection)
    monkeypatch.setattr(service, '_snapshot', None)
    client = TestClient(service.app)
    payload = {'title': 'Shared Title', 'abstract': 'alpha beta study',
               'candidates': [{'id': 1, 'title': 'Shared Title', 'abstract': 'alpha beta study'},
                              {'id': 2, 'title': 'Shared Title', 'abstract': 'unrelated gamma topic'}]}
    assert client.post('/score', json=payload).status_code == 401
    headers = {'authorization': 'Bearer test-secret'}
    response = client.post('/score', json=payload, headers=headers)
    assert response.status_code == 200
    vectorizer = TfidfVectorizer(ngram_range=(1, 2), sublinear_tf=True)
    corpus = vectorizer.fit_transform(['Shared Title alpha beta study',
                                       'Shared Title unrelated gamma topic'])
    expected = (corpus @ vectorizer.transform(['Shared Title alpha beta study']).T).toarray().ravel()
    assert np.allclose([row['score'] for row in response.json()['scores']], expected)
    payload['candidates'][0]['abstract'] = 'changed after retrieval'
    assert client.post('/score', json=payload, headers=headers).status_code == 503


def test_readiness_and_refresh_follow_archive_updates(monkeypatch):
    rows = [(1, 'Campus water sensors', 'rainwater harvesting dashboard')]

    class Archive(FakeDatabase):
        def fetchall(self):
            return list(rows)

    @contextmanager
    def archive_connection():
        yield Archive()

    monkeypatch.setenv('TFIDF_SERVICE_TOKEN', 'test-secret')
    monkeypatch.setattr(service, 'connection', archive_connection)
    monkeypatch.setattr(service, '_snapshot', None)
    client = TestClient(service.app)
    before = client.get('/ready')
    assert before.status_code == 200
    rows[0] = (1, 'Campus water sensors', 'updated rainwater storage analysis')
    payload = {'title': rows[0][1], 'abstract': rows[0][2],
               'candidates': [{'id': 1, 'title': rows[0][1], 'abstract': rows[0][2]}]}
    response = client.post('/score', json=payload, headers={'authorization': 'Bearer test-secret'})
    assert response.status_code == 200
    assert response.json()['model_version'] != before.json()['model_version']
    assert np.isclose(response.json()['scores'][0]['score'], 1)
    rows.clear()
    monkeypatch.setattr(service, '_snapshot', None)
    assert client.get('/ready').status_code == 503
