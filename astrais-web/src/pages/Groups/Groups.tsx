import React, { useEffect, useState } from "react";
import { useSearchParams } from "react-router";
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
  groupInvitacionLista,
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
  revokeGroupInvit,
  setMemberRole,
  sortTasksByCompleted,
  toggleSubtaskCompleted,
  toggleTaskCompleted,
  uncompleteTask,
  type ITaskFormData,
  userLeaveGroup,
  membersGroups,
  eventosGroup,
} from "../../data/Api";
import type {
  EventosGrupos,
  GroupInvitacionRespuesta,
  MembersResponse,
} from "../../types/LoginRequest";

const compareGroupsAlphabetically = (
  firstGroup: IGroup,
  secondGroup: IGroup,
): number => {
  return firstGroup.name.localeCompare(secondGroup.name, "es", {
    sensitivity: "base",
  });
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
    role: group.role,
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

const normalizeObjectiveId = (
  idObjetivo?: number | null,
): number | undefined => {
  return typeof idObjetivo === "number" && idObjetivo >= 0
    ? idObjetivo
    : undefined;
};

const normalizeTaskFormData = (
  data: ITaskFormData,
  fallbackObjetivoId?: number | null,
): ITaskFormData => ({
  ...data,
  idObjetivo:
    normalizeObjectiveId(data.idObjetivo) ??
    normalizeObjectiveId(fallbackObjetivoId),
});

const hasTaskBeenCompletedOnce = (task: ITarea): boolean => {
  return typeof task.fecha_completado === "string" && task.fecha_completado.trim().length > 0;
};

const getRewardedTaskStorageKey = (task: ITarea): string => `rewarded-task:${task.gid}:${task.id}`;

const wasTaskRewardedBefore = (task: ITarea): boolean => {
  if (hasTaskBeenCompletedOnce(task)) return true;
  return localStorage.getItem(getRewardedTaskStorageKey(task)) === "1";
};

const markTaskAsRewarded = (task: ITarea): void => {
  localStorage.setItem(getRewardedTaskStorageKey(task), "1");
};

const normalizeAuditEvents = (eventsLike: unknown): EventosGrupos[] => {
  const normalizeEvent = (eventLike: unknown): EventosGrupos | null => {
    if (!eventLike || typeof eventLike !== "object") {
      return null;
    }

    const event = eventLike as Record<string, unknown>;
    const eventType =
      typeof event.eventType === "string"
        ? event.eventType
        : typeof event.event_type === "string"
          ? event.event_type
          : null;
    const createdAt =
      typeof event.createdAt === "string"
        ? event.createdAt
        : typeof event.created_at === "string"
          ? event.created_at
          : null;

    if (!eventType || !createdAt) {
      return null;
    }

    const idValue =
      typeof event.id === "number"
        ? event.id
        : typeof event.id === "string"
          ? Number(event.id)
          : Date.now() + Math.random();
    const actorUidValue =
      typeof event.actorUid === "number"
        ? event.actorUid
        : typeof event.actor_uid === "number"
          ? event.actor_uid
          : -1;

    return {
      id: Number.isFinite(idValue) ? idValue : Date.now() + Math.random(),
      actorUid: Number.isFinite(actorUidValue) ? actorUidValue : -1,
      eventType,
      payloadJson:
        typeof event.payloadJson === "string"
          ? event.payloadJson
          : typeof event.payload_json === "string"
            ? event.payload_json
            : null,
      createdAt
    };
  };

  if (Array.isArray(eventsLike)) {
    return eventsLike
      .map(normalizeEvent)
      .filter((event): event is EventosGrupos => event !== null);
  }

  if (eventsLike && typeof eventsLike === "object") {
    const candidate = eventsLike as {
      events?: unknown;
      audit?: unknown;
      auditList?: unknown;
      eventList?: unknown;
      items?: unknown;
      data?: unknown;
    };

    const collection =
      candidate.events ?? candidate.audit ?? candidate.auditList ?? candidate.eventList ?? candidate.items ?? candidate.data;

    if (Array.isArray(collection)) {
      return collection
        .map(normalizeEvent)
        .filter((event): event is EventosGrupos => event !== null);
    }

    const singleEvent = normalizeEvent(eventsLike);
    return singleEvent ? [singleEvent] : [];
  }

  return [];
};

const getAuditEventLabel = (eventType: string): string => {
  switch (eventType) {
    case "invite_created":
      return "Se ha creado una invitacion.";
    case "invite_revoked":
      return "Se ha revocado una invitacion.";
    case "member_joined_by_invite":
      return "Un miembro se ha unido con una invitacion.";
    case "member_left":
      return "Un miembro ha abandonado el grupo.";
    case "member_role_changed":
      return "Se ha cambiado el rol de un miembro.";
    default:
      return eventType;
  }
};

export default function Groups() {
  const [isOpen, setIsOpen] = React.useState<boolean>(false);
  const [isOpenModal, setIsOpenModal] = React.useState<boolean>(false);
  const [activeGroup, setActiveGroup] = useState<number>(-1);
  const [isSettingsModalOpen, setIsSettingsModalOpen] =
    React.useState<boolean>(false);
  const [isCreateModalOpen, setIsCreateModalOpen] =
    React.useState<boolean>(false);
  const [isJoinModalOpen, setIsJoinModalOpen] = React.useState<boolean>(false);
  const [joinGroupInput, setJoinGroupInput] = React.useState<string>("");
  const [joinGroupLoading, setJoinGroupLoading] =
    React.useState<boolean>(false);
  const [joinGroupError, setJoinGroupError] = React.useState<string | null>(
    null,
  );
  const [initialDataModal, setInitialDataModal] = useState<ITarea | null>(null);
  const [groupTaskFilters, setGroupTaskFilters] = useState({
    completed: false,
    pending: false,
  });
  const [groupToDelete, setGroupToDelete] = useState<{
    gid: number;
    role: number;
  }>({ gid: -1, role: -1 });
  const [groups, setGroups] = useState<IGroup[]>([]);
  const [loadedGroupIds, setLoadedGroupIds] = useState<number[]>([]);
  const [loadingGroups, setLoadingGroups] = useState<boolean>(true);
  const [loadingTasks, setLoadingTasks] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [searchParams, setSearchParams] = useSearchParams();
  const [groupMembersMap, setGroupMembersMap] = useState<
    Record<number, MembersResponse[]>
  >({});
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);
  const [personalGroupId, setPersonalGroupId] = useState<number | null>(null);
  const [rewardNotification, setRewardNotification] = useState<{
    xp: number;
    ludiones: number;
  } | null>(null);
  const [isAuditModalOpen, setIsAuditModalOpen] = useState<boolean>(false);
  const [auditEvents, setAuditEvents] = useState<EventosGrupos[]>([]);
  const [loadingAudit, setLoadingAudit] = useState<boolean>(false);
  const showRewardNotification = (xp: number, ludiones: number) => {
    setRewardNotification(null);
    window.setTimeout(() => setRewardNotification({ xp, ludiones }), 0);
  };

  const toRenderableGroups = (
    userGroups: Array<{
      gid?: number;
      id?: number;
      name?: string;
      nombre?: string;
      description: string;
      role: number;
    }>,
  ): IGroup[] =>
    userGroups
      .filter((group) => (group.gid ?? group.id ?? -1) !== personalGroupId)
      .map(mapUserGroupToLocalGroup);

  const preloadMembersForGroups = async (
    baseGroups: IGroup[],
  ): Promise<IGroup[]> => {
    const entries = await Promise.all(
      baseGroups.map(async (group) => {
        try {
          const serverMembers = await membersGroups(group.gid);
          const normalizedMembers = (
            Array.isArray(serverMembers) ? serverMembers : []
          ).filter(
            (member): member is MembersResponse =>
              !!member &&
              typeof member.uid === "number" &&
              Number.isFinite(member.uid) &&
              typeof member.name === "string",
          );
          return [group.gid, normalizedMembers] as const;
        } catch {
          return [group.gid, []] as const;
        }
      }),
    );

    const mapFromServer: Record<number, MembersResponse[]> =
      Object.fromEntries(entries);
    setGroupMembersMap((prevMap) => ({ ...prevMap, ...mapFromServer }));

    return baseGroups.map((group) => ({
      ...group,
      members: (mapFromServer[group.gid] ?? []).map((member) => ({
        id: member.uid,
        name: member.name,
      })),
    }));
  };

  useEffect(() => {
    const loadGroups = async () => {
      try {
        setLoadingGroups(true);
        setError(null);

        const [userGroups, userData] = await Promise.all([
          getUserGroup(),
          getUserData(),
        ]);
        setCurrentUserId(userData.id);
        setPersonalGroupId(userData.personalGid);
        const baseGroups = userGroups
          .filter(
            (group) => (group.gid ?? group.id ?? -1) !== userData.personalGid,
          )
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
    const timeoutId = window.setTimeout(
      () => setRewardNotification(null),
      2200,
    );
    return () => window.clearTimeout(timeoutId);
  }, [rewardNotification]);

  useEffect(() => {
    if (groupToDelete.gid === -1) {
      return;
    }

    const deleteSelectedGroup = async () => {
      try {
        await deleteGroup(groupToDelete.gid, groupToDelete.role);

        setGroups((prevGroups) =>
          prevGroups.filter((group) => group.gid !== groupToDelete.gid),
        );
        setLoadedGroupIds((prevIds) =>
          prevIds.filter((gid) => gid !== groupToDelete.gid),
        );

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
                  tasks: serverTasks ?? [],
                }
              : group,
          ),
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
      const normalizedMembers = (
        Array.isArray(serverMembers) ? serverMembers : []
      ).filter(
        (member): member is MembersResponse =>
          !!member &&
          typeof member.uid === "number" &&
          Number.isFinite(member.uid) &&
          typeof member.name === "string",
      );

      setGroupMembersMap((prevMap) => ({
        ...prevMap,
        [gid]: normalizedMembers,
      }));
      setGroups((prevGroups) =>
        prevGroups.map((group) =>
          group.gid === gid
            ? {
                ...group,
                members: normalizedMembers.map((member) => ({
                  id: member.uid,
                  name: member.name,
                })),
              }
            : group,
        ),
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
  const activeGroupRole = activeGroupData?.role ?? -1;
  const canManageGroup = activeGroupRole >= 1;
  const canManageTasks = activeGroupRole >= 1;
  const canViewAudit = activeGroupRole >= 0;
  const filteredGroupTasks = sortTasksByCompleted(filterTasksByCompleted((activeGroupData?.tasks ?? []).filter((task) => isTaskVisibleInDefaultList(task)), groupTaskFilters).filter((t) => t.idObjetivo == null));
  const availableObjectives = (activeGroupData?.tasks ?? []).filter((task) => task.tipo === "OBJETIVO");

  const updateActiveGroupTasks = (updater: (tasks: ITarea[]) => ITarea[]) => {
    setGroups((prevGroups) =>
      prevGroups.map((group) =>
        group.gid === activeGroup
          ? {
              ...group,
              tasks: updater(group.tasks),
            }
          : group,
      ),
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
    const createdTaskId = await createTask(
      buildCreateTaskRequest(activeGroup, normalizedData),
    );

    updateActiveGroupTasks((groupTasks) => [
      ...groupTasks,
      createLocalTask(normalizedData, {
        gid: activeGroup,
        id: createdTaskId,
        idObjetivo: normalizedData.idObjetivo,
      }),
    ]);
  };

  const editTaskWithSubtasks = async (
    currentTask: ITarea,
    data: ITaskFormData,
  ) => {
    const normalizedData = normalizeTaskFormData(data, currentTask.idObjetivo);
    await editTask(currentTask.id, buildEditTaskRequest(normalizedData));

    const nextTasks = (activeGroupData?.tasks ?? []).map((task) =>
      task.id === currentTask.id
        ? {
            ...task,
            titulo: normalizedData.name.trim(),
            descripcion: normalizedData.description.trim(),
            prioridad: normalizedData.difficulty,
            recompensaXp: getTaskXpReward(normalizedData.difficulty),
          }
        : task,
    );

    updateActiveGroupTasks(() => nextTasks);
  };

  const handleModalSubmit = async (data: ITaskFormData) => {
    if (!canManageTasks) {
      setError("No tienes permisos para crear o editar tareas en este grupo.");
      return;
    }

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
    if (!canManageTasks) {
      setError("No tienes permisos para borrar tareas en este grupo.");
      return;
    }

    if (!initialDataModal || !activeGroupData) {
      return;
    }

    try {
      const currentSubtasks = getTaskSubtasks(
        activeGroupData.tasks,
        initialDataModal.id,
      );

      for (const subtask of currentSubtasks) {
        await deleteTask(subtask.id);
      }

      await deleteTask(initialDataModal.id);
      updateActiveGroupTasks((groupTasks) =>
        removeTaskWithSubtasks(groupTasks, initialDataModal.id),
      );
      closeTaskModalHandle();
    } catch (deleteError) {
      console.error("Error al borrar la tarea del grupo:", deleteError);
      setError("No se pudo borrar la tarea.");
    }
  };

  const handleCreateGroup = async (data: {
    name: string;
    description: string;
    photo?: File | null;
  }) => {
    try {
      const createdGroupId = await createGroup({
        name: data.name,
        desc: data.description,
      });
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
      await editGroup({
        gid: settings.gid,
        name: settings.name,
        desc: settings.description,
      });
    } catch (saveError) {
      console.error("Error al guardar la configuracion del grupo:", saveError);
      setError("No se pudieron guardar los cambios del grupo.");
    }

    const newPhotoUrl = settings.photo
      ? URL.createObjectURL(settings.photo)
      : undefined;

    setGroups((prevGroups) =>
      prevGroups.map((group) => {
        if (group.gid !== activeGroup) {
          return group;
        }

        return {
          ...group,
          name: settings.name,
          description: settings.description,
          photoUrl: newPhotoUrl ?? group.photoUrl ?? null,
        };
      }),
    );

    setIsSettingsModalOpen(false);
  };

  const handleActiveTaskFilter = (active: string) => {
    if (active === "Completadas") {
      setGroupTaskFilters((prev) => ({
        ...prev,
        completed: !prev.completed,
      }));
      return;
    }

    setGroupTaskFilters((prev) => ({
      ...prev,
      pending: !prev.pending,
    }));
  };

  const handleToggleTaskCompleted = async (taskId: number) => {
    const groupTasks = activeGroupData?.tasks ?? [];
    const task = groupTasks.find((t) => t.id === taskId);
    if (!task) return;

    const subtasks = getTaskSubtasks(groupTasks, taskId);
    const willComplete = !isTaskCompleted(task);
    const wasCompletedBefore = wasTaskRewardedBefore(task);

    try {
      if (willComplete) {
        await Promise.all(
          subtasks
            .filter((s) => !isTaskCompleted(s))
            .map((s) => completeTask(s.id)),
        );
        await completeTask(taskId);
      } else {
        await uncompleteTask(taskId);
        await Promise.all(
          subtasks
            .filter((s) => isTaskCompleted(s))
            .map((s) => uncompleteTask(s.id)),
        );
      }
    } catch (err) {
      console.error("Error al completar/descompletar tarea del grupo:", err);
    } finally {
        updateActiveGroupTasks((groupTasks) => toggleTaskCompleted(groupTasks, `${taskId}`));
        if (willComplete && !wasCompletedBefore) {
          markTaskAsRewarded(task);
          showRewardNotification(task.recompensaXp ?? 0, task.recompensaLudion ?? 0);
        }
    }
  };

  const handleToggleSubtaskCompleted = async (
    taskId: number,
    subtaskId: number,
  ) => {
    const subtask = activeGroupData?.tasks.find(
      (task) => task.id === subtaskId,
    );
    const parentTask = activeGroupData?.tasks.find(
      (task) => task.id === taskId,
    );
    if (!subtask) return;
    const wasCompletedBefore = wasTaskRewardedBefore(subtask);

    try {
      if (isTaskCompleted(subtask)) {
        await uncompleteTask(subtaskId);
        if (parentTask && isTaskCompleted(parentTask)) {
          await uncompleteTask(taskId);
        }
      } else {
        await completeTask(subtaskId);
        const siblingSubtasks = getTaskSubtasks(
          activeGroupData?.tasks ?? [],
          taskId,
        ).filter((task) => task.id !== subtaskId);

        if (
          parentTask &&
          !isTaskCompleted(parentTask) &&
          siblingSubtasks.every((task) => isTaskCompleted(task))
        ) {
          await completeTask(taskId);
        }
      }
    } catch (completeError) {
      console.error("Error al completar la subtarea del grupo:", completeError);
    } finally {
      updateActiveGroupTasks((groupTasks) =>
        toggleSubtaskCompleted(groupTasks, `${taskId}`, `${subtaskId}`),
      );
      if (subtask && !isTaskCompleted(subtask) && !wasCompletedBefore) {
        markTaskAsRewarded(subtask);
        showRewardNotification(subtask.recompensaXp ?? 0, subtask.recompensaLudion ?? 0);
      }
    }
  };

  const editTaskHandle = (taskId: number) => {
    if (!canManageTasks) {
      setError("No tienes permisos para editar tareas en este grupo.");
      return;
    }

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
      setGroups((prevGroups) =>
        prevGroups.filter((group) => group.gid !== gid),
      );
      setLoadedGroupIds((prevIds) =>
        prevIds.filter((loadedGid) => loadedGid !== gid),
      );
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
  };

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

  const normalizeInvite = (inviteLike: unknown): GroupInvitacionRespuesta => {
    if (inviteLike && typeof inviteLike === "object") {
      const invite = inviteLike as Partial<GroupInvitacionRespuesta>;
      return {
        code: typeof invite.code === "string" ? invite.code : "",
        inviteUrl: typeof invite.inviteUrl === "string" ? invite.inviteUrl : "",
        expiresAt:
          typeof invite.expiresAt === "string" ? invite.expiresAt : null,
        maxUses: typeof invite.maxUses === "number" ? invite.maxUses : 0,
        usesCount: typeof invite.usesCount === "number" ? invite.usesCount : 0,
        revokedAt:
          typeof invite.revokedAt === "string" ? invite.revokedAt : null,
      };
    }

    if (typeof inviteLike === "string") {
      return {
        code: "",
        inviteUrl: inviteLike,
        expiresAt: null,
        maxUses: 0,
        usesCount: 0,
        revokedAt: null,
      };
    }

    return {
      code: "",
      inviteUrl: "",
      expiresAt: null,
      maxUses: 0,
      usesCount: 0,
      revokedAt: null,
    };
  };

  const generateInviteHandler = async (
    gid: number,
  ): Promise<GroupInvitacionRespuesta> => {
    try {
      const inviteResponse = await groupInvitacion({ gid });
      const normalizedFromCreate = normalizeInvite(inviteResponse);

      let inviteUrl = normalizedFromCreate.inviteUrl;
      if (!inviteUrl) {
        inviteUrl = await groupInviteLink(gid);
      }

      // Si el endpoint de crear devuelve solo inviteUrl, buscamos el code en el listado.
      const invites = await groupInvitacionLista(gid);
      const normalizedInvites = (Array.isArray(invites) ? invites : []).map(
        normalizeInvite,
      );
      const inviteFromList = normalizedInvites.find(
        (invite) => invite.inviteUrl === inviteUrl,
      );

      if (inviteFromList) {
        return inviteFromList;
      }

      return {
        code: normalizedFromCreate.code ?? "",
        inviteUrl,
        expiresAt: normalizedFromCreate.expiresAt ?? null,
        maxUses: normalizedFromCreate.maxUses ?? 10,
        usesCount: normalizedFromCreate.usesCount ?? 0,
        revokedAt: normalizedFromCreate.revokedAt ?? null,
      };
    } catch (saveError) {
      console.error("Error al generar invitacion del grupo:", saveError);
      setError("No se pudo generar la invitacion.");
      throw saveError;
    }
  };

  const loadInvitesHandler = async (
    gid: number,
  ): Promise<GroupInvitacionRespuesta[]> => {
    try {
      const list = await groupInvitacionLista(gid);
      return (Array.isArray(list) ? list : []).map(normalizeInvite);
    } catch (loadError) {
      console.error("Error al cargar invitaciones del grupo:", loadError);
      setError("No se pudieron cargar las invitaciones.");
      throw loadError;
    }
  };

  const revokeInviteHandler = async (
    gid: number,
    code: string,
  ): Promise<void> => {
    try {
      await revokeGroupInvit({ gid, code });
    } catch (saveError) {
      console.error("Error al revocar invitacion del grupo:", saveError);
      setError("No se pudo revocar la invitacion.");
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

      const isLink =
        value.startsWith("http://") ||
        value.startsWith("https://") ||
        value.includes("/");
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

  const setMemberRoleHandler = async (
    gid: number,
    uid: number,
    role: number,
  ) => {
    try {
      await setMemberRole({ gid, userId: uid, role });
      await loadGroupMembers(gid);
    } catch (saveError) {
      console.error(
        "Error al cambiar rol del miembro (gid/uid/role):",
        gid,
        uid,
        role,
        saveError,
      );
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
                role: currentUserId === newOwnerUserId ? 2 : 1,
              }
            : group,
        ),
      );
    } catch (saveError) {
      console.error("Error al ceder ownership del grupo:", saveError);
      setError("No se pudo ceder el ownership del grupo.");
      throw saveError;
    }
  };

  const openAuditModal = async () => {
    if (!activeGroupData) {
      return;
    }

    try {
      setLoadingAudit(true);
      const events = await eventosGroup(activeGroupData.gid);
      setAuditEvents(normalizeAuditEvents(events));
      setIsAuditModalOpen(true);
    } catch (loadError) {
      console.error("Error al cargar auditoria del grupo:", loadError);
      setError("No se pudo cargar el historial de auditoria.");
    } finally {
      setLoadingAudit(false);
    }
  };

  return (
    <div
      className="relative flex h-screen w-screen overflow-hidden flex-col gap-4 font-['Space_Grotesk'] text-white"
    >
      <Navbar />
      {rewardNotification ? (
        <div className="fixed bottom-4 right-4 z-60">
          <NotificationModal
            xp={rewardNotification.xp}
            ludiones={rewardNotification.ludiones}
          />
        </div>
      ) : null}

      <div className="grid grid-cols-1 gap-2 px-5 md:flex md:flex-row md:justify-center">
        <div className="flex w-full flex-col gap-2 md:w-1/3">
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="w-full rounded-md border border-white/15 bg-accent-beige-300/25 px-4 py-2 backdrop-blur-sm disabled:cursor-not-allowed disabled:opacity-60"
          >
            <span className="text-2xl font-bold">Crear grupo</span>
          </button>
          <button
            onClick={() => setIsJoinModalOpen(true)}
            className="w-full rounded-md border border-white/15 bg-accent-beige-300/25 px-4 py-2 backdrop-blur-sm disabled:cursor-not-allowed disabled:opacity-60"
          >
            <span className="text-2xl font-bold">Unir al grupo</span>
          </button>
          <div className="flex flex-col groups-scroll min-h-0 max-h-176 max-[1537px]:max-h-118 overflow-y-auto gap-2">
            {loadingGroups ? (
            <p className="py-4 text-center italic text-gray-300">
              Cargando grupos...
            </p>
          ) : (
            sortedGroups.map((group) => (
              <GroupCard
                key={`${group.gid}-${group.name}`}
                onClick={handleActiveGroup}
                id={group.gid}
                activeId={activeGroup}
                data={group}
              />
            ))
          )}
          </div>
          
        </div>

        <div className={`${isOpen ? "" : "hidden"} flex w-full flex-col gap-2 md:w-1/2`}>
          <div className="flex w-full flex-row justify-between">
            <button disabled={!activeGroupData || !canManageGroup} onClick={() => setIsSettingsModalOpen(true)} className="rounded-md border border-[#F4E9E9]/15 bg-accent-beige-300/25 px-4 py-2 backdrop-blur-sm disabled:cursor-not-allowed disabled:opacity-60">
              <span className="text-2xl font-bold">Configuracion</span>
            </button>
            <div className="flex flex-row gap-2">
              <button disabled={!activeGroupData || loadingAudit || !canViewAudit} onClick={() => { void openAuditModal(); }} className="ml-2 rounded-md border border-[#F4E9E9]/15 bg-accent-beige-300/25 px-4 py-2 backdrop-blur-sm disabled:cursor-not-allowed disabled:opacity-60">
                <span className="text-2xl font-bold">Historial</span>
              </button>
              <button disabled={!activeGroupData || !canManageTasks} onClick={() => {setInitialDataModal(null); setIsOpenModal(true);}}
                className="ml-auto rounded-md border border-[#F4E9E9]/15 bg-accent-beige-300/25 px-4 py-2 backdrop-blur-sm disabled:cursor-not-allowed disabled:opacity-60">
                  <span className="text-2xl font-bold">+ Añadir tarea</span>
              </button>
            </div>
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

              <ButtonComplete
                title="Completadas"
                active={groupTaskFilters.completed}
                handleActive={handleActiveTaskFilter}
              />
              <ButtonComplete
                title="Pendientes"
                active={groupTaskFilters.pending}
                handleActive={handleActiveTaskFilter}
              />
            </div>

            <div className="flex flex-col gap-2">
              {loadingTasks ? (
                <p className="py-4 text-center italic text-gray-300">
                  Cargando tareas...
                </p>
              ) : filteredGroupTasks.length === 0 ? (
                <p className="py-4 text-center italic text-gray-400">
                  No hay tareas
                </p>
              ) : (
                filteredGroupTasks.map((task) => (
                  <Task
                    key={task.id}
                    data={task}
                    subtasks={getTaskSubtasks(
                      activeGroupData?.tasks ?? [],
                      task.id,
                    )}
                    onComplete={handleToggleTaskCompleted}
                    onToggleSubtask={handleToggleSubtaskCompleted}
                    onToggleConfig={canManageTasks ? editTaskHandle : undefined}
                  />
                ))
              )}
            </div>
          </div>
        </div>
      </div>

      <div
        className={`${isOpenModal ? "" : "hidden"} fixed inset-0 z-50 flex items-center justify-center`}
      >
        <Modal
          onSubmit={handleModalSubmit}
          onCancel={closeTaskModalHandle}
          onDelete={initialDataModal ? handleDeleteTask : null}
          initialData={initialDataModal}
          tareasObjetivos={availableObjectives}
        />
      </div>

      {activeGroupData && canManageGroup && (
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
          onGenerateInviteLink={generateInviteHandler}
          onLoadInvites={loadInvitesHandler}
          onRevokeInvite={revokeInviteHandler}
          onSetMemberRole={setMemberRoleHandler}
          onPassOwnership={passOwnershipHandler}
        />
      )}

      {isAuditModalOpen ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 font-['Space_Grotesk']">
          <div className="bg-(--astrais-panel-bg) rounded-lg shadow-xl w-full max-w-3xl max-h-[85vh] overflow-hidden flex flex-col">
            <div className="flex items-center justify-between p-5 border-b border-gray-700">
              <h2 className="text-xl font-bold text-white">
                Historial de auditoria
              </h2>
              <button
                onClick={() => setIsAuditModalOpen(false)}
                className="text-gray-300 hover:text-white text-2xl"
              >
                &times;
              </button>
            </div>
            <div className="p-5 overflow-y-auto">
              {auditEvents.length === 0 ? (
                <p className="text-gray-300">No hay eventos para mostrar.</p>
              ) : (
                <div className="space-y-2">
                  {auditEvents.map((event) => (
                    <div key={event.id} className="rounded-md border border-gray-700 bg-gray-900/45 p-3">
                      <p className="text-sm text-white">{getAuditEventLabel(event.eventType)}</p>
                      <p className="text-xs text-gray-300">{new Date(event.createdAt).toLocaleString()}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
            <div className="border-t border-gray-700 p-4 flex justify-end">
              <button
                onClick={() => setIsAuditModalOpen(false)}
                className="px-6 py-2 border border-gray-600 rounded-md text-white hover:bg-gray-700 transition-colors"
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      ) : null}

      <CreateGroupModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSave={handleCreateGroup}
      />

      {isJoinModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 font-['Space_Grotesk']">
          <div className="bg-(--astrais-panel-bg) rounded-lg shadow-xl w-full max-w-xl p-6">
            <h2 className="text-2xl font-bold text-white mb-3">
              Unir al grupo
            </h2>
            <p className="text-sm text-gray-300 mb-3">
              Introduce un codigo o un enlace de invitacion.
            </p>
            <input
              type="text"
              value={joinGroupInput}
              onChange={(e) => setJoinGroupInput(e.target.value)}
              placeholder="Codigo o enlace"
              className="w-full bg-gray-800 border border-gray-700 rounded-md px-4 py-2 text-white focus:outline-none focus:ring-2 focus:ring-accent-beige-300"
            />
            {joinGroupError && (
              <p className="mt-2 text-sm text-red-300">{joinGroupError}</p>
            )}
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

    <style>{`
      .groups-scroll {
        scrollbar-width: none;
        -ms-overflow-style: none;
        overscroll-behavior: contain;
      }

      .groups-scroll::-webkit-scrollbar {
        display: none;
      }
    `}</style>
    </div>
  );
}

