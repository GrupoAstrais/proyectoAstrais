import { useEffect, useState } from "react";
import Navbar from "../../components/layout/Navbar";
import Task from "../../components/ui/Task";
import bgImage from "../../assets/homeScreenBack.jpg";
import type { ITarea } from "../../types/Interfaces";
import Calendar from "../../components/layout/Calendar";
import Modal from "../../components/modales/TaskModal";
import ButtonFilter from "../../components/ui/ButtonFilter";
import ButtonComplete from "../../components/ui/ButtonComplete";
import NotificationModal from "../../components/modales/NotificationModal";
import {
  buildCreateTaskRequest,
  buildEditTaskRequest,
  completeTask,
  createLocalTask,
  createTask,
  deleteTask,
  editTask,
  filterTasksByCompleted,
  filterTasksByTime,
  getDailyTasks,
  getHabitTasks,
  getTaskSubtasks,
  getTaskXpReward,
  getTasksFromGroup,
  getUserData,
  isTaskCompleted,
  removeTaskWithSubtasks,
  sortTasksByCompleted,
  toggleSubtaskCompleted,
  toggleTaskCompleted,
  uncompleteTask,
  type ITaskFormData,
  type TTaskTimeFilter
} from "../../data/Api";

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

const normalizeObjectiveId = (id?: number | null): number | undefined =>
  typeof id === "number" ? id : undefined;

