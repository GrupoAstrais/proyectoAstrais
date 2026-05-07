import { useEffect, useState } from "react";
import { NavLink } from "react-router";
import Navbar from "../../components/layout/Navbar";
import bgImage from "../../assets/homeScreenBack.jpg";
import astra from "../../assets/astra.png";
import game from "../../assets/game.png";
import type { ITarea } from "../../types/Interfaces";
import Task from "../../components/ui/Task";
import Achiv from "../../components/ui/Achiv";
import Modal from "../../components/modales/TaskModal";
import Pet from "../../components/ui/Pet";
import NotificationModal from "../../components/modales/NotificationModal";
import { buildAchievements } from "../Achiv/achievementCatalog";
import { readArcadeStats } from "../Games/gameStorage";
import {
  buildCreateTaskRequest,
  buildEditTaskRequest,
  completeTask,
  createLocalTask,
  createTask,
  deleteTask,
  editTask,
  getDailyTasks,
  getHabitTasks,
  getTaskSubtasks,
  getTaskXpReward,
  getTasksFromGroup,
  getUserData,
  isTaskCompleted,
  isTaskVisibleInDefaultList,
  removeTaskWithSubtasks,
  toggleSubtaskCompleted,
  toggleTaskCompleted,
  uncompleteTask,
  type ITaskFormData
} from "../../data/Api";

const normalizeObjectiveId = (idObjetivo?: number | null): number | undefined => {
  return typeof idObjetivo === "number" && idObjetivo >= 0 ? idObjetivo : undefined;
};

const normalizeTaskFormData = (data: ITaskFormData, fallbackObjetivoId?: number | null): ITaskFormData => ({
  ...data,
  idObjetivo: normalizeObjectiveId(data.idObjetivo) ?? normalizeObjectiveId(fallbackObjetivoId)
});

const buildHomeAchievements = () =>
  buildAchievements(readArcadeStats(), [])
    .sort((leftAchievement, rightAchievement) => Number(rightAchievement.unlocked) - Number(leftAchievement.unlocked) || rightAchievement.percent - leftAchievement.percent)
    .slice(0, 8);

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

