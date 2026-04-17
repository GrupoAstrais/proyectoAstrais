import React, { useEffect, useState } from "react";
import { useSearchParams } from "react-router";
import bgImage from '../../assets/homeScreenBack.jpg';
import Navbar from "../../components/layout/Navbar";
import GroupCard from "../../components/ui/GroupCard";
import ButtonComplete from "../../components/ui/ButtonComplete";
import Task from "../../components/ui/Task";
import Modal from "../../components/modales/TaskModal";
import GroupSettingsModal from "../../components/modales/GroupSettingsModal";
import CreateGroupModal from "../../components/modales/CreateGroupModal";
import type { IGroup, ITarea } from "../../types/Interfaces";
import {
    createGroup,
    createLocalTask,
    createNewGroup,
    deleteGroup,
    filterTasksByCompleted,
    getUserGroup,
    sortTasksByCompleted,
    toggleSubtaskCompleted,
    toggleTaskCompleted
} from "../../data/Api";

const compareGroupsAlphabetically = (firstGroup: IGroup, secondGroup: IGroup): number => {
    return firstGroup.name.localeCompare(secondGroup.name, "es", { sensitivity: "base" });
};

const mapUserGroupToLocalGroup = (group: {
    gid?: number;
    id?: number;
    name?: string;
    nombre?: string;
    description: string;
    role: number;
}): IGroup => {
    return {
        gid: group.gid ?? group.id ?? -1,
        name: group.name ?? group.nombre ?? "Grupo",
        description: group.description,
        members: [],
        tasks: [],
        role: group.role
    };
};

const getSortedGroups = (groups: IGroup[], activeGroup: number): IGroup[] => {
    return [...groups].sort((firstGroup, secondGroup) => {
        const firstGroupIsActive = firstGroup.gid === activeGroup;
        const secondGroupIsActive = secondGroup.gid === activeGroup;

        if (firstGroupIsActive && !secondGroupIsActive) return -1;
        if (!firstGroupIsActive && secondGroupIsActive) return 1;

        return compareGroupsAlphabetically(firstGroup, secondGroup);
    });
};

