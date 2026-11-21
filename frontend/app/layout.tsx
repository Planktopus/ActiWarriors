import type { Metadata } from 'next'
import '../styles/globals.css'
import AuthProvider from '@components/AuthProvider'

export const metadata: Metadata = {
  title: 'ActiWarriors Training Planner',
  description: 'Training planning and attendance management',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="nl">
      <head>
        <script src="/env-config.js" />
      </head>
      <body>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  )
}
