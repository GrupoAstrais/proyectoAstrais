import { Route, Routes } from 'react-router'
import Login from './pages/Auth/Login'
import Register from './pages/Auth/Register'
import Achivs from './pages/Achiv/Achivs'
import Home from './pages/Dashboard/Home'
import Games from './pages/Games/Games'
import Group from './pages/Groups/Groups'
import Shop from './pages/Shop/Shop'
import Tasks from './pages/Tasks/Tasks'
import Profile from './pages/User/Profile'
import './styles/colors.css'

function Placeholder({ title }: { title: string }) {
  return (
    <main className="grid min-h-screen place-items-center p-4">
      <h1>{title}</h1>
    </main>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<Login />} />
      <Route path="/forgot-password" element={<Placeholder title="Recuperacion de contrasena (proximamente)" />} />
      <Route path="/register" element={<Register />} />
      <Route path="/home" element={<Home />} />
      <Route path="/groups" element={<Group />} />
      <Route path="/tasks" element={<Tasks />} />
      <Route path="/profile" element={<Profile />} />
      <Route path="/shop" element={<Shop />} />
      <Route path="/games" element={<Games />} />
      <Route path="/achievements" element={<Achivs />} />
      <Route path="*" element={<Placeholder title="Pagina no encontrada" />} />
    </Routes>
  )
}
