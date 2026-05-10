import type { HTMLAttributes } from 'react'

interface CompactDeviceNoticeProps extends HTMLAttributes<HTMLElement> {
  eyebrow: string
  title: string
  description: string
  as?: 'article' | 'section' | 'aside' | 'div'
}

// Aviso reutilizable para vistas que no estan optimizadas en pantallas compactas.
export default function CompactDeviceNotice({
  eyebrow,
  title,
  description,
  as = 'section',
  className = '',
  children,
  ...props
}: CompactDeviceNoticeProps) {
  // El elemento contenedor se puede cambiar sin duplicar el diseno.
  const Component = as

  return (
    <Component
      {...props}
      className={`mx-auto flex h-full w-full max-w-md items-center justify-center px-3 py-4 md:hidden ${className}`}
    >
    <article className="w-full rounded-[28px] border border-white/14 bg-[linear-gradient(165deg,color-mix(in_srgb,var(--astrais-background)_92%,var(--astrais-background-alt)_8%),color-mix(in_srgb,var(--astrais-background-alt)_76%,var(--astrais-secondary)_24%))] p-6 text-center shadow-[0_24px_60px_color-mix(in_srgb,var(--astrais-background)_46%,transparent)] backdrop-blur-sm">
        <p className="text-[0.68rem] uppercase tracking-[0.28em] text-accent-beige-300">{eyebrow}</p>
        <h1 className="mt-4 font-['Press_Start_2P'] text-[0.98rem] leading-6 text-white">{title}</h1>
        <p className="mt-4 text-sm leading-6 text-slate-300">{description}</p>
        {children ? <div className="mt-4">{children}</div> : null}
      </article>
    </Component>
  )
}
