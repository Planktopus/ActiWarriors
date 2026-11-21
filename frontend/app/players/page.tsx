'use client'

import useSWR from 'swr'
import Header from '@components/header'
import UserOverview from '@components/users/UserOverview'
import UserService from '@services/UserService'

const fetcher = () => UserService.getAll().then((response) => response.json())

export default function PlayersPage() {
  const { data: users } = useSWR('players', fetcher)

  return (
    <>
      <Header />
      <main className="p-6 min-h-screen flex flex-col items-center">
        <h1>Players</h1>

        <section className="mt-5">
          {users && <UserOverview users={users} />}
        </section>
      </main>
    </>
  )
}
