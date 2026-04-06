import Navbar from "../../components/layout/Navbar"
import bgImage from '../../assets/homeScreenBack.jpg'
import GroupCard from "../../components/ui/GroupCard"
import GroupModal from "../../components/modales/GroupModal"
import React, { useState } from "react";
import type { ITarea } from "../../types/Interfaces";
import Modal from "../../components/modales/TaskModal";

export default function Groups() {
    const [isOpen, setIsOpen] = React.useState<boolean>(false);

    const [isOpenModal, setIsOpenModal] = React.useState<boolean>(false);

    const [activeGroup, setActiveGroup] = useState<number>(-1);
    
    const [tasks, setTasks] = useState<ITarea[]>([]);
    
    const handleActiveGroup = (active: number) => {
        if(isOpen == false || active != activeGroup) {
          setIsOpen(true);  
          setActiveGroup(active);
        } else {
            setIsOpen(false);
            setActiveGroup(-1);
        }
    }

    const handleModalSubmit = (data: any) => {
        // Datos de la tarea creada por el modal
        const newTask: ITarea = {
          title: data.name,
          dificultad: data.difficulty,
          recompensa: data.difficulty === "EASY" ? 20 : data.difficulty === "MEDIUM" ? 35 : 50,
          taskType: data.taskType, // "diary" o "habit"
          tags: data.tags || [], // solo si el usuario añadió tags
          isComposed: data.isComposed,
          subtasks: data.subtasks || [],
          habitFrequency: data.habitFrequency
        };
    
        setTasks([...tasks, newTask]);
        setIsOpenModal(false);
    };

    return (
        <div style={{ backgroundImage: `url(${bgImage})` }} className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white">
            <Navbar /> 
            <div className="md:flex md:flex-row justify-center px-5 grid grid-cols-1 gap-2 ">
                <div className="w-1/3 flex flex-col gap-2">
                    <GroupCard onClick={handleActiveGroup} id={0} activeId={activeGroup} />
                    <GroupCard onClick={handleActiveGroup} id={1} activeId={activeGroup} />
                    <GroupCard onClick={handleActiveGroup} id={2} activeId={activeGroup} />
                </div>
                <div className={`${isOpen ? '' : 'hidden'} flex flex-col gap-2 w-1/2`}>
                    <button onClick={() => setIsOpenModal(true)} className="ml-auto border border-[#F4E9E9] bg-accent-beige-300/25 rounded-md px-4 py-2 w-1/5"><span className="font-bold text-2xl">+ Añadir tarea</span></button>
                    <GroupModal data={tasks} />
                </div>
            </div>

            {/* modal */}
            <div className={`${isOpenModal ? "" : "hidden"} fixed inset-0 z-50 flex items-center justify-center`}>
                <Modal
                  onSubmit={handleModalSubmit}
                  onCancel={() => setIsOpenModal(false)}
                  />
            </div>
        </div>
    )
}