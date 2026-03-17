import Navbar from "../../components/layout/Navbar";
import Tarea from "../../components/ui/Tarea";
import bgImage from '../../assets/homeScreenBack.jpg'
import type { ITarea } from "../../types/Interfaces";
import Calendar from "../../components/layout/Calendar";
import React from "react";

export default function Tasks() {
      const tarea: ITarea = {
        title: "Estudiar TypeScript",
        dificultad: "HARD",
        recompensa: 50
      };

    const [active, isActive] = React.useState<boolean>();

    return (
    <div style={{ backgroundImage: `url(${bgImage})` }} className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white">
        <Navbar />

        <div className="flex flex-col gap-6 px-2">
            <button className="ml-auto border border-[#F4E9E9] bg-[#E8DCC4]/35 rounded-md px-4 py-2 w-1/5"><span className="font-bold text-2xl">+ Añadir tarea</span></button>

            <div className="flex flex-row gap-4 px-10 pt-5">
                {/* Diarias */}

                <div className="pb-2 w-1/3">
                    <h1 className="pb-5">Diarias</h1>
                    <div className="flex flex-col gap-2 justify-center">
                        {/* filtrado */}
                        <div className="flex flex-col gap-2.5">
                            <div className="flex flex-row gap-2.5 justify-center">
                                <button className={`rounded-full px-4 ${active ?  'bg-white text-black' : 'bg-black text-white' }`}><span className="font-bold">Today</span></button>
                                <button className={`rounded-full px-4 ${active ?  'bg-white text-black' : 'bg-black text-white' }`}><span className="font-bold">Tomorrow</span></button>
                                <button className={`rounded-full px-4 ${active ?  'bg-white text-black' : 'bg-black text-white' }`}><span className="font-bold">All</span></button>
                            </div>
                            <div className="flex flex-row gap-2.5 justify-center">
                                <button className="bg-state-success rounded-md px-4"><span className="text-primary-900 font-bold">Completadas</span></button>
                                <button className="bg-state-warning rounded-md px-4"><span className="text-primary-900 font-bold">Pendientes</span></button>
                            </div>
                        </div>


                        <Tarea data={tarea} checked={false} onChange={function (): void {
                                throw new Error("Function not implemented.");
                            } } />

                        <Tarea data={tarea} checked={false} onChange={function (): void { 
                            throw new Error("Function not implemented."); 
                            } } />
                    </div>
                </div>

                {/* Hábito */}

                <div className="pb-2 w-1/3">
                    <h1 className="pb-5">Hábitos</h1>
                    <div className="flex flex-col gap-2 justify-center">
                        {/* filtrado */}
                        <div className="flex flex-col gap-2.5">
                            <div className="flex flex-row gap-2.5 justify-center">
                                <button className={`rounded-full px-4 ${active ?  'bg-white text-black' : 'bg-black text-white' }`}><span className="font-bold">Today</span></button>
                                <button className={`rounded-full px-4 ${active ?  'bg-white text-black' : 'bg-black text-white' }`}><span className="font-bold">Tomorrow</span></button>
                                <button className={`rounded-full px-4 ${active ?  'bg-white text-black' : 'bg-black text-white' }`}><span className="font-bold">All</span></button>
                            </div>
                            <div className="flex flex-row gap-2.5 justify-center">
                                <button className="bg-state-success rounded-md px-4"><span className="text-primary-900 font-bold">Completadas</span></button>
                                <button className="bg-state-warning rounded-md px-4"><span className="text-primary-900 font-bold">Pendientes</span></button>
                            </div>
                        </div>


                        <Tarea data={tarea} checked={false} onChange={function (): void {
                                throw new Error("Function not implemented.");
                            } } />

                        <Tarea data={tarea} checked={false} onChange={function (): void { 
                            throw new Error("Function not implemented."); 
                            } } />
                    </div>
                </div>

                {/* Calendario */}
                <div className="w-1/3">
                    <Calendar />
                </div>
            </div>
        </div>
    </div>
  )
}