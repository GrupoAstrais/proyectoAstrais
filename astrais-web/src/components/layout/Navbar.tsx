import { NavLink } from 'react-router'

export default function Navbar() {
  return (
    <nav className='bg-blue-200'>
        <NavLink to="/tasks" className={({isActive}) => isActive ? 'font-bold text-blue-600' : 'text-white'}>
            Tareas
        </NavLink>
        <NavLink to="/groups" className={({isActive}) => isActive ? 'font-bold text-blue-600' : 'text-white'}>
            Grupos
        </NavLink>
        <NavLink to="/shop" className={({isActive}) => isActive ? 'font-bold text-blue-600' : 'text-white'}>
            Tienda
        </NavLink>
        <NavLink to="/games" className={({isActive}) => isActive ? 'font-bold text-blue-600' : 'text-white'}>
            Juegos
        </NavLink>
        <NavLink to="/achiv" className={({isActive}) => isActive ? 'font-bold text-blue-600' : 'text-white'}>
            Logros
        </NavLink>
        <NavLink to="/perfil" className={({isActive}) => isActive ? 'font-bold text-blue-600' : 'text-white'}>
            <div>
                <p>P</p>
            </div>
        </NavLink>
    </nav>
  )
}