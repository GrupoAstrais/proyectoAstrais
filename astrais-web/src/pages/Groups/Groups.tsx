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
import NotificationModal from "../../components/modales/NotificationModal";
import type { IGroup, ITarea } from "../../types/Interfaces";
import {
  addUserToGroup,
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
  groupInviteLink,
  groupInvitacion,
  groupJoinByCode,
  groupJoinByLink,
  getTaskSubtasks,
  getTaskXpReward,
  getTasksFromGroup,
  getUserData,
  getUserGroup,
  isTaskCompleted,
  isTaskVisibleInDefaultList,
  passOwnershipGroup,
  removeUserFromGroup,
  removeTaskWithSubtasks,
  setMemberRole,
  sortTasksByCompleted,
  toggleSubtaskCompleted,
  toggleTaskCompleted,
  uncompleteTask,
  type ITaskFormData,
  userLeaveGroup,
  membersGroups
} from "../../data/Api";
import type { MembersResponse } from "../../types/LoginRequest";

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

const normalizeObjectiveId = (idObjetivo?: number): number | undefined => {
  return typeof idObjetivo === "number" && idObjetivo >= 0 ? idObjetivo : undefined;
};

const normalizeTaskFormData = (data: ITaskFormData, fallbackObjetivoId?: number): ITaskFormData => ({
  ...data,
  idObjetivo: normalizeObjectiveId(data.idObjetivo) ?? normalizeObjectiveId(fallbackObjetivoId)
});

