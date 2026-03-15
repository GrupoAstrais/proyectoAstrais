import { NavLink } from 'react-router'

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
    <nav className="home-sidebar" aria-label="Navegación principal">
      <div className="home-sidebar__brand">
        <p>Astrais</p>
        <span>Dashboard</span>
      </div>

      <div className="home-sidebar__links">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            className={({ isActive }) =>
              `home-sidebar__link ${isActive ? 'home-sidebar__link--active' : ''}`
            }
          >
            {link.label}
          </NavLink>
        ))}
      </div>

      <NavLink
        to="/perfil"
        className={({ isActive }) =>
          `home-sidebar__profile ${isActive ? 'home-sidebar__profile--active' : ''}`
        }
      >
        <span className="home-sidebar__avatar">P</span>
        <span>Mi perfil</span>
      </NavLink>
    </nav>
  )
}