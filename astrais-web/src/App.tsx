import './App.css'
import { Route, Routes } from 'react-router'
import Home from './pages/Dashboard/Home'
import Login from './pages/Auth/Login'


export default function App() {

  return (
    <>
      <Routes>
        <Route path="/login" element={<Login/>} />
        <Route path="/" element={<Home/>} />
      </Routes>
    </>
  )
}