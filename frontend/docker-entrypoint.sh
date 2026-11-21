#!/bin/sh
set -e

# output: "export" bakes process.env.* into the JS bundle at `docker build` time,
# so a `docker run -e NEXT_PUBLIC_API_URL=...` has nothing left to affect at
# container startup. Regenerate the runtime config the app reads from `window`
# instead, every time the container starts, so the backend URL stays configurable
# without rebuilding the image.
cat <<EOF > /usr/share/nginx/html/env-config.js
window.__ENV__ = {
  NEXT_PUBLIC_API_URL: "${NEXT_PUBLIC_API_URL:-}",
}
EOF

exec "$@"
