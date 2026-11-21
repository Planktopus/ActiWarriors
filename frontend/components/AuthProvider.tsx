'use client'

import { createContext, useContext, useEffect, useState } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { api, AuthUser } from '@services/api'

type AuthContextType = { user: AuthUser | null; loading: boolean; logout: () => void }
const AuthContext = createContext<AuthContextType>({ user: null, loading: true, logout: () => {} })

export function useAuth() { return useContext(AuthContext) }

export default function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [loading, setLoading] = useState(true)
  const router = useRouter()
  const pathname = usePathname()

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) {
      setLoading(false)
      if (pathname !== '/login') router.replace('/login')
      return
    }
    api('/api/auth/me').then(setUser).catch(() => {
      localStorage.removeItem('token')
      router.replace('/login')
    }).finally(() => setLoading(false))
  }, [pathname, router])

  useEffect(() => {
    if (!loading && user) {
      if (pathname.startsWith('/admin') && user.role !== 'ADMIN') router.replace('/planner')
      if (pathname.startsWith('/users') && user.role === 'STUDENT') router.replace('/planner')
    }
  }, [loading, user, pathname, router])

  const logout = () => { localStorage.removeItem('token'); setUser(null); router.replace('/login') }
  if (loading) return <div className="min-h-screen grid place-items-center text-slate-500">Laden…</div>
  return <AuthContext.Provider value={{ user, loading, logout }}>{children}</AuthContext.Provider>
}