export default function Home() {
  const [notif] = useState<number>(0);
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [tasks, setTasks] = useState<ITarea[]>([]);
  const [homeAchievements] = useState(buildHomeAchievements);
  const [personalGroupId, setPersonalGroupId] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [initialDataModal, setInitialDataModal] = useState<ITarea | null>(null);
  const [rewardNotification, setRewardNotification] = useState<{ xp: number; ludiones: number } | null>(null);
  const showRewardNotification = (xp: number, ludiones: number) => {
    setRewardNotification(null);
    window.setTimeout(() => setRewardNotification({ xp, ludiones }), 0);
  };

  useEffect(() => {
    const loadTasks = async () => {
      try {
        setLoading(true);
        setError(null);

        const userData = await getUserData();
        setPersonalGroupId(userData.personalGid);

        const serverTasks = await getTasksFromGroup(userData.personalGid);
        console.log("use effect funciona confirmo");
        setTasks(serverTasks);
      } catch (loadError) {
        console.error("Error al cargar las tareas del dashboard:", loadError);
        setError("No se pudieron cargar las tareas del dashboard.");
      } finally {
        setLoading(false);
      }
    };

    void loadTasks();
  }, []);

  useEffect(() => {
    if (!rewardNotification) return;
    const timeoutId = window.setTimeout(() => setRewardNotification(null), 2200);
    return () => window.clearTimeout(timeoutId);
  }, [rewardNotification]);

  const closeModalHandle = () => {
    setInitialDataModal(null);
    setIsOpen(false);
  };

  const createTaskWithSubtasks = async (data: ITaskFormData) => {
    if (personalGroupId === null) {
      return;
    }

    const normalizedData = normalizeTaskFormData(data);
    const createdTaskId = await createTask(buildCreateTaskRequest(personalGroupId, normalizedData));

    setTasks((prevTasks) => [
      ...prevTasks,
      createLocalTask(normalizedData, {
        gid: personalGroupId,
        id: createdTaskId,
        idObjetivo: normalizedData.idObjetivo
      })
    ]);
  };


  const editTaskWithSubtasks = async (currentTask: ITarea, data: ITaskFormData) => {
    const normalizedData = normalizeTaskFormData(data, currentTask.idObjetivo);
    await editTask(currentTask.id, buildEditTaskRequest(normalizedData));

    setTasks((prevTasks) =>
      prevTasks.map((task) =>
      task.id === currentTask.id
        ? {
            ...task,
            titulo: normalizedData.name.trim(),
            descripcion: normalizedData.description.trim(),
            prioridad: normalizedData.difficulty,
            recompensaXp: getTaskXpReward(normalizedData.difficulty)
          }
        : task
      )
    );
  };

  const handleModalSubmit = async (data: ITaskFormData) => {
    const normalizedData = normalizeTaskFormData(data, initialDataModal?.idObjetivo);

    if (data.taskType === "OBJETIVO" && typeof normalizedData.idObjetivo !== "number") {
      setError("Selecciona un objetivo.");
      return;
    }

    try {
      setError(null);

      if (!initialDataModal) {
        await createTaskWithSubtasks(normalizedData);
        closeModalHandle();
        return;
      }


      await editTaskWithSubtasks(initialDataModal, normalizedData);

      closeModalHandle();
    } catch (submitError) {
      console.error("Error al guardar la tarea del dashboard:", submitError);
      setError("No se pudieron guardar los cambios de la tarea.");
    }
  };

  const handleDeleteTask = async () => {
    if (!initialDataModal) {
      return;
    }

    try {
      const currentSubtasks = getTaskSubtasks(tasks, initialDataModal.id);

      for (const subtask of currentSubtasks) {
        await deleteTask(subtask.id);
      }

      await deleteTask(initialDataModal.id);
      setTasks((prevTasks) => removeTaskWithSubtasks(prevTasks, initialDataModal.id));
      closeModalHandle();
    } catch (deleteError) {
      console.error("Error al borrar la tarea del dashboard:", deleteError);
      setError("No se pudo borrar la tarea.");
    }
  };

  const handleToggleTaskCompleted = async (taskId: number) => {
    const task = tasks.find((t) => t.id === taskId);
    if (!task) return;

    const subtasks = getTaskSubtasks(tasks, taskId);
    const willComplete = !isTaskCompleted(task);
    const wasCompletedBefore = wasTaskRewardedBefore(task);

    try {
        if (willComplete) {
            // Completar hijas primero, luego padre
            await Promise.all(
                subtasks
                    .filter((s) => !isTaskCompleted(s))
                    .map((s) => completeTask(s.id))
            );
            await completeTask(taskId);
        } else {
            // Descompletar padre primero, luego hijas
            await uncompleteTask(taskId);
            await Promise.all(
                subtasks
                    .filter((s) => isTaskCompleted(s))
                    .map((s) => uncompleteTask(s.id))
            );
        }
    } catch (err) {
        console.error("Error al completar/descompletar tarea:", err);
    } finally {
        setTasks((prev) => toggleTaskCompleted(prev, `${taskId}`));
        if (willComplete && !wasCompletedBefore) {
          markTaskAsRewarded(task);
          showRewardNotification(task.recompensaXp ?? 0, task.recompensaLudion ?? 0);
        }
    }
  };

  const handleToggleSubtaskCompleted = async (taskId: number, subtaskId: number) => {
    const subtask = tasks.find((currentTask) => currentTask.id === subtaskId);
    const parentTask = tasks.find((currentTask) => currentTask.id === taskId);
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
        const siblingSubtasks = getTaskSubtasks(tasks, taskId).filter((task) => task.id !== subtaskId);

        if (parentTask && !isTaskCompleted(parentTask) && siblingSubtasks.every(isTaskCompleted)) {
          await completeTask(taskId);
        }
      }
    } catch (completeError) {
      console.error("Error al completar la subtarea:", completeError);
    } finally {
      setTasks((prevTasks) => toggleSubtaskCompleted(prevTasks, `${taskId}`, `${subtaskId}`));
      if (subtask && !isTaskCompleted(subtask) && !wasCompletedBefore) {
        markTaskAsRewarded(subtask);
        showRewardNotification(subtask.recompensaXp ?? 0, subtask.recompensaLudion ?? 0);
      }
    }
  };

  const dashboardTasks = [...getDailyTasks(tasks), ...getHabitTasks(tasks)].filter((task) => isTaskVisibleInDefaultList(task) && !isTaskCompleted(task) && task.idObjetivo == null);
  const availableObjectives = tasks.filter((task) => task.id !== initialDataModal?.id);

  const editTaskHandle = (taskId: number) => {
    const taskToEdit = tasks.find((task) => task.id === taskId);

    if (!taskToEdit) {
      return;
    }

    setInitialDataModal(taskToEdit);
    setIsOpen(true);
  };

  return (
    <main
      style={{ backgroundImage: `url(${bgImage})` }}
      className="overflow-hidden h-screen w-screen bg-cover bg-center font-['Space_Grotesk'] text-white"
    >
      <div className={`${isOpen ? "" : "hidden"} fixed inset-0 z-50 flex items-center justify-center`}>
        <Modal
          onSubmit={handleModalSubmit}
          onCancel={closeModalHandle}
          onDelete={initialDataModal ? handleDeleteTask : null}
          initialData={initialDataModal}
          tareasObjetivos={availableObjectives}
        />
      </div>

      <Navbar />
      {rewardNotification ? (
        <div className="fixed bottom-4 right-4 z-60">
          <NotificationModal xp={rewardNotification.xp} ludiones={rewardNotification.ludiones} />
        </div>
      ) : null}

      
      <section className="mx-auto overflow-hidden flex-1 flex max-w-7xl flex-col items-center justify-center gap-4 px-4">
        <article className="relative flex w-full max-w-2xl mt-5 flex-col gap-6 rounded-2xl border border-white/15 bg-(--astrais-panel-bg) p-6 shadow-[0_15px_32px_color-mix(in_srgb,var(--astrais-background)_45%,transparent)]">
          <header>
            <p className="pb-2 text-[0.78rem] uppercase tracking-[0.08em] text-(--astrais-rarity-epic)">Bienvenido de vuelta</p>
            <h1 className="font-['Press_Start_2P'] text-xl sm:text-2xl">Hi, Astrais</h1>
            <p className="mt-1">Que te queda por hacer?</p>
          </header>
          <div className="grid w-2/3 grid-cols-1 z-20 gap-2.5 sm:grid-cols-2">
            <button
              onClick={() => {
                setInitialDataModal(null);
                setIsOpen(true);
              }}
              className="cursor-pointer rounded-xl border-0 [background:var(--astrais-cta-bg)] px-3 py-2 text-white transition-colors duration-200"
            >
              Crear tarea
            </button>
            <NavLink className="cursor-pointer rounded-xl border border-white/15 bg-white/10 px-3 py-2 text-center text-white backdrop-blur-sm transition-colors duration-200 hover:bg-white/20" to="/groups?openCreateModal=true">
              <button>Crear un grupo</button>
            </NavLink>
            <NavLink className="cursor-pointer rounded-xl border border-white/15 bg-white/10 px-3 py-2 text-center text-white backdrop-blur-sm transition-colors duration-200 hover:bg-white/20" to="/profile">
              <button>Ver perfil</button>
            </NavLink>
            <NavLink className="cursor-pointer rounded-xl border border-white/15 bg-white/10 px-3 py-2 text-center text-white backdrop-blur-sm transition-colors duration-200 hover:bg-white/20" to="/shop">
              <button>Cambiar la mascota</button>
            </NavLink>
          </div>
          <img className="absolute -bottom-7 -right-56 z-0 w-9/10" src={astra} alt="Astra" />
        </article>

        <div className="grid w-full home-scroll min-h-0 max-[1537px]:max-h-80 overflow-y-auto grid-cols-1 gap-4 lg:grid-cols-3"> 
          {/*Tareas Pendientes*/}
          <article className="flex h-80 lg:h-full min-h-0 flex-col rounded-2xl border border-white/15 bg-(--astrais-panel-bg) p-4 shadow-[0_15px_32px_color-mix(in_srgb,var(--astrais-background)_45%,transparent)]">
            <header className="mb-3">
              <NavLink to="/tasks">
                <h2 className="font-['Press_Start_2P'] text-lg">Tareas Pendientes</h2>
              </NavLink>
            </header>
            {error && <p className="pb-3 text-sm text-red-200">{error}</p>}
            <section className="home-scroll min-h-0 max-h-114 overflow-y-auto pr-1">
              <div className="flex flex-col gap-3">
                {loading ? (
                  <p className="py-4 text-center italic text-gray-300">Cargando tareas...</p>
                ) : dashboardTasks.length === 0 ? (
                  <p className="py-4 text-center italic text-gray-400">No hay tareas</p>
                ) : (
                  dashboardTasks.map((task) => (
                    <Task
                      key={task.id}
                      data={task}
                      subtasks={getTaskSubtasks(tasks, task.id)}
                      onComplete={handleToggleTaskCompleted}
                      onToggleSubtask={handleToggleSubtaskCompleted}
                      onToggleConfig={editTaskHandle}
                    />
                  ))
                )}
              </div>
            </section>
          </article>

          <div className="flex flex-col gap-4 lg:col-span-2">            
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              {/*Tienda*/}
              <NavLink to="/shop">
                <article className="h-80 rounded-2xl border border-white/15 bg-(--astrais-panel-bg)x p-4 shadow-[0_15px_32px_color-mix(in_srgb,var(--astrais-background)_45%,transparent)]">
                  <header className="mb-3">
                    <h2 className="font-['Press_Start_2P'] text-lg">Tienda</h2>
                  </header>
                  <button className="flex w-full flex-row justify-center">
                    <Pet url={astra} />
                  </button>
                </article>
              </NavLink>

              <div className="flex flex-col gap-4">
                <article className="rounded-2xl h-20 border border-white/15 bg-(--astrais-panel-bg) p-4 shadow-[0_15px_32px_color-mix(in_srgb,var(--astrais-background)_45%,transparent)]">
                  <header className="mb-3">
                    <button className="flex items-center gap-2">
                      <h2 className="font-['Press_Start_2P'] text-lg">Notificaciones</h2>
                      <span className="rounded-full bg-state-error px-2 py-1 text-xs text-white">{notif}</span>
                    </button>
                  </header>
                </article>

                {/*Logros*/}
                <NavLink to="/achievements">
                  <article className="flex h-56 flex-col gap-4 rounded-2xl border border-white/15 bg-(--astrais-panel-bg) p-4 shadow-[0_15px_32px_color-mix(in_srgb,var(--astrais-background)_45%,transparent)]">
                    <header className="mb-3">
                      <h2 className="font-['Press_Start_2P'] text-lg">Logros</h2>
                    </header>
                    <section className="home-scroll min-h-0 max-h-44 overflow-y-auto pr-1">
                      <div className="flex flex-col gap-3">
                        {homeAchievements.map((achievement) => (
                          <div
                            key={achievement.id}
                            className={`flex items-center justify-between gap-3 rounded-2xl border p-3 transition ${
                              achievement.unlocked
                                ? "border-accent-mint-300/22 bg-[color-mix(in_srgb,var(--astrais-background)_80%,transparent)]"
                                : "border-white/10 bg-[color-mix(in_srgb,var(--astrais-background)_74%,transparent)]"
                            }`}
                          >
                            <div className="flex min-w-0 items-center gap-3">
                              <Achiv />
                              <div className="min-w-0">
                                <p className="text-[0.58rem] uppercase tracking-[0.18em] text-slate-400">{achievement.category}</p>
                                <h3 className="mt-1 truncate text-[0.78rem] font-semibold text-white">{achievement.title}</h3>
                              </div>
                            </div>
                            <span className="shrink-0 rounded-full border border-white/15 bg-white/8 px-2 py-1 text-[0.58rem] font-semibold text-(--astrais-reward)">
                              {achievement.percent}%
                            </span>
                          </div>
                        ))}
                      </div>
                    </section>
                  </article>
                </NavLink>
              </div>
            </div>

            <article className="rounded-2xl border border-white/15 bg-(--astrais-panel-bg) p-4 shadow-[0_15px_32px_color-mix(in_srgb,var(--astrais-background)_45%,transparent)]">
              <header className="mb-3">
                <h2 className="font-['Press_Start_2P'] text-lg">Minijuegos</h2>
              </header>
              <div className="flex flex-col items-center gap-4">
                <img src={game} className="h-auto w-1/2 max-w-30 rounded-lg" alt="Juego" />
                <NavLink to="/games">
                  <button className="w-full max-w-xs cursor-pointer rounded-xl border border-white/25 bg-white/10 px-3 py-2 text-white transition-colors duration-200 hover:bg-white/20">
                    Jugar ahora
                  </button>
                </NavLink>
              </div>
            </article>
          </div>
        </div>
      </section>
      
      <style>{`
        .home-scroll {
          scrollbar-width: none;
          -ms-overflow-style: none;
          overscroll-behavior: contain;
        }

        .home-scroll::-webkit-scrollbar {
          display: none;
        }
      `}</style>
    </main>
  );
}
