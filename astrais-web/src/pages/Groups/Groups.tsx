import Navbar from "../../components/layout/Navbar"
import bgImage from '../../assets/homeScreenBack.jpg'
import GroupCard from "../../components/ui/GroupCard"
import GroupModal from "../../components/modales/GroupModal"
import React, { useState } from "react";
import type { ITarea } from "../../types/Interfaces";
import Modal from "../../components/modales/TaskModal";
import GroupSettingsModal from "../../components/modales/GroupSettingsModal";
import { createLocalTask, filterTasksByCompleted, sortTasksByCompleted, toggleSubtaskCompleted, toggleTaskCompleted } from "../../data/Api";

interface IGroupData {
    id: number;
    name: string;
    description: string;
    photoUrl?: string | null;
    members: Array<{ id: number; name: string; avatar?: string }>;
    tasks: ITarea[];
}

export default function Groups() {
    const [isOpen, setIsOpen] = React.useState<boolean>(false);
    const [isOpenModal, setIsOpenModal] = React.useState<boolean>(false);
    const [activeGroup, setActiveGroup] = useState<number>(-1);
    const [isSettingsModalOpen, setIsSettingsModalOpen] = React.useState<boolean>(false);
    const [groupTaskFilters, setGroupTaskFilters] = useState({
        completed: false,
        pending: false
    });

    const [groups, setGroups] = useState<IGroupData[]>([
        {
            id: 0,
            name: 'Astraïs',
            description: 'Grupo de trabajo para el proyecto Astraïs',
            members: [
                { id: 1, name: 'Juan Pérez' },
                { id: 2, name: 'María García' },
                { id: 3, name: 'Carlos López' }
            ],
            tasks: []
        },
        {
            id: 1,
            name: 'Nebula',
            description: 'Diseño y narrativa del juego',
            members: [
                { id: 1, name: 'Lucía Torres' },
                { id: 2, name: 'Diego Ruiz' }
            ],
            tasks: []
        },
        {
            id: 2,
            name: 'Obviamente no astrais',
            description: 'Otro grupo de astrais',
            members: [
                { id: 1, name: 'Elena Martín' },
                { id: 2, name: 'Pablo Sanz' },
                { id: 3, name: 'Irene Gil' }
            ],
            tasks: []
        }
    ]);

    const activeGroupData = groups.find((group) => group.id === activeGroup) ?? null;
    const filteredGroupTasks = sortTasksByCompleted(filterTasksByCompleted(activeGroupData?.tasks ?? [], groupTaskFilters));

    const handleActiveGroup = (active: number) => {
        if (isOpen == false || active != activeGroup) {
            setIsOpen(true);
            setActiveGroup(active);
        } else {
            setIsOpen(false);
            setActiveGroup(-1);
        }
    }

    const handleModalSubmit = (data: any) => {
        if (activeGroup === -1) return;

        const newTask: ITarea = createLocalTask(data);

        setGroups((prevGroups) =>
            prevGroups.map((group) =>
                group.id === activeGroup
                    ? { ...group, tasks: [...group.tasks, newTask] }
                    : group
            )
        );
        setIsOpenModal(false);
    };

    const handleSaveSettings = (settings: {
        name: string;
        description: string;
        photo?: File | null;
        newMembers: string[];
    }) => {
        if (activeGroup === -1) return;

        const newPhotoUrl = settings.photo ? URL.createObjectURL(settings.photo) : undefined;

        setGroups((prevGroups) =>
            prevGroups.map((group) => {
                if (group.id !== activeGroup) return group;

                const nextMemberId =
                    group.members.reduce((maxId, member) => Math.max(maxId, member.id), 0) + 1;

                const createdMembers = settings.newMembers.map((member, index) => ({
                    id: nextMemberId + index,
                    name: member
                }));

                return {
                    ...group,
                    name: settings.name,
                    description: settings.description,
                    photoUrl: newPhotoUrl ?? group.photoUrl ?? null,
                    members: [...group.members, ...createdMembers]
                };
            })
        );

        console.log('Nuevos datos:', settings);
        setIsSettingsModalOpen(false);
    };

    const handleActiveTaskFilter = (active: string) => {
        if(active == "Completadas") {
            setGroupTaskFilters((prev) => ({
                ...prev,
                completed: !prev.completed
            }));
            return;
        }

        setGroupTaskFilters((prev) => ({
            ...prev,
            pending: !prev.pending
        }));
    }

    const handleToggleTaskCompleted = (taskId: string) => {
        if(activeGroup === -1) return;

        setGroups((prevGroups) =>
            prevGroups.map((group) =>
                group.id === activeGroup
                    ? { ...group, tasks: toggleTaskCompleted(group.tasks, taskId) }
                    : group
            )
        );
    }

    const handleToggleSubtaskCompleted = (taskId: string, subtaskId: string) => {
        if(activeGroup === -1) return;

        setGroups((prevGroups) =>
            prevGroups.map((group) =>
                group.id === activeGroup
                    ? { ...group, tasks: toggleSubtaskCompleted(group.tasks, taskId, subtaskId) }
                    : group
            )
        );
    }

    return (
        <div style={{ backgroundImage: `url(${bgImage})` }} className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white">
            <Navbar />
            <div className="md:flex md:flex-row justify-center px-5 grid grid-cols-1 gap-2 ">
                <div className="w-1/3 flex flex-col gap-2">
                    {groups.map((group) => (
                        <GroupCard key={group.id}onClick={handleActiveGroup} id={group.id} activeId={activeGroup}data={group}  />
                    ))}
                </div>
                <div className={`${isOpen ? '' : 'hidden'} flex flex-col gap-2 w-1/2`}>
                    <button disabled={!activeGroupData} onClick={() => setIsSettingsModalOpen(true)}className="border border-[#F4E9E9]/15 backdrop-blur-sm bg-accent-beige-300/25 rounded-md px-4 py-2 w-auto disabled:cursor-not-allowed disabled:opacity-60">
                        <span className="font-bold text-lg">Configuración</span>
                    </button>
                    <button disabled={!activeGroupData} onClick={() => setIsOpenModal(true)} className="ml-auto backdrop-blur-sm border border-[#F4E9E9]/15 bg-accent-beige-300/25 rounded-md px-4 py-2 w-1/5 disabled:cursor-not-allowed disabled:opacity-60">
                        <span className="font-bold text-2xl ">+ Añadir tarea</span>
                    </button>
                    <GroupModal data={filteredGroupTasks} groupName={activeGroupData?.name ?? "Grupo"} activeCompleted={groupTaskFilters.completed} activePending={groupTaskFilters.pending} handleActiveFilter={handleActiveTaskFilter} handleToggleComplete={handleToggleTaskCompleted}  handleToggleSubtask={handleToggleSubtaskCompleted}/>
                </div>
            </div>

            <div className={`${isOpenModal ? "" : "hidden"} fixed inset-0 z-50 flex items-center justify-center`}>
                <Modal onSubmit={handleModalSubmit} onCancel={() => setIsOpenModal(false)} />
            </div>

            <GroupSettingsModal  isOpen={isSettingsModalOpen}  onClose={() => setIsSettingsModalOpen(false)} initialData={ activeGroupData ?? { name: '',  description: '', members: [] } }  onSave={handleSaveSettings}/>
        </div>
    )
}
