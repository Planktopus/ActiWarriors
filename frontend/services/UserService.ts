// Read from the runtime config injected by docker-entrypoint.sh (Docker), falling
// back to the build-time env var for local dev via `next dev` / `.env.local`.
// See public/env-config.js for why this can't just be process.env.NEXT_PUBLIC_API_URL.
const getApiUrl = () => {
  if (typeof window !== 'undefined' && window.__ENV__?.NEXT_PUBLIC_API_URL) {
    return window.__ENV__.NEXT_PUBLIC_API_URL
  }
  return process.env.NEXT_PUBLIC_API_URL ?? ''
}

const getAll = () => {
  return fetch(getApiUrl() + '/players', {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  })
}

const UserService = {
  getAll,
}

export default UserService
