import Navbar from '../../components/layout/Navbar'
import bgImage from '../../assets/homeScreenBack.jpg'
import astra from '../../assets/astra.png'
import shop from '../../assets/shop.png'
import game from '../../assets/game.png'
import type { ITarea } from '../../types/Interfaces';
import Task from '../../components/ui/Task';
import React, { useState } from 'react'
import Achiv from '../../components/ui/Achiv'
import Modal from '../../components/modales/TaskModal'
import { NavLink } from 'react-router'
import { createLocalTask, toggleSubtaskCompleted, toggleTaskCompleted } from '../../data/Api'

export default function Home() {
  const [notif] = React.useState<number>(0);

  const [isOpen, setIsOpen] = React.useState<boolean>(false);

  const [tasks, setTasks] = useState<ITarea[]>(() => [
    createLocalTask({
      name: "Estudiar TypeScript",
      difficulty: "HARD",
      taskType: "habit",
      isComposed: false,
      subtasks: [],
      habitFrequency: "daily"
    })
  ]);
  
  const handleModalSubmit = (data: any) => {
    const newTask: ITarea = createLocalTask(data);
      
    setTasks((prevTasks) => [...prevTasks, newTask]);
    setIsOpen(false);
  };

  const handleToggleTaskCompleted = (taskId: string) => {
    setTasks((prevTasks) => toggleTaskCompleted(prevTasks, taskId));
  }
  
  const handleToggleSubtaskCompleted = (taskId: string, subtaskId: string) => {
    setTasks((prevTasks) => toggleSubtaskCompleted(prevTasks, taskId, subtaskId));
  }

  const filteredDiariasTasks = tasks.filter((t) => !t.completed);

  return (
    <main
      style={{ backgroundImage: `url(${bgImage})` }}
      className="relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white">

      {/* modal */}
      <div className={`${isOpen ? '' : 'hidden'} fixed inset-0 z-50 flex items-center justify-center`}>
        <Modal onSubmit={handleModalSubmit} onCancel={() => setIsOpen(false)} />
      </div>
      <Navbar />

      <section className="mx-auto flex w-full max-w-7xl flex-col items-center justify-center gap-4 px-4 py-6">
        {/* dashboard */}
        <article className="relative flex w-full max-w-2xl flex-col gap-6 rounded-2xl border border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-6 shadow-[0_15px_32px_#090b1f59]">
          <header>
            <p className="pb-2 text-[0.78rem] uppercase tracking-[0.08em] text-[#c9b7ff]">Bienvenido de vuelta</p>
            <h1 className="font-['Press_Start_2P'] text-xl sm:text-2xl">{'Hi, Astra\u00efs'}</h1>
            <p className="mt-1">{'Qué te queda por hacer?'}</p>
          </header>
          <div className="grid w-2/3 grid-cols-1 gap-2.5 sm:grid-cols-2">
            <button onClick={() => setIsOpen(true)} className="cursor-pointer rounded-xl border border-transparent bg-[linear-gradient(90deg,#8b5cf6,#3b82f6)] px-3 py-2 text-[#f8f9ff] transition-colors duration-200">Crear tarea</button>
            <NavLink  className="cursor-pointer text-center rounded-xl border border-white/25 bg-white/10 px-3 py-2 text-[#f8f9ff] transition-colors duration-200 hover:bg-white/20" to="/groups"><button>Crear un grupo</button></NavLink>
            <NavLink  className="cursor-pointer text-center rounded-xl border border-white/25 bg-white/10 px-3 py-2 text-[#f8f9ff] transition-colors duration-200 hover:bg-white/20" to="/profile"><button>Ver perfil</button></NavLink>
            <NavLink className="cursor-pointer text-center rounded-xl border border-white/25 bg-white/10 px-3 py-2 text-[#f8f9ff] transition-colors duration-200 hover:bg-white/20" to="/"><button >Cambiar la mascota</button></NavLink>
          </div>
          <img
            className="absolute -bottom-7 -right-56 z-10 w-9/10"
            src={astra} 
            alt={"AstraAstra"}
          />
        </article>

        <div className="grid w-full grid-cols-1 gap-4 lg:grid-cols-3">
          {/* Tareas Pendientes */}
          
          <article className="rounded-2xl border border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-4 shadow-[0_15px_32px_#090b1f59]">
            <header className="mb-3">
              <NavLink to="/tasks">
                <h2 className="font-['Press_Start_2P'] text-lg">Tareas Pendientes</h2>
              </NavLink>
            </header>
            <div className="flex flex-col gap-3">
              {filteredDiariasTasks.length === 0 ? (
              <p className="text-gray-400 italic text-center py-4">No hay tareas</p>
              ) : (
              filteredDiariasTasks.map((t, i) => (
                <Task key={t.id ?? i} data={t} onComplete={handleToggleTaskCompleted} onToggleSubtask={handleToggleSubtaskCompleted} />
              )))}
            </div>
          </article>

          {/* Tienda + Notificaciones + Logros */}
          <div className="flex flex-col gap-4 lg:col-span-2">
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              {/* Tienda */}
              <NavLink to="/shop">
                <article className="rounded-2xl border border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-4 shadow-[0_15px_32px_#090b1f59]">
                  <header className="mb-3">
                    <h2 className="font-['Press_Start_2P'] text-lg">Tienda</h2>
                  </header>
                  <button className="w-full">
                    <img src={shop} className="aspect-video max-w-full rounded-lg object-cover" alt="Tienda" />
                  </button>
                </article>
              </NavLink>

              {/* Notificaciones + Logros */}
              <div className="flex flex-col gap-4">
                <article className="rounded-2xl border border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-4 shadow-[0_15px_32px_#090b1f59]">
                  <header className="mb-3">
                    <button className="flex items-center gap-2">
                      <h2 className="font-['Press_Start_2P'] text-lg">Notificaciones</h2>
                      <span className="rounded-full bg-state-error px-2 py-1 text-xs text-white">{notif}</span>
                    </button>
                  </header>
                </article>

                <NavLink to="/achievements">
                  <article className="flex h-full flex-col gap-4 rounded-2xl border border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-4 shadow-[0_15px_32px_#090b1f59]">
                    <header className="mb-3">
                      <h2 className="font-['Press_Start_2P'] text-lg">Logros</h2>
                    </header>
                    <div className="flex flex-row justify-between">
                      <Achiv />
                      <Achiv />
                      <Achiv />
                    </div>
                  </article>
                </NavLink>
              </div>
            </div>

            {/* Minijuegos */}
            <article className="rounded-2xl border border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-4 shadow-[0_15px_32px_#090b1f59]">
              <header className="mb-3">
                <h2 className="font-['Press_Start_2P'] text-lg">Minijuegos</h2>
              </header>
              <div className="flex flex-col items-center gap-4">
                <img
                  src={game}
                  className="h-auto w-1/2 max-w-30 rounded-lg"
                  alt="Juego"
                />
                <NavLink to="/games">
                  <button className="w-full max-w-xs cursor-pointer rounded-xl border border-white/25 bg-white/10 px-3 py-2 text-[#f8f9ff] transition-colors duration-200 hover:bg-white/20">Jugar ahora</button>
                </NavLink>
              </div>
            </article>
          </div>
        </div>
      </section>
    </main>
  )
}
