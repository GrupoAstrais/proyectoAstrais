import { NavLink } from 'react-router'
import logo from '../../assets/logo.svg'

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
    <nav className="mb-2 flex w-full flex-row items-center justify-between border-b border-secondary-500/50 bg-secondary-500/80 px-2 text-white font-['Press_Start_2P']" aria-label="Navegación principal">
      <div className="flex flex-row items-center">
        <div className="items-center justify-center">
          <img className="h-20 w-20" src={logo} alt="Astrais logo" />
        </div>

        <div>
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                `inline-block rounded-[0.8rem] px-[0.8rem] py-[0.72rem] text-[#e8eaff] no-underline transition duration-200 hover:translate-x-0.5 hover:bg-white/10 ${
                  isActive
                    ? 'border border-white/25 bg-[linear-gradient(90deg,rgba(167,139,250,0.45),rgba(96,165,250,0.35))]'
                    : ''
                }`
              }
            >
              {link.label}
            </NavLink>
          ))}
        </div>
      </div>

      <NavLink to="/profile">
        <div className="flex items-center justify-center rounded-full bg-black w-12 h-12">
          <span className="text-2xl text-white leading-none">P</span>
        </div>
      </NavLink>
    </nav>
  )
}