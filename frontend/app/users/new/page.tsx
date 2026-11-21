'use client'

import { useEffect, useState } from 'react'
import Header from '@components/header'
import { useAuth } from '@components/AuthProvider'
import { api, AuthUser } from '@services/api'

export default function NewUserPage() {
  const { user } = useAuth()
  const [users,setUsers]=useState<AuthUser[]>([])
  const [error,setError]=useState('')
  const [success,setSuccess]=useState('')

  const load=()=>api('/api/users').then(setUsers).catch(()=>setUsers([]))
  useEffect(()=>{load()},[])
  if(user?.role==='STUDENT') return <><Header/><main className="p-8 text-center">Geen toegang</main></>

  const submit=async(e:React.FormEvent<HTMLFormElement>)=>{e.preventDefault();setError('');setSuccess('');const f=new FormData(e.currentTarget);const role=user?.role==='TRAINER'?'STUDENT':f.get('role');try{await api('/api/users',{method:'POST',body:JSON.stringify({firstName:f.get('firstName'),lastName:f.get('lastName'),email:f.get('email'),password:f.get('password'),role})});setSuccess('Gebruiker aangemaakt');e.currentTarget.reset();await load()}catch(err:any){setError(err.message)}}

  return <><Header/><main className="min-h-[calc(100vh-65px)] bg-slate-50 p-4 md:p-8"><div className="mx-auto max-w-6xl"><h1 className="mb-6 text-left text-3xl font-bold text-slate-900">Gebruikersbeheer</h1>
    <div className="grid gap-6 lg:grid-cols-[360px_1fr]"><form onSubmit={submit} className="h-fit rounded-2xl border bg-white p-5 shadow-sm"><h2 className="px-0 pt-0">Nieuwe gebruiker</h2>{error&&<p className="mt-3 text-sm text-red-600">{error}</p>}{success&&<p className="mt-3 text-sm text-green-600">{success}</p>}<div className="mt-4 grid grid-cols-2 gap-3"><input name="firstName" required placeholder="Voornaam" className="rounded-lg border p-2.5"/><input name="lastName" required placeholder="Achternaam" className="rounded-lg border p-2.5"/></div><input name="email" required type="email" placeholder="E-mail" className="mt-3 w-full rounded-lg border p-2.5"/><input name="password" required type="password" placeholder="Tijdelijk wachtwoord" className="mt-3 w-full rounded-lg border p-2.5"/><select name="role" defaultValue="STUDENT" disabled={user?.role==='TRAINER'} className="mt-3 w-full rounded-lg border p-2.5"><option value="STUDENT">Student</option>{user?.role==='ADMIN'&&<><option value="TRAINER">Trainer</option><option value="ADMIN">Admin</option></>}</select><button className="mt-5 w-full rounded-lg bg-indigo-600 py-2.5 font-semibold text-white">Gebruiker aanmaken</button></form>
    <div className="overflow-hidden rounded-2xl border bg-white shadow-sm"><table className="w-full"><thead className="bg-slate-50"><tr><th>Naam</th><th>E-mail</th><th>Rol</th><th>Status</th></tr></thead><tbody>{users.map(u=><tr key={u.id}><td>{u.firstName} {u.lastName}</td><td>{u.email}</td><td><span className="rounded-full bg-slate-100 px-2 py-1 text-xs">{u.role}</span></td><td>{u.active?'Actief':'Inactief'}</td></tr>)}</tbody></table></div></div>
  </div></main></>
}