export default function Groups() {
    const [isOpen, setIsOpen] = React.useState<boolean>(false);
    const [isOpenModal, setIsOpenModal] = React.useState<boolean>(false);
    const [activeGroup, setActiveGroup] = useState<number>(-1);
    const [isSettingsModalOpen, setIsSettingsModalOpen] = React.useState<boolean>(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = React.useState<boolean>(false);
    const [initialDataModal, setInitialDataModal] = useState<ITarea | null>(null);
    const [groupTaskFilters, setGroupTaskFilters] = useState({
        completed: false,
        pending: false
    });
    const [groupToDelete, setGroupToDelete] = useState<{ gid: number; role: number }>({ gid: -1, role: -1 });

    const [groups, setGroups] = useState<IGroup[]>([]);

    useEffect(() => {
        const loadData = async () => {
            try {
                const d = await getUserGroup();
                setGroups(d.slice(1).map(mapUserGroupToLocalGroup));
            } catch (error) {
                console.error("Error fetching user groups:", error);
            } finally {
                console.log("Final")
            }
        };
        loadData();

    }, []);

    useEffect(() => {
        if (groupToDelete.gid === -1) return;

        const loadData = async () => {
            await deleteGroup(groupToDelete.gid, groupToDelete.role).then(() => {
                console.log("Grupo borrad con éxito");

                setGroups((prev) => prev.filter(group => group.gid !== groupToDelete.gid));
                if (groupToDelete.gid === activeGroup) {
                    setActiveGroup(-1);
                    setIsOpen(false);
                }
                setIsSettingsModalOpen(false);
            }).catch((error) => {
                console.error("Error al borrar el grupo:", error);
            });
        };
        loadData();

    }, [groupToDelete]);


    const [searchParams, setSearchParams] = useSearchParams();
    const sortedGroups = getSortedGroups(groups, activeGroup);
    const activeGroupData = groups.find((group) => group.gid === activeGroup);
    const filteredGroupTasks = sortTasksByCompleted(
        filterTasksByCompleted(activeGroupData?.tasks ?? [], groupTaskFilters)
    );

    React.useEffect(() => {
        if (searchParams.get('openCreateModal') !== 'true') return;

        setIsCreateModalOpen(true);

        const nextSearchParams = new URLSearchParams(searchParams);
        nextSearchParams.delete('openCreateModal');
        setSearchParams(nextSearchParams, { replace: true });
    }, [searchParams, setSearchParams]);

    const handleActiveGroup = (active: number) => {
        if (isOpen === false || active !== activeGroup) {
            setIsOpen(true);
            setActiveGroup(active);
        } else {
            setIsOpen(false);
            setActiveGroup(-1);
        }
    };


    const closeTaskModalHandle = () => {
        setInitialDataModal(null);
        setIsOpenModal(false);
    };

    const handleModalSubmit = (data: any) => {
        if (activeGroup === -1) return;

        if (initialDataModal?.id) {
            const updatedTask: ITarea = {
                ...createLocalTask(data),
                id: initialDataModal.id,
                completed: initialDataModal.completed
            };

            setGroups((prevGroups) =>
                prevGroups.map((group) =>
                    group.gid === activeGroup
                        ? {
                            ...group,
                            tasks: group.tasks.map((task) =>
                                task.id === initialDataModal.id ? updatedTask : task
                            )
                        }
                        : group
                )
            );
            closeTaskModalHandle();
            return;
        }

        const newTask: ITarea = createLocalTask(data);

        setGroups((prevGroups) =>
            prevGroups.map((group) =>
                group.gid === activeGroup
                    ? { ...group, tasks: [...group.tasks, newTask] }
                    : group
            )
        );
        closeTaskModalHandle();
    };

    const handleCreateGroup = async (data: any) => {

        const create = async () => {
            try {
                const d = createGroup({name: data.name, desc: data.description});
                const newGroup: IGroup = createNewGroup(data, await d);
                setGroups((prev) => [...prev, newGroup]);
            } catch (error) {
                console.error("Error fetching user groups:", error);
            } finally {
                console.log("Final")
            }
        };

        create();
        setIsCreateModalOpen(false);
    }


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
                if (group.gid !== activeGroup) return group;

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
        if (active === "Completadas") {
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
    };

    const handleToggleTaskCompleted = (taskId: string) => {
        if (activeGroup === -1) return;

        setGroups((prevGroups) =>
            prevGroups.map((group) =>
                group.gid === activeGroup
                    ? { ...group, tasks: toggleTaskCompleted(group.tasks, taskId) }
                    : group
            )
        );
    };

    const handleToggleSubtaskCompleted = (taskId: string, subtaskId: string) => {
        if (activeGroup === -1) return;

        setGroups((prevGroups) =>
            prevGroups.map((group) =>
                group.gid === activeGroup
                    ? { ...group, tasks: toggleSubtaskCompleted(group.tasks, taskId, subtaskId) }
                    : group
            )
        );
    };

    const editTaskHandle = (taskId: string) => {
        if (!activeGroupData) return;

        const taskToEdit = activeGroupData.tasks.find((task) => task.id === taskId);

        if (!taskToEdit) return;

        setInitialDataModal(taskToEdit);
        setIsOpenModal(true);
    };


    return (
        <div style={{ backgroundImage: `url(${bgImage})` }} className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white">
            <Navbar />

            <div className="md:flex md:flex-row justify-center px-5 grid grid-cols-1 gap-2 ">
                <div className="w-1/3 flex flex-col gap-2">
                    <button onClick={() => setIsCreateModalOpen(true)} className="backdrop-blur-sm border border-[#F4E9E9]/15 bg-accent-beige-300/25 rounded-md px-4 py-2 w-full disabled:cursor-not-allowed disabled:opacity-60">
                        <span className="font-bold text-2xl ">Crear grupo</span>
                    </button>
                    {sortedGroups.map((group) => (
                        <GroupCard key={group.gid+"-"+group.name} onClick={handleActiveGroup} id={group.gid} activeId={activeGroup} data={group} />
                    ))}
                </div>

                <div className={`${isOpen ? '' : 'hidden'} flex flex-col gap-2 w-1/2`}>
                    <div className="flex flex-row  w-full">
                        <button disabled={!activeGroupData} onClick={() => setIsSettingsModalOpen(true)} className="border border-[#F4E9E9]/15 backdrop-blur-sm bg-accent-beige-300/25 rounded-md px-4 py-2 disabled:cursor-not-allowed disabled:opacity-60">
                            <span className="font-bold text-2xl">Configuración</span>
                        </button>
                        <button
                            disabled={!activeGroupData}
                            onClick={() => {
                                setInitialDataModal(null);
                                setIsOpenModal(true);
                            }}
                            className="ml-auto backdrop-blur-sm border border-[#F4E9E9]/15 bg-accent-beige-300/25 rounded-md px-4 py-2 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            <span className="font-bold text-2xl ">+ Añadir tarea</span>
                        </button>
                    </div>

                    <div className="font-['Space_Grotesk'] flex flex-col gap-2 mx-2">
                        <div className="flex flex-row gap-2 items-center">
                            <div className="bg-accent-beige-300/35 rounded-md px-4 py-2 font-bold font-['Press_Start_2P']">
                                <h2>{activeGroupData?.name ?? "Grupo"}</h2>
                            </div>

                            <div className="rounded-full px-2 border border-white bg-black">
                                <p className="text-white">Filtrar:</p>
                            </div>

                            <ButtonComplete title="Completadas" active={groupTaskFilters.completed} handleActive={handleActiveTaskFilter} />
                            <ButtonComplete title="Pendientes" active={groupTaskFilters.pending} handleActive={handleActiveTaskFilter} />
                        </div>

                        <div className="flex flex-col gap-2">
                            {filteredGroupTasks.length === 0 ? (
                                <p className="text-gray-400 italic text-center py-4">No hay tareas</p>
                            ) : (
                                filteredGroupTasks.map((task, i) => (
                                    <Task
                                        key={ i}
                                        data={task}
                                        onComplete={handleToggleTaskCompleted}
                                        onToggleSubtask={handleToggleSubtaskCompleted}
                                        onToggleConfig={editTaskHandle}
                                    />
                                ))
                            )}
                        </div>
                    </div>
                </div>
            </div>

            <div className={`${isOpenModal ? "" : "hidden"} fixed inset-0 z-50 flex items-center justify-center`}>
                <Modal onSubmit={handleModalSubmit} onCancel={closeTaskModalHandle} initialData={initialDataModal} />
            </div>

            {activeGroupData && <GroupSettingsModal isOpen={isSettingsModalOpen} onClose={() => setIsSettingsModalOpen(false)} initialData={activeGroupData!!} onSave={handleSaveSettings} onDelete={({gid, role}) => setGroupToDelete({gid, role})} /> }
            <CreateGroupModal isOpen={isCreateModalOpen} onClose={() => setIsCreateModalOpen(false)} onSave={handleCreateGroup} />
        </div>
    );
}
