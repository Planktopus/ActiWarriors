import type { Metadata } from 'next'
import '../styles/globals.css'

export const metadata: Metadata = {
  title: 'Penguin Project',
  description: 'Exam app',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <head>
        <script src="/env-config.js" />
      </head>
      <body>{children}</body>
    </html>
  )
}
