'use client'

import { useEffect, useMemo, useState } from 'react'
import Header from '@components/header'
import { useAuth } from '@components/AuthProvider'
import { api, AuthUser } from '@services/api'

type Session = { id:number; title:string; date:string; startTime:string; endTime:string; trainerId:number; trainerName:string; maxCapacity:number; subscribed?:boolean; enrollmentCount?:number }

function toLocalDateKey(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export default function PlannerPage() {
  const { user } = useAuth()
  const [sessions,setSessions] = useState<Session[]>([])
  const [mode,setMode] = useState<'week'|'month'>('month')
  const [anchor,setAnchor] = useState(new Date())
  const [selected,setSelected] = useState<Session|null>(null)
  const [showAdd,setShowAdd] = useState(false)
  const [trainers,setTrainers] = useState<AuthUser[]>([])
  const [enrollments,setEnrollments] = useState<any[]>([])

  const load = async () => {
    let start: Date
    let end: Date

    if (mode === 'week') {
      start = new Date(anchor)
      start.setDate(anchor.getDate() - ((anchor.getDay() + 6) % 7))
      end = new Date(start)
      end.setDate(start.getDate() + 6)
    } else {
      const first = new Date(anchor.getFullYear(), anchor.getMonth(), 1)
      const mondayOffset = (first.getDay() + 6) % 7
      start = new Date(first)
      start.setDate(first.getDate() - mondayOffset)
      end = new Date(start)
      end.setDate(start.getDate() + 41)
    }

    const data = await api(`/api/training-sessions?from=${toLocalDateKey(start)}&to=${toLocalDateKey(end)}`)
    setSessions(data)
  }
  useEffect(() => { load().catch(() => {}) }, [anchor, mode])
  useEffect(() => { if (user?.role === 'ADMIN') api('/api/users').then((u:any[]) => setTrainers(u.filter(x=>x.role==='TRAINER'))).catch(()=>{}) }, [user])

  useEffect(() => { if (selected) api(`/api/training-sessions/${selected.id}/enrollments`).then(setEnrollments).catch(()=>setEnrollments([])) }, [selected])

  const year = anchor.getFullYear(), month = anchor.getMonth()
  const days = useMemo(() => {
    if (mode==='month') {
      const first = new Date(year,month,1)
      const mondayOffset = (first.getDay() + 6) % 7
      const start = new Date(year,month,1-mondayOffset)
      return Array.from({length:42},(_,i)=>new Date(start.getFullYear(),start.getMonth(),start.getDate()+i))
    }
    const start = new Date(anchor); start.setDate(anchor.getDate()-((anchor.getDay()+6)%7))
    return Array.from({length:7},(_,i)=>new Date(start.getFullYear(),start.getMonth(),start.getDate()+i))
  }, [anchor,mode,year,month])

  const actionEnroll = async (session: Session) => {
    if (!session.subscribed && confirm(`Inschrijven voor ${session.title}?`)) await api(`/api/training-sessions/${session.id}/enroll`,{method:'POST'})
    if (session.subscribed && confirm(`Uitschrijven voor ${session.title}?`)) await api(`/api/training-sessions/${session.id}/enroll`,{method:'DELETE'})
    await load()
  }

  const attendance = async (enrollmentId:number,status:string) => {
    if(!selected)return
    await api(`/api/training-sessions/${selected.id}/enrollments/${enrollmentId}/attendance`,{method:'PATCH',body:JSON.stringify({status})})
    setEnrollments(await api(`/api/training-sessions/${selected.id}/enrollments`))
  }

  const deleteSession = async () => { if(selected && confirm('Deze training verwijderen?')) { await api(`/api/training-sessions/${selected.id}`,{method:'DELETE'}); setSelected(null); await load() } }

  return <><Header /><main className="min-h-[calc(100vh-65px)] bg-slate-50 p-4 md:p-8">
    <div className="mx-auto max-w-7xl">
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div><h1 className="text-left text-3xl font-bold text-slate-900">Trainingplanner</h1><p className="mt-1 text-sm text-slate-500">Plan, schrijf in en beheer trainingen</p></div>
        <div className="flex items-center gap-2">
          <button onClick={()=>setAnchor(current => mode==='week' ? new Date(current.getFullYear(),current.getMonth(),current.getDate()-7) : new Date(current.getFullYear(),current.getMonth()-1,current.getDate()))} className="rounded-lg border bg-white px-3 py-2">‹</button>
          <button onClick={()=>setAnchor(new Date())} className="rounded-lg border bg-white px-3 py-2 text-sm">Vandaag</button>
          <button onClick={()=>setAnchor(current => mode==='week' ? new Date(current.getFullYear(),current.getMonth(),current.getDate()+7) : new Date(current.getFullYear(),current.getMonth()+1,current.getDate()))} className="rounded-lg border bg-white px-3 py-2">›</button>
          <div className="ml-2 rounded-lg bg-slate-200 p-1"><button onClick={()=>setMode('week')} className={`rounded-md px-3 py-1.5 text-sm ${mode==='week'?'bg-white shadow':''}`}>Week</button><button onClick={()=>setMode('month')} className={`rounded-md px-3 py-1.5 text-sm ${mode==='month'?'bg-white shadow':''}`}>Maand</button></div>
          {(user?.role==='ADMIN'||user?.role==='TRAINER')&&<button onClick={()=>setShowAdd(true)} className="ml-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white">+ Training</button>}
        </div>
      </div>
      <div className="rounded-2xl border border-slate-200 bg-white p-3 shadow-sm">
        <div className="mb-3 text-center font-semibold text-slate-700">{anchor.toLocaleDateString('nl-BE',{month:'long',year:'numeric'})}</div>
        {mode==='month'&&<div className="grid grid-cols-7 text-center text-xs font-semibold uppercase text-slate-400">{['Ma','Di','Wo','Do','Vr','Za','Zo'].map(d=><div key={d} className="py-2">{d}</div>)}</div>}
        <div className={`grid ${mode==='week'?'grid-cols-7':'grid-cols-7'} gap-px overflow-hidden rounded-xl bg-slate-200`}>
          {days.map(day=>{ const key=toLocalDateKey(day); const list=sessions.filter(s=>s.date===key)
            return <div key={key} className={`min-h-28 bg-white p-2 ${day.getMonth()!==month?'text-slate-300':''}`}>
              <div className="mb-1 text-sm font-medium">{day.getDate()}</div>
              <div className="space-y-1">{list.map(s=>{
                const full=(s.enrollmentCount||0)>=s.maxCapacity
                const cls=user?.role==='STUDENT'?(s.subscribed?'border-green-500 bg-green-100 text-green-900':full?'border-slate-300 bg-slate-200 text-slate-500':'border-blue-500 bg-blue-100 text-blue-900'):'border-indigo-300 bg-indigo-100 text-indigo-900'
                return <button key={s.id} onClick={()=>user?.role==='STUDENT'?actionEnroll(s):setSelected(s)} className={`block w-full rounded-md border-l-4 p-1.5 text-left text-xs ${cls}`}>
                  <b>{s.startTime}</b> {s.title}<div className="mt-0.5 opacity-70">{s.enrollmentCount||0}/{s.maxCapacity}</div>
                </button>
              })}</div>
            </div>
          })}
        </div>
      </div>
    </div>
    {showAdd&&<TrainingModal trainers={trainers} onClose={()=>setShowAdd(false)} onSaved={()=>{setShowAdd(false);load()}} />}
    {selected&&<div className="fixed inset-0 z-40 grid place-items-center bg-slate-900/40 p-4" onMouseDown={()=>setSelected(null)}><div onMouseDown={e=>e.stopPropagation()} className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl">
      <div className="flex items-start justify-between"><div><h2 className="px-0 pt-0 text-xl">{selected.title}</h2><p className="mt-1 text-sm text-slate-500">{selected.date} • {selected.startTime}–{selected.endTime} • {selected.trainerName}</p></div><button onClick={()=>setSelected(null)} className="text-2xl text-slate-400">×</button></div>
      {(user?.role==='ADMIN'||user?.role==='TRAINER')&&<><h3 className="px-0">Aanwezigheid</h3><div className="mt-3 space-y-2">{enrollments.map(e=><div key={e.id} className="flex items-center justify-between rounded-lg border p-3"><span>{e.name}</span><select value={e.status} onChange={ev=>attendance(e.id,ev.target.value)} className="rounded-lg border px-2 py-1 text-sm"><option>INGESCHREVEN</option><option>AANWEZIG</option><option>AFWEZIG</option></select></div>)}</div>
      <div className="mt-5 flex gap-2"><button onClick={deleteSession} className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">Verwijderen</button></div></>}
    </div></div>}
  </main></>
}

function TrainingModal({trainers,onClose,onSaved}:{trainers:AuthUser[];onClose:()=>void;onSaved:()=>void}) {
  const { user }=useAuth(); const [error,setError]=useState('');
  const submit=async(e:React.FormEvent<HTMLFormElement>)=>{e.preventDefault();const f=new FormData(e.currentTarget);const data={title:f.get('title'),date:f.get('date'),startTime:f.get('startTime'),endTime:f.get('endTime'),maxCapacity:Number(f.get('maxCapacity')),trainerId:f.get('trainerId')?Number(f.get('trainerId')):undefined};try{await api('/api/training-sessions',{method:'POST',body:JSON.stringify(data)});onSaved()}catch(err:any){setError(err.message)}}
  return <div className="fixed inset-0 z-50 grid place-items-center bg-slate-900/40 p-4"><form onSubmit={submit} className="w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl"><div className="mb-5 flex justify-between"><h2 className="px-0 pt-0">Nieuwe training</h2><button type="button" onClick={onClose}>×</button></div>{error&&<div className="mb-3 text-sm text-red-600">{error}</div>}<input name="title" required placeholder="Titel / type training" className="w-full rounded-lg border p-2.5"/><div className="mt-3 grid grid-cols-2 gap-3"><input name="date" required type="date" className="rounded-lg border p-2.5"/><input name="maxCapacity" type="number" min="1" defaultValue="6" className="rounded-lg border p-2.5"/></div><div className="mt-3 grid grid-cols-2 gap-3"><input name="startTime" required type="time" className="rounded-lg border p-2.5"/><input name="endTime" required type="time" className="rounded-lg border p-2.5"/></div>{user?.role==='ADMIN'&&<select name="trainerId" required className="mt-3 w-full rounded-lg border p-2.5"><option value="">Kies trainer</option>{trainers.map(t=><option key={t.id} value={t.id}>{t.firstName} {t.lastName}</option>)}</select>}<div className="mt-5 flex justify-end gap-2"><button type="button" onClick={onClose} className="rounded-lg border px-4 py-2">Annuleren</button><button className="rounded-lg bg-indigo-600 px-4 py-2 text-white">Opslaan</button></div></form></div>
}
