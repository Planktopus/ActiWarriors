'use client'

import { useEffect, useState } from 'react'
import Header from '@components/header'
import { useAuth } from '@components/AuthProvider'
import { api } from '@services/api'

export default function AdminPage() {
  const { user }=useAuth()
  const today=new Date()
  const [month,setMonth]=useState(today.getMonth()+1)
  const [year,setYear]=useState(today.getFullYear())
  const [rows,setRows]=useState<any[]>([])
  const load=()=>api(`/api/admin/stats?month=${month}&year=${year}`).then(setRows).catch(()=>setRows([]))
  useEffect(()=>{if(user?.role==='ADMIN')load()},[month,year,user])
  if(user?.role!=='ADMIN') return <><Header/><main className="p-8 text-center">Alleen toegankelijk voor administrators</main></>

  return <><Header/><main className="min-h-[calc(100vh-65px)] bg-slate-50 p-2 sm:p-4 md:p-8"><div className="mx-auto max-w-6xl"><h1 className="mb-4 text-left text-2xl font-bold text-slate-900 sm:mb-6 sm:text-3xl">Admin dashboard</h1>
    <div className="mb-6 flex flex-wrap items-end gap-3 rounded-2xl border bg-white p-3 shadow-sm sm:p-4"><label className="text-sm">Maand<select value={month} onChange={e=>setMonth(Number(e.target.value))} className="ml-2 rounded-lg border px-3 py-2">{Array.from({length:12},(_,i)=><option key={i+1} value={i+1}>{new Date(2020,i,1).toLocaleDateString('nl-BE',{month:'long'})}</option>)}</select></label><label className="text-sm">Jaar<input type="number" value={year} onChange={e=>setYear(Number(e.target.value))} className="ml-2 w-24 rounded-lg border px-3 py-2" /></label></div>
    <div className="overflow-x-auto rounded-2xl border bg-white shadow-sm"><table className="w-full min-w-[620px]"><thead className="bg-slate-50"><tr><th>Student</th><th>Inschrijvingen</th><th>Effectief aanwezig</th><th>Opkomst</th></tr></thead><tbody>{rows.map(r=><tr key={r.studentId}><td className="font-medium">{r.name}</td><td>{r.enrolled}</td><td>{r.present}</td><td><div className="flex items-center gap-3"><div className="h-2 w-32 overflow-hidden rounded-full bg-slate-200"><div className="h-full rounded-full bg-indigo-500" style={{width:`${r.percentage}%`}} /></div><b>{r.percentage}%</b></div></td></tr>)}</tbody></table>{rows.length===0&&<p className="p-8 text-center text-slate-500">Geen studentstatistieken voor deze periode</p>}</div>
  </div></main></>
}
