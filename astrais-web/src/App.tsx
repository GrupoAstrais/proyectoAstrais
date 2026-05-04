import { useEffect } from 'react'
import { Navigate, Outlet, Route, Routes, useLocation } from 'react-router'
import Login from './pages/Auth/Login'
import Register from './pages/Auth/Register'
import Achivs from './pages/Achiv/Achivs'
import Home from './pages/Dashboard/Home'
import GameEmbed from './pages/Games/GameEmbed'
import Games from './pages/Games/Games'
import Group from './pages/Groups/Groups'
import Shop from './pages/Shop/Shop'
import Tasks from './pages/Tasks/Tasks'
import Profile from './pages/User/Profile'
import { getUserData } from './data/Api'
import './styles/colors.css'
import { applyThemeColors } from './styles/theme'

function Placeholder({ title }: { title: string }) {
  return (
    <main className="grid min-h-screen place-items-center p-4">
      <h1>{title}</h1>
    </main>
  )
}

function RequireAuth() {
  if (typeof window === 'undefined') {
    return <Navigate to="/login" replace />
  }

  const token = window.localStorage.getItem('jwtToken')
  return token ? <Outlet /> : <Navigate to="/login" replace />
}

export default function App() {
  const location = useLocation()

  useEffect(() => {
    if (['/', '/login', '/register', '/forgot-password'].includes(location.pathname)) {
      applyThemeColors()
      return
    }

    if (typeof window === 'undefined' || !window.localStorage.getItem('jwtToken')) {
      applyThemeColors()
      return
    }

    void getUserData().catch(() => undefined)
  }, [location.pathname])

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<Login />} />
      <Route path="/forgot-password" element={<Placeholder title="Recuperacion de contrasena (proximamente)" />} />
      <Route path="/register" element={<Register />} />
      <Route element={<RequireAuth />}>
        <Route path="/home" element={<Home />} />
        <Route path="/groups" element={<Group />} />
        <Route path="/tasks" element={<Tasks />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/shop" element={<Shop />} />
        <Route path="/games/embed/:gameId" element={<GameEmbed />} />
        <Route path="/games" element={<Games />} />
        <Route path="/achievements" element={<Achivs />} />
      </Route>
      <Route path="*" element={<Placeholder title="Pagina no encontrada" />} />
    </Routes>
  )
}
