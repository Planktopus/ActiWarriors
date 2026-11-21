'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { api } from '@services/api'

export default function LoginPage() {
  const router = useRouter()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  useEffect(() => { if (localStorage.getItem('token')) router.replace('/planner') }, [router])

  const submit = async (event: React.FormEvent) => {
    event.preventDefault(); setError('')
    try {
      const result = await api('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })
      localStorage.setItem('token', result.token)
      router.replace('/planner')
    } catch (err: any) { setError(err.message) }
  }

  return <main className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-sky-50 grid place-items-center px-4">
    <form onSubmit={submit} className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-8 shadow-xl">
      <div className="mb-8 text-center"><div className="mx-auto mb-3 grid h-12 w-12 place-items-center rounded-xl bg-indigo-600 text-xl text-white">A</div>
        <h1 className="text-2xl font-bold text-slate-900">ActiWarriors</h1><p className="mt-1 text-sm text-slate-500">Log in op je trainingsaccount</p></div>
      {error && <div className="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</div>}
      <label className="block text-sm font-medium text-slate-700">E-mail</label>
      <input required type="email" value={email} onChange={e => setEmail(e.target.value)} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2.5" placeholder="naam@voorbeeld.be" />
      <label className="mt-4 block text-sm font-medium text-slate-700">Wachtwoord</label>
      <input required type="password" value={password} onChange={e => setPassword(e.target.value)} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2.5" />
      <button className="mt-6 w-full rounded-lg bg-indigo-600 px-4 py-3 font-semibold text-white hover:bg-indigo-700">Inloggen</button>
    </form>
  </main>
}