const normalizeTaskFormData = (data: ITaskFormData, fallbackObjetivoId?: number | null): ITaskFormData => ({
  ...data,
  idObjetivo: normalizeObjectiveId(data.idObjetivo) ?? normalizeObjectiveId(fallbackObjetivoId)
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


// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function Tasks() {
  const [tasks, setTasks] = useState<ITarea[]>([]);
  const [personalGroupId, setPersonalGroupId] = useState<number | null>(null);
  const [isOpen, setIsOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [activeDiarias, setActiveDiarias] = useState<TTaskTimeFilter>("Today");
  const [activeHabitos, setActiveHabitos] = useState<TTaskTimeFilter>("Today");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [diariasCompletedFilters, setDiariasCompletedFilters] = useState({ completed: false, pending: false });
  const [habitosCompletedFilters, setHabitosCompletedFilters] = useState({ completed: false, pending: false });
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
        setTasks(await getTasksFromGroup(userData.personalGid));
      } catch {
        setError("No se pudieron cargar las tareas.");
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

  //console.log("DE TASKS: "+JSON.stringify(tasks, null, 2));
  // ---------------------------------------------------------------------------
  // Modal
  // ---------------------------------------------------------------------------

  const closeModal = () => {
    setInitialDataModal(null);
    setIsOpen(false);
  };

  const openEditModal = (id: number) => {
    const task = tasks.find((t) => t.id === id);
    if (!task) return;
    setInitialDataModal(task);
    setIsOpen(true);
  };

  // ---------------------------------------------------------------------------
  // Task mutations
  // ---------------------------------------------------------------------------

  const handleCreate = async (data: ITaskFormData) => {
    const id = await createTask(buildCreateTaskRequest(personalGroupId!, data));
    const localTask = createLocalTask(data, { gid: personalGroupId!, id, idObjetivo: data.idObjetivo });
    console.log("Created local task:", localTask); // ← добавь это
    setTasks((prev) => [...prev, localTask]);
  };

  const handleEdit = async (currentTask: ITarea, data: ITaskFormData) => {
    await editTask(currentTask.id, buildEditTaskRequest(data));
    setTasks((prev) =>
      prev.map((t) =>
        t.id === currentTask.id
          ? { ...t, titulo: data.name.trim(), descripcion: data.description.trim(), prioridad: data.difficulty, recompensaXp: getTaskXpReward(data.difficulty) }
          : t
      )
    );
  };


  const handleModalSubmit = async (data: ITaskFormData) => {
    const normalized = normalizeTaskFormData(data, initialDataModal?.idObjetivo);

    try {
      setError(null);

      if (!initialDataModal) {
        await handleCreate(normalized);
      } else {
        await handleEdit(initialDataModal, normalized);
      }

      closeModal();
    } catch {
      setError("No se pudieron guardar los cambios de la tarea.");
    }
  };

  const handleDeleteTask = async () => {
    if (!initialDataModal) return;

    try {
      const subtasks = getTaskSubtasks(tasks, initialDataModal.id);
      await Promise.all([...subtasks.map((s) => deleteTask(s.id)), deleteTask(initialDataModal.id)]);
      setTasks((prev) => removeTaskWithSubtasks(prev, initialDataModal.id));
      closeModal();
    } catch {
      setError("No se pudo borrar la tarea.");
    }
  };

  // ---------------------------------------------------------------------------
  // Toggle completed
  // ---------------------------------------------------------------------------

  const handleToggleTask = async (taskId: number) => {
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

  const handleToggleSubtask = async (taskId: number, subtaskId: number) => {
    const subtask = tasks.find((t) => t.id === subtaskId);
    const parentTask = tasks.find((t) => t.id === taskId);
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
        const siblings = getTaskSubtasks(tasks, taskId).filter((t) => t.id !== subtaskId);
        if (parentTask && !isTaskCompleted(parentTask) && siblings.every(isTaskCompleted)) {
          await completeTask(taskId);
        }
      }
    } finally {
      setTasks((prev) => toggleSubtaskCompleted(prev, `${taskId}`, `${subtaskId}`));
      if (subtask && !isTaskCompleted(subtask) && !wasCompletedBefore) {
        markTaskAsRewarded(subtask);
        showRewardNotification(subtask.recompensaXp ?? 0, subtask.recompensaLudion ?? 0);
      }
    }
  };

  // ---------------------------------------------------------------------------
  // Filters
  // ---------------------------------------------------------------------------

  const handleTimeFilter = (setter: (v: TTaskTimeFilter) => void) => (value: string) => {
    setSelectedDate(null);
    setter(value as TTaskTimeFilter);
  };

  const toggleCompletedFilter = (
    setter: React.Dispatch<React.SetStateAction<{ completed: boolean; pending: boolean }>>
  ) => (value: string) => {
    const key = value === "Completadas" ? "completed" : "pending";
    setter((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  // ---------------------------------------------------------------------------
  // Derived data
  // ---------------------------------------------------------------------------

  const getFilteredTasks = (
    source: ITarea[],
    timeFilter: TTaskTimeFilter,
    completedFilters: { completed: boolean; pending: boolean }
  ) =>
    sortTasksByCompleted(
      filterTasksByCompleted(filterTasksByTime(source, timeFilter, selectedDate), completedFilters)
    );

  const filteredDiariasTasks = getFilteredTasks(getDailyTasks(tasks).filter((t) => t.idObjetivo == null), activeDiarias, diariasCompletedFilters);
  const filteredHabitosTasks = getFilteredTasks(getHabitTasks(tasks), activeHabitos, habitosCompletedFilters);
  const availableObjectives = tasks.filter((t) => t.tipo === "OBJETIVO");
  console.log(tasks);

  // ---------------------------------------------------------------------------
  // Render helpers
  // ---------------------------------------------------------------------------

  const renderTimeFilters = (
    active: TTaskTimeFilter,
    handler: (v: string) => void
  ) =>
    (["Today", "Tomorrow", "All"] as TTaskTimeFilter[]).map((titulo) => (
      <ButtonFilter
        key={titulo}
        titulo={titulo}
        active={!selectedDate && active === titulo}
        esOtroActivo={selectedDate ? "" : active}
        handleActive={handler}
      />
    ));

  const renderCompletedFilters = (
    filters: { completed: boolean; pending: boolean },
    handler: (v: string) => void
  ) => (
    <>
      <ButtonComplete title="Completadas" active={filters.completed} handleActive={handler} />
      <ButtonComplete title="Pendientes" active={filters.pending} handleActive={handler} />
    </>
  );

  // ---------------------------------------------------------------------------
  // JSX
  // ---------------------------------------------------------------------------






  return (
    <div
      style={{ backgroundImage: `url(${bgImage})` }}
      className="relative flex min-h-screen flex-col gap-4 bg-cover bg-center font-['Space_Grotesk'] text-white"
    >
      <div className={`${isOpen ? "" : "hidden"} fixed inset-0 z-50 flex items-center justify-center`}>
        <Modal
          onSubmit={handleModalSubmit}
          onCancel={closeModal}
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

      <div className="flex flex-col gap-6 px-2">
        <button
          onClick={() => { setInitialDataModal(null); setIsOpen(true); }}
          className="ml-auto w-full rounded-md border border-[#F4E9E9]/15 bg-accent-beige-300/25 px-4 py-2 backdrop-blur-sm md:w-1/5"
        >
          <span className="text-2xl font-bold">+ Anadir tarea</span>
        </button>

        {error && <p className="px-10 text-center text-sm text-red-200">{error}</p>}

        <div className="grid grid-cols-1 gap-4 px-10 pt-5 sm:grid-cols-2 md:flex md:flex-row">

          {/* Diarias */}
          <div className="pb-2 md:w-1/3">
            <h1 className="pb-5 text-3xl">Diarias</h1>
            <div className="flex flex-col justify-center gap-2">
              <div className="flex flex-col gap-2.5">
                <div className="flex flex-row justify-center gap-2.5">
                  {renderTimeFilters(activeDiarias, handleTimeFilter(setActiveDiarias))}
                </div>
                <div className="flex flex-row justify-center gap-2.5">
                  {renderCompletedFilters(diariasCompletedFilters, toggleCompletedFilter(setDiariasCompletedFilters))}
                </div>
              </div>

              {loading ? (
                <p className="py-4 text-center italic text-gray-300">Cargando tareas...</p>
              ) : filteredDiariasTasks.length === 0 ? (
                <p className="py-4 text-center italic text-gray-400">No hay tareas diarias</p>
              ) : (
                filteredDiariasTasks.map((task) => (
                  <Task
                    key={task.id}
                    data={task}
                    subtasks={getTaskSubtasks(tasks, task.id)}
                    onComplete={handleToggleTask}
                    onToggleSubtask={handleToggleSubtask}
                    onToggleConfig={openEditModal}
                  />
                ))
              )}
            </div>
          </div>

          {/* Habitos */}
          <div className="pb-2 md:w-1/3">
            <h1 className="pb-5 text-3xl">Habitos</h1>
            <div className="flex flex-col justify-center gap-2">
              <div className="flex flex-col gap-2.5">
                <div className="flex flex-row justify-center gap-2.5">
                  {renderTimeFilters(activeHabitos, handleTimeFilter(setActiveHabitos))}
                </div>
                <div className="flex flex-row justify-center gap-2.5">
                  {renderCompletedFilters(habitosCompletedFilters, toggleCompletedFilter(setHabitosCompletedFilters))}
                </div>
              </div>

              {loading ? (
                <p className="py-4 text-center italic text-gray-300">Cargando habitos...</p>
              ) : filteredHabitosTasks.length === 0 ? (
                <p className="py-4 text-center italic text-gray-400">No hay habitos</p>
              ) : (
                filteredHabitosTasks.map((task) => (
                  <Task
                    key={task.id}
                    data={task}
                    subtasks={[]}
                    onComplete={handleToggleTask}
                    onToggleSubtask={handleToggleSubtask}
                    onToggleConfig={openEditModal}
                  />
                ))
              )}
            </div>
          </div>

          {/* Calendar */}
          <div className="flex flex-col md:w-1/3">
            <Calendar selectedDate={selectedDate} onSelectDate={setSelectedDate} />
          </div>

        </div>
      </div>
    </div>
  );
}
