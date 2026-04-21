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
  removeTaskWithSubtasks,
  shouldRecreateTaskOnEdit,
  toggleSubtaskCompleted,
  toggleTaskCompleted,
  type ITaskFormData,
  type ITaskFormSubtask
} from "../../data/Api";

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

export default function Home() {
  const [notif] = useState<number>(0);
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [tasks, setTasks] = useState<ITarea[]>([]);
  const [personalGroupId, setPersonalGroupId] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [initialDataModal, setInitialDataModal] = useState<ITarea | null>(null);

  useEffect(() => {
    const loadTasks = async () => {
      try {
        setLoading(true);
        setError(null);

        const userData = await getUserData();
        setPersonalGroupId(userData.personalGid);

        const serverTasks = await getTasksFromGroup(userData.personalGid);
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

  const closeModalHandle = () => {
    setInitialDataModal(null);
    setIsOpen(false);
  };

  const createTaskWithSubtasks = async (data: ITaskFormData) => {
    if (personalGroupId === null) {
      return;
    }

    const parentTaskId = await createTask(buildCreateTaskRequest(personalGroupId, data));
    const createdTasks: ITarea[] = [
      createLocalTask(data, {
        gid: personalGroupId,
        id: parentTaskId
      })
    ];

    for (const subtask of data.isComposed ? data.subtasks : []) {
      const subtaskData = createSubtaskFormData(data, subtask);
      const subtaskId = await createTask(buildCreateTaskRequest(personalGroupId, subtaskData, parentTaskId));

      createdTasks.push(
        createLocalTask(subtaskData, {
          gid: personalGroupId,
          id: subtaskId,
          idObjetivo: parentTaskId,
          tipo: "UNIQUE"
        })
      );
    }

    setTasks((prevTasks) => [...prevTasks, ...createdTasks]);
  };

  const recreateTaskWithChanges = async (currentTask: ITarea, currentSubtasks: ITarea[], data: ITaskFormData) => {
    await createTaskWithSubtasks(data);

    for (const subtask of currentSubtasks) {
      await deleteTask(subtask.id);
    }

    await deleteTask(currentTask.id);
    setTasks((prevTasks) => removeTaskWithSubtasks(prevTasks, currentTask.id));
  };

  const editTaskWithSubtasks = async (currentTask: ITarea, currentSubtasks: ITarea[], data: ITaskFormData) => {
    await editTask(currentTask.id, buildEditTaskRequest(data));

    let nextTasks = tasks.map((task) =>
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

      if (personalGroupId === null) {
        continue;
      }

      const subtaskData = createSubtaskFormData(data, subtask);
      const newSubtaskId = await createTask(buildCreateTaskRequest(personalGroupId, subtaskData, currentTask.id));

      nextTasks = [
        ...nextTasks,
        createLocalTask(subtaskData, {
          gid: personalGroupId,
          id: newSubtaskId,
          idObjetivo: currentTask.id,
          tipo: "UNIQUE"
        })
      ];
    }

    setTasks(nextTasks);
  };

  const handleModalSubmit = async (data: ITaskFormData) => {
    try {
      if (!initialDataModal) {
        await createTaskWithSubtasks(data);
        closeModalHandle();
        return;
      }

      const currentSubtasks = getTaskSubtasks(tasks, initialDataModal.id);

      if (shouldRecreateTaskOnEdit(initialDataModal, currentSubtasks, data)) {
        await recreateTaskWithChanges(initialDataModal, currentSubtasks, data);
      } else {
        await editTaskWithSubtasks(initialDataModal, currentSubtasks, data);
      }

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
    try {
      await completeTask(taskId);
    } catch (completeError) {
      console.error("Error al completar la tarea:", completeError);
    } finally {
      setTasks((prevTasks) => toggleTaskCompleted(prevTasks, `${taskId}`));
    }
  };

  const handleToggleSubtaskCompleted = async (taskId: number, subtaskId: number) => {
    try {
      await completeTask(subtaskId);
    } catch (completeError) {
      console.error("Error al completar la subtarea:", completeError);
    } finally {
      setTasks((prevTasks) => toggleSubtaskCompleted(prevTasks, `${taskId}`, `${subtaskId}`));
    }
  };

  const dashboardTasks = [...getDailyTasks(tasks), ...getHabitTasks(tasks)].filter((task) => !isTaskCompleted(task));
  const selectedTaskSubtasks = initialDataModal ? getTaskSubtasks(tasks, initialDataModal.id) : [];

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
      className="relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white"
    >
      <div className={`${isOpen ? "" : "hidden"} fixed inset-0 z-50 flex items-center justify-center`}>
        <Modal
          onSubmit={handleModalSubmit}
          onCancel={closeModalHandle}
          onDelete={initialDataModal ? handleDeleteTask : null}
          initialData={initialDataModal}
          subtasks={selectedTaskSubtasks}
        />
      </div>

      <Navbar />

      <section className="mx-auto flex w-full max-w-7xl flex-col items-center justify-center gap-4 px-4 py-6">
        <article className="relative flex w-full max-w-2xl flex-col gap-6 rounded-2xl border border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-6 shadow-[0_15px_32px_#090b1f59]">
          <header>
            <p className="pb-2 text-[0.78rem] uppercase tracking-[0.08em] text-[#c9b7ff]">Bienvenido de vuelta</p>
            <h1 className="font-['Press_Start_2P'] text-xl sm:text-2xl">Hi, Astrais</h1>
            <p className="mt-1">Que te queda por hacer?</p>
          </header>
          <div className="grid w-2/3 grid-cols-1 gap-2.5 sm:grid-cols-2">
            <button
              onClick={() => {
                setInitialDataModal(null);
                setIsOpen(true);
              }}
              className="cursor-pointer rounded-xl border border-transparent bg-[linear-gradient(90deg,#8b5cf6,#3b82f6)] px-3 py-2 text-[#f8f9ff] transition-colors duration-200"
            >
              Crear tarea
            </button>
            <NavLink className="cursor-pointer rounded-xl border border-white/15 bg-white/10 px-3 py-2 text-center text-[#f8f9ff] backdrop-blur-sm transition-colors duration-200 hover:bg-white/20" to="/groups?openCreateModal=true">
              <button>Crear un grupo</button>
            </NavLink>
            <NavLink className="cursor-pointer rounded-xl border border-white/15 bg-white/10 px-3 py-2 text-center text-[#f8f9ff] backdrop-blur-sm transition-colors duration-200 hover:bg-white/20" to="/profile">
              <button>Ver perfil</button>
            </NavLink>
            <NavLink className="cursor-pointer rounded-xl border border-white/15 bg-white/10 px-3 py-2 text-center text-[#f8f9ff] backdrop-blur-sm transition-colors duration-200 hover:bg-white/20" to="/shop">
              <button>Cambiar la mascota</button>
            </NavLink>
          </div>
          <img className="absolute -bottom-7 -right-56 z-10 w-9/10" src={astra} alt="Astra" />
        </article>

        <div className="grid w-full grid-cols-1 gap-4 lg:grid-cols-3">
          <article className="rounded-2xl border border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-4 shadow-[0_15px_32px_#090b1f59]">
            <header className="mb-3">
              <NavLink to="/tasks">
                <h2 className="font-['Press_Start_2P'] text-lg">Tareas Pendientes</h2>
              </NavLink>
            </header>
            {error && <p className="pb-3 text-sm text-red-200">{error}</p>}
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
          </article>

          <div className="flex flex-col gap-4 lg:col-span-2">
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <NavLink to="/shop">
                <article className="h-full rounded-2xl border border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-4 shadow-[0_15px_32px_#090b1f59]">
                  <header className="mb-3">
                    <h2 className="font-['Press_Start_2P'] text-lg">Tienda</h2>
                  </header>
                  <button className="flex w-full flex-row justify-center">
                    <Pet url={astra} />
                  </button>
                </article>
              </NavLink>

              <div className="flex flex-col gap-4">
                <article className="rounded-2xl border border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-4 shadow-[0_15px_32px_#090b1f59]">
                  <header className="mb-3">
                    <button className="flex items-center gap-2">
                      <h2 className="font-['Press_Start_2P'] text-lg">Notificaciones</h2>
                      <span className="rounded-full bg-state-error px-2 py-1 text-xs text-white">{notif}</span>
                    </button>
                  </header>
                </article>

                <NavLink to="/achievements">
                  <article className="flex h-full flex-col gap-4 rounded-2xl border border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-4 shadow-[0_15px_32px_#090b1f59]">
                    <header className="mb-3">
                      <h2 className="font-['Press_Start_2P'] text-lg">Logros</h2>
                    </header>
                    <div className="flex flex-row justify-between">
                      <Achiv />
                      <Achiv />
                      <Achiv />
                    </div>
                  </article>
                </NavLink>
              </div>
            </div>

            <article className="rounded-2xl border border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-4 shadow-[0_15px_32px_#090b1f59]">
              <header className="mb-3">
                <h2 className="font-['Press_Start_2P'] text-lg">Minijuegos</h2>
              </header>
              <div className="flex flex-col items-center gap-4">
                <img src={game} className="h-auto w-1/2 max-w-30 rounded-lg" alt="Juego" />
                <NavLink to="/games">
                  <button className="w-full max-w-xs cursor-pointer rounded-xl border border-white/25 bg-white/10 px-3 py-2 text-[#f8f9ff] transition-colors duration-200 hover:bg-white/20">
                    Jugar ahora
                  </button>
                </NavLink>
              </div>
            </article>
          </div>
        </div>
      </section>
    </main>
  );
}
