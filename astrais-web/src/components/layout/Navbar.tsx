import { NavLink } from 'react-router'

const navItems = [
  { to: '/tasks', label: 'Tareas' },
  { to: '/groups', label: 'Grupos' },
  { to: '/shop', label: 'Tienda' },
  { to: '/games', label: 'Juegos' },
  { to: '/achievements', label: 'Logros' },
  { to: '/profile', label: 'Perfil' },
]

const baseLinkStyles =
  'rounded-lg px-4 py-3 text-sm font-medium transition-colors duration-200'

export default function Navbar() {
  return (
    <nav className="fixed left-0 top-0 flex h-screen w-60 flex-col gap-3 bg-linear-to-b from-indigo-950 via-indigo-900 to-blue-900 p-4 shadow-2xl">
      <h2 className="mb-2 px-4 text-xs font-semibold uppercase tracking-[0.2em] text-indigo-200">
        Astrais
      </h2>

      {navItems.map(({ to, label }) => (
        <NavLink
          key={to}
          to={to}
          className={({ isActive }) =>
            `${baseLinkStyles} ${
              isActive
                ? 'bg-indigo-400/30 text-blue-100 ring-1 ring-indigo-200/70'
                : 'text-indigo-100/90 hover:bg-indigo-300/20 hover:text-white'
            }`
          }
        >
          {label}
        </NavLink>
      ))}
    </nav>
  )
}
