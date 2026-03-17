import './App.css'
import './index.css'
import { Route, Routes } from 'react-router'
import Home from './pages/Dashboard/Home'
import Login from './pages/Auth/Login'
import Register from './pages/Auth/Register'
import Group from './pages/Groups/Group'
import Tasks from './pages/Tasks/Tasks'

function Placeholder({ title }: { title: string }) {
  return (
    <main style={{ minHeight: '100vh', display: 'grid', placeItems: 'center', padding: '1rem' }}>
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
      <Route path="/register" element={<Register/>} />
      <Route path="/home" element={<Home />} />
      <Route path="/groups" element={<Group />} />
      <Route path="/tasks" element={<Tasks />} />
      <Route path="*" element={<Placeholder title="Página no encontrada" />} />
    </Routes>
  )
}