import type { ButtonHTMLAttributes } from 'react'

interface FilterPillProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  active?: boolean
}

// Pildora reutilizable para filtros con estado activo.
export default function FilterPill({ active = false, className = '', children, ...props }: FilterPillProps) {
  return (
    <button
      {...props}
      className={`rounded-full border px-4 py-2 text-[0.74rem] font-semibold transition ${
        active
          ? 'border-transparent bg-linear-to-r from-(--astrais-primary) via-(--astrais-secondary) to-(--astrais-tertiary) text-white shadow-[0_10px_24px_color-mix(in_srgb,var(--astrais-background)_32%,transparent)]'
          : 'border-white/14 bg-white/6 text-slate-200 hover:bg-white/10'
      } ${className}`}
    >
      {children}
    </button>
  )
}
