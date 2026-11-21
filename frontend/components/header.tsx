'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import { useAuth } from './AuthProvider'
import { api } from '@services/api'

const Header: React.FC = () => {
  const { user, logout } = useAuth()
  const [notifications, setNotifications] = useState<any[]>([])
  const [open, setOpen] = useState(false)

  useEffect(() => {
    if (user?.role === 'TRAINER' || user?.role === 'ADMIN') {
      api('/api/notifications').then(setNotifications).catch(() => {})
    }
  }, [user])

  const unread = notifications.filter(n => !n.read).length
  const markRead = async (id: number) => {
    await api(`/api/notifications/${id}/read`, { method: 'PATCH' })
    setNotifications(items => items.map(n => n.id === id ? { ...n, read: true } : n))
  }

  if (!user) return null
  return (
    <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/95 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3">
        <Link href="/planner" className="text-lg font-bold text-indigo-700">ActiWarriors</Link>
        <nav className="flex items-center gap-1 text-sm">
          <Link className="rounded-lg px-3 py-2 hover:bg-slate-100" href="/planner">Planner</Link>
          {(user.role === 'ADMIN' || user.role === 'TRAINER') && <Link className="rounded-lg px-3 py-2 hover:bg-slate-100" href="/users/new">Gebruikers</Link>}
          {user.role === 'ADMIN' && <Link className="rounded-lg px-3 py-2 hover:bg-slate-100" href="/admin">Admin</Link>}
          {(user.role === 'ADMIN' || user.role === 'TRAINER') && <div className="relative">
            <button onClick={() => setOpen(!open)} className="relative rounded-lg px-3 py-2 hover:bg-slate-100" aria-label="Notificaties">🔔
              {unread > 0 && <span className="absolute -right-1 -top-1 rounded-full bg-red-500 px-1.5 text-xs text-white">{unread}</span>}
            </button>
            {open && <div className="absolute right-0 mt-2 w-80 rounded-xl border bg-white p-3 shadow-xl">
              <div className="mb-2 font-semibold">Notificaties</div>
              <div className="max-h-80 space-y-2 overflow-auto">
                {notifications.length === 0 && <p className="text-sm text-slate-500">Geen meldingen</p>}
                {notifications.map(n => <button key={n.id} onClick={() => markRead(n.id)} className={`w-full rounded-lg p-2 text-left text-sm ${n.read ? 'bg-slate-50' : 'bg-indigo-50'}`}>
                  <div>{n.message}</div><div className="mt-1 text-xs text-slate-500">{new Date(n.createdAt).toLocaleString('nl-BE')}</div>
                </button>)}
              </div>
            </div>}
          </div>}
          <button onClick={logout} className="ml-2 rounded-lg bg-slate-900 px-3 py-2 text-white">Uitloggen</button>
        </nav>
      </div>
    </header>
  )
}

export default Header
