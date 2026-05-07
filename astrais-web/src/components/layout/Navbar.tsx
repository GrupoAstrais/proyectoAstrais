import { NavLink } from 'react-router'
import logo from '../../assets/logo_w.svg'

const links = [
  { to: '/home', label: 'Inicio' },
  { to: '/tasks', label: 'Tareas' },
  { to: '/groups', label: 'Grupos' },
  { to: '/shop', label: 'Tienda' },
  { to: '/games', label: 'Minijuegos' },
  { to: '/achievements', label: 'Logros' },
]

export default function Navbar() {
  return (
    <nav
      className="relative z-20 mb-1 flex w-full items-center justify-between border-b border-primary-500/40 bg-primary-500/35 px-3 py-2 text-white font-['Press_Start_2P'] backdrop-blur-sm min-[1400px]:px-4 min-[1400px]:py-2.5"
      aria-label="Navegacion principal"
    >
      <div className="flex min-w-0 flex-1 items-center gap-3 min-[1400px]:gap-4">
        <div className="flex shrink-0 items-center justify-center">
          <img className="h-14 w-14 min-[1400px]:h-16 min-[1400px]:w-16" src={logo} alt="Astrais logo" />
        </div>

        <div className="relative z-20 grid min-w-0 flex-1 grid-cols-6 gap-1.5 min-[1400px]:gap-2">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                `inline-flex min-h-10 items-center justify-center rounded-[0.9rem] px-2 py-2 text-left text-[0.5rem] uppercase tracking-widest text-(--astrais-tab-text) no-underline transition duration-200 hover:translate-y-px hover:bg-(--astrais-tab-hover) min-[1400px]:min-h-11 min-[1400px]:px-3 min-[1400px]:py-2.5 min-[1400px]:text-[0.8rem] ${
                  isActive
                    ? 'border border-(--astrais-tab-active-border) [background:var(--astrais-tab-active-bg)] shadow-[0_10px_24px_color-mix(in_srgb,var(--astrais-background)_36%,transparent)] transition duration-200 text-[1rem] min-[1400px]:text-[0.9rem]'
                    : 'border border-transparent'
                }`
              }
            >
              {link.label}
            </NavLink>
          ))}
        </div>
      </div>

      <NavLink to="/profile" className="ml-3 shrink-0 min-[1400px]:ml-4">
        <div className="flex h-10 w-10 items-center justify-center rounded-full border border-white/15 bg-black/55 min-[1400px]:h-11 min-[1400px]:w-11">
          <p className="pl-1 text-lg text-white min-[1400px]:text-xl">P</p>
        </div>
      </NavLink>
    </nav>
  )
}