export default function Groups() {
  const [isOpen, setIsOpen] = React.useState<boolean>(false);
  const [isOpenModal, setIsOpenModal] = React.useState<boolean>(false);
  const [activeGroup, setActiveGroup] = useState<number>(-1);
  const [isSettingsModalOpen, setIsSettingsModalOpen] = React.useState<boolean>(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = React.useState<boolean>(false);
  const [isJoinModalOpen, setIsJoinModalOpen] = React.useState<boolean>(false);
  const [joinGroupInput, setJoinGroupInput] = React.useState<string>("");
  const [joinGroupLoading, setJoinGroupLoading] = React.useState<boolean>(false);
  const [joinGroupError, setJoinGroupError] = React.useState<string | null>(null);
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
  const [groupMembersMap, setGroupMembersMap] = useState<Record<number, MembersResponse[]>>({});
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);
  const [personalGroupId, setPersonalGroupId] = useState<number | null>(null);
  const [rewardNotification, setRewardNotification] = useState<{ xp: number; ludiones: number } | null>(null);
  const showRewardNotification = (xp: number, ludiones: number) => {
    setRewardNotification(null);
    window.setTimeout(() => setRewardNotification({ xp, ludiones }), 0);
  };

  const toRenderableGroups = (userGroups: Array<{
    gid?: number;
    id?: number;
    name?: string;
    nombre?: string;
    description: string;
    role: number;
  }>): IGroup[] =>
    userGroups
      .filter((group) => (group.gid ?? group.id ?? -1) !== personalGroupId)
      .map(mapUserGroupToLocalGroup);

  const preloadMembersForGroups = async (baseGroups: IGroup[]): Promise<IGroup[]> => {
    const entries = await Promise.all(
      baseGroups.map(async (group) => {
        try {
          const serverMembers = await membersGroups(group.gid);
          const normalizedMembers = (Array.isArray(serverMembers) ? serverMembers : []).filter(
            (member): member is MembersResponse =>
              !!member &&
              typeof member.uid === "number" &&
              Number.isFinite(member.uid) &&
              typeof member.name === "string"
          );
          return [group.gid, normalizedMembers] as const;
        } catch {
          return [group.gid, []] as const;
        }
      })
    );

    const mapFromServer: Record<number, MembersResponse[]> = Object.fromEntries(entries);
    setGroupMembersMap((prevMap) => ({ ...prevMap, ...mapFromServer }));

    return baseGroups.map((group) => ({
      ...group,
      members: (mapFromServer[group.gid] ?? []).map((member) => ({
        id: member.uid,
        name: member.name
      }))
    }));
  };

  useEffect(() => {
    const loadGroups = async () => {
      try {
        setLoadingGroups(true);
        setError(null);

        const [userGroups, userData] = await Promise.all([getUserGroup(), getUserData()]);
        setCurrentUserId(userData.id);
        setPersonalGroupId(userData.personalGid);
        const baseGroups = userGroups
          .filter((group) => (group.gid ?? group.id ?? -1) !== userData.personalGid)
          .map(mapUserGroupToLocalGroup);
        const groupsWithMembers = await preloadMembersForGroups(baseGroups);
        setGroups(groupsWithMembers);
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
    if (!rewardNotification) return;
    const timeoutId = window.setTimeout(() => setRewardNotification(null), 2200);
    return () => window.clearTimeout(timeoutId);
  }, [rewardNotification]);

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
                  tasks: serverTasks ?? []
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

  const loadGroupMembers = async (gid: number) => {
    try {
      const serverMembers = await membersGroups(gid);
      const normalizedMembers = (Array.isArray(serverMembers) ? serverMembers : []).filter(
        (member): member is MembersResponse =>
          !!member &&
          typeof member.uid === "number" &&
          Number.isFinite(member.uid) &&
          typeof member.name === "string"
      );

      setGroupMembersMap((prevMap) => ({ ...prevMap, [gid]: normalizedMembers }));
      setGroups((prevGroups) =>
        prevGroups.map((group) =>
          group.gid === gid
            ? {
                ...group,
                members: normalizedMembers.map((member) => ({
                  id: member.uid,
                  name: member.name
                }))
              }
            : group
        )
      );
    } catch (loadError) {
      console.error("Error al cargar los miembros del grupo:", loadError);
      setError("No se pudieron cargar los miembros del grupo.");
    }
  };

  useEffect(() => {
    if (activeGroup === -1) {
      return;
    }

    void loadGroupMembers(activeGroup);
  }, [activeGroup]);
  

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
  const filteredGroupTasks = sortTasksByCompleted(filterTasksByCompleted((activeGroupData?.tasks ?? []).filter((task) => isTaskVisibleInDefaultList(task)), groupTaskFilters).filter((t) => t.idObjetivo === undefined));
  const availableObjectives = (activeGroupData?.tasks ?? []).filter((task) => task.tipo === "OBJETIVO");

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

    const normalizedData = normalizeTaskFormData(data);
    const createdTaskId = await createTask(buildCreateTaskRequest(activeGroup, normalizedData));

    updateActiveGroupTasks((groupTasks) => [
      ...groupTasks,
      createLocalTask(normalizedData, {
        gid: activeGroup,
        id: createdTaskId,
        idObjetivo: normalizedData.idObjetivo
      })
    ]);
  };


  const editTaskWithSubtasks = async (currentTask: ITarea, data: ITaskFormData) => {
    const normalizedData = normalizeTaskFormData(data, currentTask.idObjetivo);
    await editTask(currentTask.id, buildEditTaskRequest(normalizedData));

    const nextTasks = (activeGroupData?.tasks ?? []).map((task) =>
      task.id === currentTask.id
        ? {
            ...task,
            titulo: normalizedData.name.trim(),
            descripcion: normalizedData.description.trim(),
            prioridad: normalizedData.difficulty,
            recompensaXp: getTaskXpReward(normalizedData.difficulty)
          }
        : task
    );

    updateActiveGroupTasks(() => nextTasks);
  };

  const handleModalSubmit = async (data: ITaskFormData) => {
    const normalizedData = normalizeTaskFormData(data, initialDataModal?.idObjetivo);

    try {
      setError(null);

      if (!initialDataModal) {
        await createTaskWithSubtasks(normalizedData);
        closeTaskModalHandle();
        return;
      }

      
      await editTaskWithSubtasks(initialDataModal, normalizedData);
      

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

        return {
          ...group,
          name: settings.name,
          description: settings.description,
          photoUrl: newPhotoUrl ?? group.photoUrl ?? null
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
    const groupTasks = activeGroupData?.tasks ?? [];
    const task = groupTasks.find((t) => t.id === taskId);
    if (!task) return;

    const subtasks = getTaskSubtasks(groupTasks, taskId);
    const willComplete = !isTaskCompleted(task);
    const wasCompletedBefore = Boolean(task.fecha_completado);

    try {
        if (willComplete) {
            await Promise.all(
                subtasks
                    .filter((s) => !isTaskCompleted(s))
                    .map((s) => completeTask(s.id))
            );
            await completeTask(taskId);
        } else {
            await uncompleteTask(taskId);
            await Promise.all(
                subtasks
                    .filter((s) => isTaskCompleted(s))
                    .map((s) => uncompleteTask(s.id))
            );
        }
    } catch (err) {
        console.error("Error al completar/descompletar tarea del grupo:", err);
    } finally {
        updateActiveGroupTasks((groupTasks) => toggleTaskCompleted(groupTasks, `${taskId}`));
        if (willComplete && !wasCompletedBefore) showRewardNotification(task.recompensaXp ?? 0, task.recompensaLudion ?? 0);
    }
  };

  const handleToggleSubtaskCompleted = async (taskId: number, subtaskId: number) => {
    const subtask = activeGroupData?.tasks.find((task) => task.id === subtaskId);
    const parentTask = activeGroupData?.tasks.find((task) => task.id === taskId);
    if (!subtask) return;
    const wasCompletedBefore = Boolean(subtask.fecha_completado);

    try {
      if (isTaskCompleted(subtask)) {
        await uncompleteTask(subtaskId);
        if (parentTask && isTaskCompleted(parentTask)) {
          await uncompleteTask(taskId);
        }
      } else {
        await completeTask(subtaskId);
        const siblingSubtasks = getTaskSubtasks(activeGroupData?.tasks ?? [], taskId).filter((task) => task.id !== subtaskId);

        if (parentTask && !isTaskCompleted(parentTask) && siblingSubtasks.every((task) => isTaskCompleted(task))) {
          await completeTask(taskId);
        }
      }
    } catch (completeError) {
      console.error("Error al completar la subtarea del grupo:", completeError);
    } finally {
      updateActiveGroupTasks((groupTasks) => toggleSubtaskCompleted(groupTasks, `${taskId}`, `${subtaskId}`));
      if (subtask && !isTaskCompleted(subtask) && !wasCompletedBefore) {
        showRewardNotification(subtask.recompensaXp ?? 0, subtask.recompensaLudion ?? 0);
      }
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

  const leaveGroupHandler = async (gid: number) => {
    try {
      await userLeaveGroup(gid);
      setGroups((prevGroups) => prevGroups.filter((group) => group.gid !== gid));
      setLoadedGroupIds((prevIds) => prevIds.filter((loadedGid) => loadedGid !== gid));
      setGroupMembersMap((prevMap) => {
        const nextMap = { ...prevMap };
        delete nextMap[gid];
        return nextMap;
      });
      if (activeGroup === gid) {
        setActiveGroup(-1);
        setIsOpen(false);
      }
      setIsSettingsModalOpen(false);
    } catch (saveError) {
      console.error("Error al guardar la configuracion del grupo:", saveError);
      setError("No se pudieron guardar los cambios del grupo.");
    }
  }

  const addMemberByUidHandler = async (gid: number, uid: number) => {
    try {
      await addUserToGroup({ gid, userId: uid });
      await loadGroupMembers(gid);
    } catch (saveError) {
      console.error("Error al agregar miembro al grupo:", saveError);
      setError("No se pudo agregar el miembro.");
      throw saveError;
    }
  };

  const removeMemberHandler = async (gid: number, uid: number) => {
    try {
      await removeUserFromGroup({ gid, userId: uid });
      await loadGroupMembers(gid);
    } catch (saveError) {
      console.error("Error al eliminar miembro del grupo:", saveError);
      setError("No se pudo eliminar el miembro.");
      throw saveError;
    }
  };

  const generateInviteLinkHandler = async (gid: number): Promise<string> => {
    try {
      const inviteResponse = await groupInvitacion({ gid });
      const linkFromInviteResponse =
        typeof inviteResponse === "string"
          ? inviteResponse
          : typeof inviteResponse?.inviteUrl === "string"
            ? inviteResponse.inviteUrl
            : "";

      if (linkFromInviteResponse) {
        return linkFromInviteResponse;
      }

      const fallbackLink = await groupInviteLink(gid);
      if (typeof fallbackLink === "string" && fallbackLink.trim()) {
        return fallbackLink;
      }

      throw new Error("No se recibio enlace de invitacion");
    } catch (saveError) {
      console.error("Error al generar invitacion del grupo:", saveError);
      setError("No se pudo generar la invitacion.");
      throw saveError;
    }
  };

  const joinByCodeHandler = async (code: string) => {
    try {
      await groupJoinByCode(code);
      const userGroups = await getUserGroup();
      const baseGroups = toRenderableGroups(userGroups);
      const groupsWithMembers = await preloadMembersForGroups(baseGroups);
      setGroups(groupsWithMembers);
    } catch (saveError) {
      console.error("Error al unirse al grupo por codigo:", saveError);
      setError("No se pudo unir al grupo por codigo.");
      throw saveError;
    }
  };

  const joinByLinkHandler = async (inviteLink: string) => {
    try {
      await groupJoinByLink(inviteLink);
      const userGroups = await getUserGroup();
      const baseGroups = toRenderableGroups(userGroups);
      const groupsWithMembers = await preloadMembersForGroups(baseGroups);
      setGroups(groupsWithMembers);
    } catch (saveError) {
      console.error("Error al unirse al grupo por enlace:", saveError);
      setError("No se pudo unir al grupo por enlace.");
      throw saveError;
    }
  };

  const handleJoinGroupFromModal = async () => {
    const value = joinGroupInput.trim();
    if (!value) {
      setJoinGroupError("Introduce un codigo o enlace.");
      return;
    }

    try {
      setJoinGroupLoading(true);
      setJoinGroupError(null);

      const isLink = value.startsWith("http://") || value.startsWith("https://") || value.includes("/");
      if (isLink) {
        await joinByLinkHandler(value);
      } else {
        await joinByCodeHandler(value);
      }

      window.location.reload();
    } catch {
      setJoinGroupError("No se pudo unir al grupo.");
    } finally {
      setJoinGroupLoading(false);
    }
  };

  const setMemberRoleHandler = async (gid: number, uid: number, role: number) => {
    try {
      await setMemberRole({ gid, userId: uid, role });
      await loadGroupMembers(gid);
    } catch (saveError) {
      console.error("Error al cambiar rol del miembro (gid/uid/role):", gid, uid, role, saveError);
      setError("No se pudo cambiar el rol del miembro.");
      throw saveError;
    }
  };

  const passOwnershipHandler = async (gid: number, newOwnerUserId: number) => {
    try {
      await passOwnershipGroup({ gid, newOwnerUserId });
      await loadGroupMembers(gid);
      setGroups((prevGroups) =>
        prevGroups.map((group) =>
          group.gid === gid
            ? {
                ...group,
                role: currentUserId === newOwnerUserId ? 2 : 1
              }
            : group
        )
      );
    } catch (saveError) {
      console.error("Error al ceder ownership del grupo:", saveError);
      setError("No se pudo ceder el ownership del grupo.");
      throw saveError;
    }
  };




  
  return (
    <div style={{ backgroundImage: `url(${bgImage})` }} className="relative flex min-h-screen flex-col gap-4 bg-cover bg-center font-['Space_Grotesk'] text-white">
      <Navbar />
      {rewardNotification ? (
        <div className="fixed bottom-4 right-4 z-60">
          <NotificationModal xp={rewardNotification.xp} ludiones={rewardNotification.ludiones} />
        </div>
      ) : null}

      <div className="grid grid-cols-1 gap-2 px-5 md:flex md:flex-row md:justify-center">
        <div className="flex w-full flex-col gap-2 md:w-1/3">
          <button onClick={() => setIsCreateModalOpen(true)} className="w-full rounded-md border border-[#F4E9E9]/15 bg-accent-beige-300/25 px-4 py-2 backdrop-blur-sm disabled:cursor-not-allowed disabled:opacity-60">
            <span className="text-2xl font-bold">Crear grupo</span>
          </button>
          <button onClick={() => setIsJoinModalOpen(true)} className="w-full rounded-md border border-[#F4E9E9]/15 bg-accent-beige-300/25 px-4 py-2 backdrop-blur-sm disabled:cursor-not-allowed disabled:opacity-60">
            <span className="text-2xl font-bold">Unir al grupo</span>
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
            <button disabled={!activeGroupData} onClick={() => {setInitialDataModal(null); setIsOpenModal(true);}}
              className="ml-auto rounded-md border border-[#F4E9E9]/15 bg-accent-beige-300/25 px-4 py-2 backdrop-blur-sm disabled:cursor-not-allowed disabled:opacity-60">
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
          tareasObjetivos={availableObjectives}
        />
      </div>

      {activeGroupData && (
        <GroupSettingsModal
          isOpen={isSettingsModalOpen}
          onClose={() => setIsSettingsModalOpen(false)}
          initialData={activeGroupData}
          onSave={handleSaveSettings}
          onDelete={({ gid, role }) => setGroupToDelete({ gid, role })}
          onLeave={(gid) => leaveGroupHandler(gid)}
          members={groupMembersMap[activeGroupData.gid] ?? []}
          onAddMemberByUid={addMemberByUidHandler}
          onRemoveMember={removeMemberHandler}
          onGenerateInviteLink={generateInviteLinkHandler}
          onSetMemberRole={setMemberRoleHandler}
          onPassOwnership={passOwnershipHandler}
        />
      )}

      <CreateGroupModal isOpen={isCreateModalOpen} onClose={() => setIsCreateModalOpen(false)} onSave={handleCreateGroup} />

      {isJoinModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 font-['Space_Grotesk']">
          <div className="bg-[linear-gradient(160deg,#0a101ff2,#3c1480d9,#142f42e6)] rounded-lg shadow-xl w-full max-w-xl p-6">
            <h2 className="text-2xl font-bold text-white mb-3">Unir al grupo</h2>
            <p className="text-sm text-gray-300 mb-3">Introduce un codigo o un enlace de invitacion.</p>
            <input
              type="text"
              value={joinGroupInput}
              onChange={(e) => setJoinGroupInput(e.target.value)}
              placeholder="Codigo o enlace"
              className="w-full bg-gray-800 border border-gray-700 rounded-md px-4 py-2 text-white focus:outline-none focus:ring-2 focus:ring-accent-beige-300"
            />
            {joinGroupError && <p className="mt-2 text-sm text-red-300">{joinGroupError}</p>}
            <div className="mt-4 flex justify-end gap-3">
              <button
                onClick={() => {
                  setIsJoinModalOpen(false);
                  setJoinGroupInput("");
                  setJoinGroupError(null);
                }}
                className="px-6 py-2 border border-gray-600 rounded-md text-white hover:bg-gray-700 transition-colors"
              >
                Cancelar
              </button>
              <button
                onClick={handleJoinGroupFromModal}
                disabled={joinGroupLoading}
                className="px-6 py-2 bg-accent-beige-300 text-black rounded-md font-medium hover:bg-accent-beige-400 transition-colors disabled:opacity-60"
              >
                Aceptar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
