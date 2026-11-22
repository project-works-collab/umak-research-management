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
