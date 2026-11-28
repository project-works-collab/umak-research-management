#!/bin/sh
set -eu
npm ci --ignore-scripts
# Keep the managed interpreter inside the deployed project, not a build-only cache.
python3 -m venv .render-bootstrap
.render-bootstrap/bin/python -m pip install --disable-pip-version-check uv==0.11.8
export UV_PYTHON_INSTALL_DIR="$PWD/.render-python"
export UV_PYTHON_DOWNLOADS=manual
export UV_SYSTEM_PYTHON=false
.render-bootstrap/bin/uv python install 3.12
hosted_demo_python=''
for interpreter in "$UV_PYTHON_INSTALL_DIR"/cpython-3.12.*/bin/python3.12; do
    if [ -x "$interpreter" ]; then hosted_demo_python="$interpreter"; break; fi
done
[ -n "$hosted_demo_python" ] || { echo 'Managed Python 3.12 interpreter missing' >&2; exit 1; }
"$hosted_demo_python" -m venv .render-tfidf
.render-bootstrap/bin/uv pip install --python .render-tfidf/bin/python -r services/tfidf/requirements.txt
