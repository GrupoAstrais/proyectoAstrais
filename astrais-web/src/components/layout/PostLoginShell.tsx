import type { ReactNode } from 'react'
import Navbar from './Navbar'

type PostLoginTone = 'home' | 'tasks' | 'groups' | 'profile' | 'shop' | 'games' | 'achievements'

interface PostLoginShellProps {
  tone: PostLoginTone
  children: ReactNode
  contentClassName?: string
}

export default function PostLoginShell({ tone, children, contentClassName = '' }: PostLoginShellProps) {
  return (
    <main data-tone={tone} className="post-login-shell font-['Space_Grotesk'] text-white">
      <Navbar />
      <section className={`post-login-shell__content ${contentClassName}`}>{children}</section>
    </main>
  )
}
