import Navbar from "../../components/layout/Navbar";
import Tarea from "../../components/ui/Tarea";
import bgImage from '../../assets/homeScreenBack.jpg'
import type { ITarea } from "../../types/Interfaces";
import Calendar from "../../components/layout/Calendar";
import React, { useState } from "react";
import Modal from "../../components/ui/Modal";
import ButtonFiltro from "../../components/ui/ButtonFiltro";

export default function Tasks() {
      const tarea: ITarea = {
        title: "Estudiar TypeScript",
        dificultad: "HARD",
        recompensa: 50
      };

    
    const [isOpen, setIsOpen] = React.useState<boolean>(false);
    
    const confirmarModal = (confirmar: string) => {
        setIsOpen(false);
    
        if(confirmar == "Confirmar") {
            {/* add tarea */}
        }
    }

    const [activeDiarias, setActiveDiarias] = useState<string>("");

    const [activeHabitos, setActiveHabitos] = useState<string>("");

    //de momento no sirve
    const handleActiveDiarias = (active: string) => {
        setActiveDiarias(active);
    }

    const handleActiveHabitos= (active: string) => {
        setActiveHabitos(active)
    }



    

    return (
    <div style={{ backgroundImage: `url(${bgImage})` }} className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white">
        {/* modal */}
        <div className={`${isOpen ? '' : 'hidden'} fixed inset-0 z-50 flex items-center justify-center`}>
            <Modal onPress={confirmarModal}  />
        </div>
        
        <Navbar />

        <div className="flex flex-col gap-6 px-2">
            <button onClick={() => setIsOpen(true)} className="ml-auto border border-[#F4E9E9] bg-[#E8DCC4]/35 rounded-md px-4 py-2 w-1/5"><span className="font-bold text-2xl">+ Añadir tarea</span></button>

            <div className="flex flex-row gap-4 px-10 pt-5">
                {/* Diarias */}

                <div className="pb-2 w-1/3">
                    <h1 className="pb-5 text-3xl">Diarias</h1>
                    <div className="flex flex-col gap-2 justify-center">
                        {/* filtrado */}
                        <div className="flex flex-col gap-2.5">
                            <div className="flex flex-row gap-2.5 justify-center">
                                <ButtonFiltro esOtroActivo={activeDiarias} handleActive={handleActiveDiarias} titulo={"Today"}/>
                                <ButtonFiltro esOtroActivo={activeDiarias} handleActive={handleActiveDiarias} titulo={"Tomorrow"}/>
                                <ButtonFiltro esOtroActivo={activeDiarias} handleActive={handleActiveDiarias} titulo={"All"}/>
                            </div>
                            <div className="flex flex-row gap-2.5 justify-center">
                                <button className="bg-state-success rounded-md px-4"><span className="text-primary-900 font-bold">Completadas</span></button>
                                <button className="bg-state-warning rounded-md px-4"><span className="text-primary-900 font-bold">Pendientes</span></button>
                            </div>
                        </div>


                        <Tarea data={tarea} />

                        <Tarea data={tarea}/>
                    </div>
                </div>

                {/* Hábito */}

                <div className="pb-2 w-1/3">
                    <h1 className="pb-5 text-3xl">Hábitos</h1>
                    <div className="flex flex-col gap-2 justify-center">
                        {/* filtrado */}
                        <div className="flex flex-col gap-2.5">
                            <div className="flex flex-row gap-2.5 justify-center">
                                <ButtonFiltro esOtroActivo={activeHabitos} handleActive={handleActiveHabitos} titulo={"Today"}/>
                                <ButtonFiltro esOtroActivo={activeHabitos} handleActive={handleActiveHabitos} titulo={"Tomorrow"}/>
                                <ButtonFiltro esOtroActivo={activeHabitos} handleActive={handleActiveHabitos} titulo={"All"}/>
                            </div>
                            <div className="flex flex-row gap-2.5 justify-center">
                                <button className="bg-state-success rounded-md px-4"><span className="text-primary-900 font-bold">Completadas</span></button>
                                <button className="bg-state-warning rounded-md px-4"><span className="text-primary-900 font-bold">Pendientes</span></button>
                            </div>
                        </div>


                        <Tarea data={tarea}  />

                        <Tarea data={tarea} />
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