import { NavLink } from 'react-router'
import logo from '../../assets/logo.svg'

const links = [
  { to: '/home', label: 'Inicio' },
  { to: '/tasks', label: 'Tareas' },
  { to: '/groups', label: 'Grupos' },
  { to: '/shop', label: 'Tienda' },
  { to: '/games', label: 'Videojuegos' },
  { to: '/achievements', label: 'Logros' },
]

export default function Navbar() {
  return (
    <nav className="flex flex-row w-full justify-between items-center px-2 bg-secondary-500/80 text-white font-['Press_Start_2P'] border-b border-secondary-500/50 mb-2" aria-label="Navegación principal">
      <div className='flex flex-row items-center'>
        <div className='items-center justify-center'>
          <img className='h-20 w-20' src={logo} />
        </div>

        <div className="">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                `home-sidebar__link ${isActive ? 'home-sidebar__link--active' : ''} font-medium`
              }
            >
              {link.label}
            </NavLink>
          ))}
        </div>
      </div>

      <NavLink
        to="/perfil"

      >
        <div className='bg-black rounded-full py-4 px-6 flex text-center'>
          <span className='text-white text-2xl'>P</span>
        </div>
      </NavLink>
    </nav>
  )
}