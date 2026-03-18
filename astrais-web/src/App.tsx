import { Route, Routes } from 'react-router'
import Home from './pages/Dashboard/Home'
import Login from './pages/Auth/Login'
import Register from './pages/Auth/Register'
import Group from './pages/Groups/Group'
import Tasks from './pages/Tasks/Tasks'
import './styles/colors.css';

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
      <Route path="/forgot-password" element={<Placeholder title="Recuperación de contraseña (próximamente)" />} />
      <Route path="/register" element={<Register />} />
      <Route path="/home" element={<Home />} />
      <Route path="/groups" element={<Group />} />
      <Route path="/tasks" element={<Tasks />} />
      <Route path="*" element={<Placeholder title="Página no encontrada" />} />
    </Routes>
  )
}