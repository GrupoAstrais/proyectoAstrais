import Navbar from "../../components/layout/Navbar";
import Task from "../../components/ui/Task";
import bgImage from '../../assets/homeScreenBack.jpg'
import type { ITarea } from "../../types/Interfaces";
import Calendar from "../../components/layout/Calendar";
import React, { useState } from "react";
import Modal from "../../components/modales/Modal";
import ButtonFilter from "../../components/ui/ButtonFilter";
import ButtonComplete from "../../components/ui/ButtonComplete";

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
            <button onClick={() => setIsOpen(true)} className="ml-auto border border-[#F4E9E9] bg-accent-beige-300/25 rounded-md px-4 py-2 w-1/5"><span className="font-bold text-2xl">+ Añadir tarea</span></button>

            <div className="md:flex md:flex-row gap-4 px-10 pt-5 sm:grid sm:grid-cols-2 ">
                {/* Diarias */}

                <div className="pb-2 md:w-1/3">
                    <h1 className="pb-5 text-3xl">Diarias</h1>
                    <div className="flex flex-col gap-2 justify-center">
                        {/* filtrado */}
                        <div className="flex flex-col gap-2.5">
                            <div className="flex flex-row gap-2.5 justify-center">
                                <ButtonFilter esOtroActivo={activeDiarias} handleActive={handleActiveDiarias} titulo={"Today"}/>
                                <ButtonFilter esOtroActivo={activeDiarias} handleActive={handleActiveDiarias} titulo={"Tomorrow"}/>
                                <ButtonFilter esOtroActivo={activeDiarias} handleActive={handleActiveDiarias} titulo={"All"}/>
                            </div>
                            <div className="flex flex-row gap-2.5 justify-center">
                                <ButtonComplete title="Completadas" />
                                <ButtonComplete title="Pendientes" />
                            </div>
                        </div>


                        <Task data={tarea} />
                        <Task data={tarea}/>
                    
                    </div>
                </div>

                {/* Hábito */}

                <div className="pb-2 md:w-1/3">
                    <h1 className="pb-5 text-3xl">Hábitos</h1>
                    <div className="flex flex-col gap-2 justify-center">
                        {/* filtrado */}
                        <div className="flex flex-col gap-2.5">
                            <div className="flex flex-row gap-2.5 justify-center">
                                <ButtonFilter esOtroActivo={activeHabitos} handleActive={handleActiveHabitos} titulo={"Today"}/>
                                <ButtonFilter esOtroActivo={activeHabitos} handleActive={handleActiveHabitos} titulo={"Tomorrow"}/>
                                <ButtonFilter esOtroActivo={activeHabitos} handleActive={handleActiveHabitos} titulo={"All"}/>
                            </div>
                            <div className="flex flex-row gap-2.5 justify-center">
                                <ButtonComplete title="Completadas" />
                                <ButtonComplete title="Pendientes" />
                            </div>
                        </div>


                        <Task data={tarea} />
                        <Task data={tarea} />
                        <Task data={tarea} />
                        <Task data={tarea} />
                        
                    </div>
                </div>

                {/* Calendario */}
               <div className="md:w-1/3 flex flex-col">
                    <Calendar />
                </div>
            </div>
        </div>
    </div>
  )
}