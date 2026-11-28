#!/bin/sh
set -eu
npm ci --ignore-scripts
# Keep the managed interpreter inside the deployed project, not a build-only cache.
python3 -m venv .render-bootstrap
.render-bootstrap/bin/python -m pip install --disable-pip-version-check uv==0.11.8
export UV_PYTHON_INSTALL_DIR="$PWD/.render-python"
export UV_PYTHON_DOWNLOADS=manual
.render-bootstrap/bin/uv python install 3.12
.render-bootstrap/bin/uv venv --python 3.12 --allow-existing .render-tfidf
.render-bootstrap/bin/uv pip install --python .render-tfidf/bin/python -r services/tfidf/requirements.txt
