import Navbar from '../../components/layout/Navbar'
import '../../styles/Home.css'
import bgImage from '../../assets/homeScreenBack.jpg'
import astra from '../../assets/astra.png'
import shop from '../../assets/shop.png'
import game from '../../assets/game.png'
import type { ITarea } from '../../types/Interfaces';
import Tarea from '../../components/ui/Tarea';
import React from 'react'
import Logro from '../../components/ui/Logro'

export default function Home() {
  const tarea: ITarea = {
    title: "Estudiar TypeScript",
    dificultad: "HARD",
    recompensa: 50
  };

  const [notif, setNotif] = React.useState<number>(0);

  return (
    <main 
      style={{ backgroundImage: `url(${bgImage})` }} 
      className="min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white"
    >
      <Navbar />

      <section className="flex flex-col justify-center items-center gap-4 px-4 py-6 w-full max-w-7xl mx-auto">
        {/* dashboard */}
        <article className="relative home-card flex flex-col gap-6 p-6 w-full max-w-2xl">
          <header>
            <p className="home-card__eyebrow pb-2">Bienvenido de vuelta</p>
            <h1 className="font-['Press_Start_2P'] text-xl sm:text-2xl">Hi, Astraïs</h1>
            <p className="mt-1">¿Qué te queda por hacer?</p>
          </header>
          <div className="home-actions grid grid-cols-1 sm:grid-cols-2 gap-4 w-2/3">
            <button className="home-btn home-btn--primary">Crear tarea</button>
            <button className="home-btn">Unirme a un grupo</button>
            <button className="home-btn">Ver agenda</button>
            <button className="home-btn">Reclamar recompensa</button>
          </div>
          <img 
            className="absolute -right-56 -bottom-7 " 
            src={astra} 
            alt="Astraïs" 
          />
        </article>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 w-full">
          {/* Tareas Pendientes */}
          <article className="home-card p-4">
            <header className="mb-3">
              <h2 className="font-['Press_Start_2P'] text-lg">Tareas Pendientes</h2>
            </header>
            <div className="flex flex-col gap-3">
              <Tarea data={tarea} />
              <Tarea data={tarea} />
              <Tarea data={tarea} />
              <Tarea data={tarea} />
            </div>
          </article>

          {/* Tienda + Notificaciones + Logros */}
          <div className="lg:col-span-2 flex flex-col gap-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Tienda */}
              <article className="home-card p-4">
                <header className="mb-3">
                  <h2 className="font-['Press_Start_2P'] text-lg">Tienda</h2>
                </header>
                <button className="w-full">
                  <img src={shop} className="rounded-lg w-full aspect-video object-cover" alt="Tienda" />
                </button>
              </article>

              {/* Notificaciones + Logros */}
              <div className="flex flex-col gap-4">
                <article className="home-card p-4">
                  <header className="mb-3">
                    <button className="flex items-center gap-2">
                      <h2 className="font-['Press_Start_2P'] text-lg">Notificaciones</h2>
                      <span className="bg-state-error text-white rounded-full px-2 py-1 text-xs">{notif}</span>
                    </button>
                  </header>
                </article>

                <article className="home-card p-4 h-full flex flex-col gap-4">
                  <header className="mb-3">
                    <h2 className="font-['Press_Start_2P'] text-lg">Logros</h2>
                  </header>
                  <div className="flex flex-row justify-between">
                    <Logro />
                    <Logro />
                    <Logro />
                  </div>
                </article>
              </div>
            </div>

            {/* Minijuegos */}
            <article className="home-card p-4">
              <header className="mb-3">
                <h2 className="font-['Press_Start_2P'] text-lg">Minijuegos</h2>
              </header>
              <div className="flex flex-col items-center gap-4">
                <img 
                  src={game} 
                  className="w-1/2 max-w-48 h-auto rounded-lg" 
                  alt="Juego" 
                />
                <button className="home-btn w-full max-w-xs">Jugar ahora</button>
              </div>
            </article>
          </div>
        </div>
      </section>
    </main>
  )
}