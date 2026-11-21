// Default runtime config for local/dev use.
// In the Docker image, docker-entrypoint.sh regenerates this file from the
// NEXT_PUBLIC_API_URL environment variable each time the container starts,
// since output: "export" bakes process.env.* into the JS bundle at `docker build`
// time and can no longer react to `docker run -e ...`.
window.__ENV__ = {
  NEXT_PUBLIC_API_URL: '',
}
