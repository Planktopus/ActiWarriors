export type Role = 'ADMIN' | 'TRAINER' | 'STUDENT'

export type AuthUser = {
  id: number
  firstName: string
  lastName: string
  email: string
  role: Role
  active: boolean
}

export function apiUrl() {
  if (typeof window !== 'undefined' && window.__ENV__?.NEXT_PUBLIC_API_URL) {
    return window.__ENV__.NEXT_PUBLIC_API_URL
  }
  return process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
}

export async function api(path: string, options: RequestInit = {}) {
  const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null
  const headers = new Headers(options.headers || {})
  headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const response = await fetch(`${apiUrl()}${path}`, { ...options, headers })
  const data = await response.json().catch(() => null)
  if (!response.ok) throw new Error(data?.message || 'Er ging iets mis')
  return data
}
