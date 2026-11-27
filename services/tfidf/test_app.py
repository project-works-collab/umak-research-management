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


def test_global_retrieval_finds_different_titles_and_enforces_scope(monkeypatch):
    rows = [(1, 'Automated vision attendance', 'facial recognition campus attendance camera'),
            (2, 'Smart roll call engine', 'facial recognition campus attendance camera'),
            (3, 'Soil chemistry', 'agriculture nitrogen fertilizer')]

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
    payload = {'title': 'Smart roll call engine',
               'abstract': 'facial recognition campus attendance camera', 'allowed_ids': [1, 3]}
    assert client.post('/search', json=payload).status_code == 401
    response = client.post('/search', json=payload, headers={'authorization': 'Bearer test-secret'})
    assert response.status_code == 200
    body = response.json()
    assert body['scored_count'] == 2
    assert [item['id'] for item in body['scores']] == [1, 3]
    assert body['scores'][0]['score'] > body['scores'][1]['score']
    assert 2 not in [item['id'] for item in body['scores']]
    payload['allowed_ids'] = [999]
    assert client.post('/search', json=payload, headers={'authorization': 'Bearer test-secret'}).status_code == 503
