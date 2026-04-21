import React, { useEffect, useState } from "react";
import { useSearchParams } from "react-router";
import bgImage from "../../assets/homeScreenBack.jpg";
import Navbar from "../../components/layout/Navbar";
import GroupCard from "../../components/ui/GroupCard";
import ButtonComplete from "../../components/ui/ButtonComplete";
import Task from "../../components/ui/Task";
import Modal from "../../components/modales/TaskModal";
import GroupSettingsModal from "../../components/modales/GroupSettingsModal";
import CreateGroupModal from "../../components/modales/CreateGroupModal";
import type { IGroup, ITarea } from "../../types/Interfaces";
import {
  buildCreateTaskRequest,
  buildEditTaskRequest,
  completeTask,
  createLocalTask,
  createGroup,
  createNewGroup,
  createTask,
  deleteGroup,
  deleteTask,
  editGroup,
  editTask,
  filterTasksByCompleted,
  getRootTasks,
  getTaskSubtasks,
  getTaskXpReward,
  getTasksFromGroup,
  getUserGroup,
  isTaskCompleted,
  removeTaskWithSubtasks,
  shouldRecreateTaskOnEdit,
  sortTasksByCompleted,
  toggleSubtaskCompleted,
  toggleTaskCompleted,
  type ITaskFormData,
  type ITaskFormSubtask
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

const createSubtaskFormData = (data: ITaskFormData, subtask: ITaskFormSubtask): ITaskFormData => ({
  name: subtask.name,
  description: "",
  difficulty: data.difficulty,
  taskType: "daily",
  isComposed: false,
  subtasks: [],
  habitFrequency: null,
  taskDate: data.taskDate
});

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
  const [loadedGroupIds, setLoadedGroupIds] = useState<number[]>([]);
  const [loadingGroups, setLoadingGroups] = useState<boolean>(true);
  const [loadingTasks, setLoadingTasks] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [searchParams, setSearchParams] = useSearchParams();

  useEffect(() => {
    const loadGroups = async () => {
      try {
        setLoadingGroups(true);
        setError(null);

        const userGroups = await getUserGroup();
        setGroups(userGroups.slice(1).map(mapUserGroupToLocalGroup));
      } catch (loadError) {
        console.error("Error al cargar los grupos:", loadError);
        setError("No se pudieron cargar los grupos.");
      } finally {
        setLoadingGroups(false);
      }
    };

    void loadGroups();
  }, []);

  useEffect(() => {
    if (groupToDelete.gid === -1) {
      return;
    }

    const deleteSelectedGroup = async () => {
      try {
        await deleteGroup(groupToDelete.gid, groupToDelete.role);

        setGroups((prevGroups) => prevGroups.filter((group) => group.gid !== groupToDelete.gid));
        setLoadedGroupIds((prevIds) => prevIds.filter((gid) => gid !== groupToDelete.gid));

        if (groupToDelete.gid === activeGroup) {
          setActiveGroup(-1);
          setIsOpen(false);
        }

        setIsSettingsModalOpen(false);
      } catch (deleteError) {
        console.error("Error al borrar el grupo:", deleteError);
        setError("No se pudo borrar el grupo.");
      } finally {
        setGroupToDelete({ gid: -1, role: -1 });
      }
    };

    void deleteSelectedGroup();
  }, [activeGroup, groupToDelete]);

  useEffect(() => {
    if (activeGroup === -1 || loadedGroupIds.includes(activeGroup)) {
      return;
    }

    const loadGroupTasks = async () => {
      try {
        setLoadingTasks(true);
        setError(null);

        const serverTasks = await getTasksFromGroup(activeGroup);
        setGroups((prevGroups) =>
          prevGroups.map((group) =>
            group.gid === activeGroup
              ? {
                  ...group,
                  tasks: serverTasks
                }
              : group
          )
        );
        setLoadedGroupIds((prevIds) => [...prevIds, activeGroup]);
      } catch (loadError) {
        console.error("Error al cargar las tareas del grupo:", loadError);
        setError("No se pudieron cargar las tareas del grupo.");
      } finally {
        setLoadingTasks(false);
      }
    };

    void loadGroupTasks();
  }, [activeGroup, loadedGroupIds]);

  React.useEffect(() => {
    if (searchParams.get("openCreateModal") !== "true") {
      return;
    }

    setIsCreateModalOpen(true);

    const nextSearchParams = new URLSearchParams(searchParams);
    nextSearchParams.delete("openCreateModal");
    setSearchParams(nextSearchParams, { replace: true });
  }, [searchParams, setSearchParams]);

  const sortedGroups = getSortedGroups(groups, activeGroup);
  const activeGroupData = groups.find((group) => group.gid === activeGroup);
  const filteredGroupTasks = sortTasksByCompleted(
    filterTasksByCompleted(getRootTasks(activeGroupData?.tasks ?? []), groupTaskFilters)
  );
  const selectedTaskSubtasks = initialDataModal && activeGroupData ? getTaskSubtasks(activeGroupData.tasks, initialDataModal.id) : [];

  const updateActiveGroupTasks = (updater: (tasks: ITarea[]) => ITarea[]) => {
    setGroups((prevGroups) =>
      prevGroups.map((group) =>
        group.gid === activeGroup
          ? {
              ...group,
              tasks: updater(group.tasks)
            }
          : group
      )
    );
  };

  const handleActiveGroup = (nextActiveGroup: number) => {
    if (!isOpen || nextActiveGroup !== activeGroup) {
      setIsOpen(true);
      setActiveGroup(nextActiveGroup);
      return;
    }

    setIsOpen(false);
    setActiveGroup(-1);
  };

  const closeTaskModalHandle = () => {
    setInitialDataModal(null);
    setIsOpenModal(false);
  };

  const createTaskWithSubtasks = async (data: ITaskFormData) => {
    if (activeGroup === -1) {
      return;
    }

    const parentTaskId = await createTask(buildCreateTaskRequest(activeGroup, data));
    const createdTasks: ITarea[] = [
      createLocalTask(data, {
        gid: activeGroup,
        id: parentTaskId
      })
    ];

    for (const subtask of data.isComposed ? data.subtasks : []) {
      const subtaskData = createSubtaskFormData(data, subtask);
      const subtaskId = await createTask(buildCreateTaskRequest(activeGroup, subtaskData, parentTaskId));

      createdTasks.push(
        createLocalTask(subtaskData, {
          gid: activeGroup,
          id: subtaskId,
          idObjetivo: parentTaskId,
          tipo: "UNIQUE"
        })
      );
    }

    updateActiveGroupTasks((groupTasks) => [...groupTasks, ...createdTasks]);
  };

  const recreateTaskWithChanges = async (currentTask: ITarea, currentSubtasks: ITarea[], data: ITaskFormData) => {
    await createTaskWithSubtasks(data);

    for (const subtask of currentSubtasks) {
      await deleteTask(subtask.id);
    }

    await deleteTask(currentTask.id);
    updateActiveGroupTasks((groupTasks) => removeTaskWithSubtasks(groupTasks, currentTask.id));
  };

  const editTaskWithSubtasks = async (currentTask: ITarea, currentSubtasks: ITarea[], data: ITaskFormData) => {
    await editTask(currentTask.id, buildEditTaskRequest(data));

    let nextTasks = (activeGroupData?.tasks ?? []).map((task) =>
      task.id === currentTask.id
        ? {
            ...task,
            titulo: data.name.trim(),
            descripcion: data.description.trim(),
            prioridad: data.difficulty,
            recompensaXp: getTaskXpReward(data.difficulty)
          }
        : task
    );

    const nextSubtasksById = new Map(data.subtasks.map((subtask) => [`${subtask.id}`, subtask]));

    for (const subtask of currentSubtasks) {
      if (!nextSubtasksById.has(`${subtask.id}`)) {
        await deleteTask(subtask.id);
        nextTasks = nextTasks.filter((task) => task.id !== subtask.id);
      }
    }

    for (const subtask of data.subtasks) {
      const existingSubtask = currentSubtasks.find((currentSubtask) => `${currentSubtask.id}` === `${subtask.id}`);

      if (existingSubtask) {
        if (existingSubtask.titulo !== subtask.name.trim() || existingSubtask.prioridad !== data.difficulty) {
          await editTask(existingSubtask.id, {
            titulo: subtask.name.trim(),
            descripcion: existingSubtask.descripcion,
            prioridad: `${data.difficulty}`
          });
        }

        nextTasks = nextTasks.map((task) =>
          task.id === existingSubtask.id
            ? {
                ...task,
                titulo: subtask.name.trim(),
                prioridad: data.difficulty,
                recompensaXp: getTaskXpReward(data.difficulty)
              }
            : task
        );
        continue;
      }

      const subtaskData = createSubtaskFormData(data, subtask);
      const newSubtaskId = await createTask(buildCreateTaskRequest(activeGroup, subtaskData, currentTask.id));

      nextTasks = [
        ...nextTasks,
        createLocalTask(subtaskData, {
          gid: activeGroup,
          id: newSubtaskId,
          idObjetivo: currentTask.id,
          tipo: "UNIQUE"
        })
      ];
    }

    updateActiveGroupTasks(() => nextTasks);
  };

  const handleModalSubmit = async (data: ITaskFormData) => {
    try {
      if (!initialDataModal) {
        await createTaskWithSubtasks(data);
        closeTaskModalHandle();
        return;
      }

      const currentSubtasks = activeGroupData ? getTaskSubtasks(activeGroupData.tasks, initialDataModal.id) : [];

      if (shouldRecreateTaskOnEdit(initialDataModal, currentSubtasks, data)) {
        await recreateTaskWithChanges(initialDataModal, currentSubtasks, data);
      } else {
        await editTaskWithSubtasks(initialDataModal, currentSubtasks, data);
      }

      closeTaskModalHandle();
    } catch (submitError) {
      console.error("Error al guardar la tarea del grupo:", submitError);
      setError("No se pudieron guardar los cambios de la tarea.");
    }
  };

  const handleDeleteTask = async () => {
    if (!initialDataModal || !activeGroupData) {
      return;
    }

    try {
      const currentSubtasks = getTaskSubtasks(activeGroupData.tasks, initialDataModal.id);

      for (const subtask of currentSubtasks) {
        await deleteTask(subtask.id);
      }

      await deleteTask(initialDataModal.id);
      updateActiveGroupTasks((groupTasks) => removeTaskWithSubtasks(groupTasks, initialDataModal.id));
      closeTaskModalHandle();
    } catch (deleteError) {
      console.error("Error al borrar la tarea del grupo:", deleteError);
      setError("No se pudo borrar la tarea.");
    }
  };

  const handleCreateGroup = async (data: { name: string; description: string; photo?: File | null }) => {
    try {
      const createdGroupId = await createGroup({ name: data.name, desc: data.description });
      const newGroup: IGroup = createNewGroup(data, createdGroupId);
      setGroups((prevGroups) => [...prevGroups, newGroup]);
      setIsCreateModalOpen(false);
    } catch (createError) {
      console.error("Error al crear el grupo:", createError);
      setError("No se pudo crear el grupo.");
    }
  };

  const handleSaveSettings = async (settings: {
    gid: number;
    name: string;
    description: string;
    photo?: File | null;
    newMembers: string[];
  }) => {
    if (activeGroup === -1) {
      return;
    }

    try {
      await editGroup({ gid: settings.gid, name: settings.name, desc: settings.description });
    } catch (saveError) {
      console.error("Error al guardar la configuracion del grupo:", saveError);
      setError("No se pudieron guardar los cambios del grupo.");
    }

    const newPhotoUrl = settings.photo ? URL.createObjectURL(settings.photo) : undefined;

    setGroups((prevGroups) =>
      prevGroups.map((group) => {
        if (group.gid !== activeGroup) {
          return group;
        }

        const nextMemberId = group.members.reduce((maxId, member) => Math.max(maxId, member.id), 0) + 1;
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

  const handleToggleTaskCompleted = async (taskId: number) => {
    try {
      await completeTask(taskId);
    } catch (completeError) {
      console.error("Error al completar la tarea del grupo:", completeError);
    } finally {
      updateActiveGroupTasks((groupTasks) => toggleTaskCompleted(groupTasks, `${taskId}`));
    }
  };

  const handleToggleSubtaskCompleted = async (taskId: number, subtaskId: number) => {
    const subtask = activeGroupData?.tasks.find((task) => task.id === subtaskId);
    const shouldSyncParent = subtask ? !isTaskCompleted(subtask) : false;

    try {
      await completeTask(subtaskId);
      if (shouldSyncParent) {
        const siblingSubtasks = getTaskSubtasks(activeGroupData?.tasks ?? [], taskId).filter((task) => task.id !== subtaskId);

        if (siblingSubtasks.every((task) => isTaskCompleted(task))) {
          await completeTask(taskId);
        }
      }
    } catch (completeError) {
      console.error("Error al completar la subtarea del grupo:", completeError);
    } finally {
      updateActiveGroupTasks((groupTasks) => toggleSubtaskCompleted(groupTasks, `${taskId}`, `${subtaskId}`));
    }
  };

  const editTaskHandle = (taskId: number) => {
    if (!activeGroupData) {
      return;
    }

    const taskToEdit = activeGroupData.tasks.find((task) => task.id === taskId);

    if (!taskToEdit) {
      return;
    }

    setInitialDataModal(taskToEdit);
    setIsOpenModal(true);
  };

  return (
    <div style={{ backgroundImage: `url(${bgImage})` }} className="relative flex min-h-screen flex-col gap-4 bg-cover bg-center font-['Space_Grotesk'] text-white">
      <Navbar />

      <div className="grid grid-cols-1 gap-2 px-5 md:flex md:flex-row md:justify-center">
        <div className="flex w-full flex-col gap-2 md:w-1/3">
          <button onClick={() => setIsCreateModalOpen(true)} className="w-full rounded-md border border-[#F4E9E9]/15 bg-accent-beige-300/25 px-4 py-2 backdrop-blur-sm disabled:cursor-not-allowed disabled:opacity-60">
            <span className="text-2xl font-bold">Crear grupo</span>
          </button>
          {loadingGroups ? (
            <p className="py-4 text-center italic text-gray-300">Cargando grupos...</p>
          ) : (
            sortedGroups.map((group) => (
              <GroupCard key={`${group.gid}-${group.name}`} onClick={handleActiveGroup} id={group.gid} activeId={activeGroup} data={group} />
            ))
          )}
        </div>

        <div className={`${isOpen ? "" : "hidden"} flex w-full flex-col gap-2 md:w-1/2`}>
          <div className="flex w-full flex-row">
            <button disabled={!activeGroupData} onClick={() => setIsSettingsModalOpen(true)} className="rounded-md border border-[#F4E9E9]/15 bg-accent-beige-300/25 px-4 py-2 backdrop-blur-sm disabled:cursor-not-allowed disabled:opacity-60">
              <span className="text-2xl font-bold">Configuracion</span>
            </button>
            <button
              disabled={!activeGroupData}
              onClick={() => {
                setInitialDataModal(null);
                setIsOpenModal(true);
              }}
              className="ml-auto rounded-md border border-[#F4E9E9]/15 bg-accent-beige-300/25 px-4 py-2 backdrop-blur-sm disabled:cursor-not-allowed disabled:opacity-60"
            >
              <span className="text-2xl font-bold">+ Anadir tarea</span>
            </button>
          </div>

          {error && <p className="px-2 text-sm text-red-200">{error}</p>}

          <div className="mx-2 flex flex-col gap-2 font-['Space_Grotesk']">
            <div className="flex flex-row items-center gap-2">
              <div className="rounded-md bg-accent-beige-300/35 px-4 py-2 font-['Press_Start_2P'] font-bold">
                <h2>{activeGroupData?.name ?? "Grupo"}</h2>
              </div>

              <div className="rounded-full border border-white bg-black px-2">
                <p className="text-white">Filtrar:</p>
              </div>

              <ButtonComplete title="Completadas" active={groupTaskFilters.completed} handleActive={handleActiveTaskFilter} />
              <ButtonComplete title="Pendientes" active={groupTaskFilters.pending} handleActive={handleActiveTaskFilter} />
            </div>

            <div className="flex flex-col gap-2">
              {loadingTasks ? (
                <p className="py-4 text-center italic text-gray-300">Cargando tareas...</p>
              ) : filteredGroupTasks.length === 0 ? (
                <p className="py-4 text-center italic text-gray-400">No hay tareas</p>
              ) : (
                filteredGroupTasks.map((task) => (
                  <Task
                    key={task.id}
                    data={task}
                    subtasks={getTaskSubtasks(activeGroupData?.tasks ?? [], task.id)}
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
        <Modal
          onSubmit={handleModalSubmit}
          onCancel={closeTaskModalHandle}
          onDelete={initialDataModal ? handleDeleteTask : null}
          initialData={initialDataModal}
          subtasks={selectedTaskSubtasks}
        />
      </div>

      {activeGroupData && (
        <GroupSettingsModal
          isOpen={isSettingsModalOpen}
          onClose={() => setIsSettingsModalOpen(false)}
          initialData={activeGroupData}
          onSave={handleSaveSettings}
          onDelete={({ gid, role }) => setGroupToDelete({ gid, role })}
        />
      )}

      <CreateGroupModal isOpen={isCreateModalOpen} onClose={() => setIsCreateModalOpen(false)} onSave={handleCreateGroup} />
    </div>
  );
}
