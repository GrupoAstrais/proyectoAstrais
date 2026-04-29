import { useEffect, useState } from "react";
import Navbar from "../../components/layout/Navbar";
import Task from "../../components/ui/Task";
import bgImage from "../../assets/homeScreenBack.jpg";
import type { ITarea } from "../../types/Interfaces";
import Calendar from "../../components/layout/Calendar";
import Modal from "../../components/modales/TaskModal";
import ButtonFilter from "../../components/ui/ButtonFilter";
import ButtonComplete from "../../components/ui/ButtonComplete";
import {
  buildCreateTaskRequest,
  buildEditTaskRequest,
  buildTaskFormData,
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
  shouldRecreateTaskOnEdit,
  sortTasksByCompleted,
  toggleSubtaskCompleted,
  toggleTaskCompleted,
  type ITaskFormData,
  type TTaskTimeFilter
} from "../../data/Api";

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

const normalizeObjectiveId = (id?: number): number | undefined =>
  typeof id === "number" ? id : undefined;

const normalizeTaskFormData = (data: ITaskFormData, fallbackObjetivoId?: number): ITaskFormData => ({
  ...data,
  idObjetivo: normalizeObjectiveId(data.idObjetivo) ?? normalizeObjectiveId(fallbackObjetivoId)
});

const recreateSubtasks = async (gid: number, parentId: number, subtasks: ITarea[]): Promise<ITarea[]> => {
  const results: ITarea[] = [];

  for (const subtask of subtasks) {
    const subtaskData = buildTaskFormData(subtask);
    const subtaskId = await createTask(buildCreateTaskRequest(gid, subtaskData, parentId));
    results.push(createLocalTask(subtaskData, { gid, id: subtaskId, idObjetivo: parentId, tipo: "UNICO" }));
  }

  return results;
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

  const handleRecreate = async (currentTask: ITarea, currentSubtasks: ITarea[], data: ITaskFormData) => {
    const newId = await createTask(buildCreateTaskRequest(personalGroupId!, data));
    const recreated: ITarea[] = [
      createLocalTask(data, { gid: personalGroupId!, id: newId, idObjetivo: data.idObjetivo })
    ];

    if (typeof data.idObjetivo !== "number") {
      recreated.push(...(await recreateSubtasks(personalGroupId!, newId, currentSubtasks)));
    }

    await Promise.all([...currentSubtasks.map((s) => deleteTask(s.id)), deleteTask(currentTask.id)]);
    setTasks((prev) => [...removeTaskWithSubtasks(prev, currentTask.id), ...recreated]);
  };

  const handleModalSubmit = async (data: ITaskFormData) => {
    const normalized = normalizeTaskFormData(data, initialDataModal?.idObjetivo);

    try {
      setError(null);

      if (!initialDataModal) {
        await handleCreate(normalized);
      } else {
        const subtasks = getTaskSubtasks(tasks, initialDataModal.id);
        if (shouldRecreateTaskOnEdit(initialDataModal, normalized)) {
          await handleRecreate(initialDataModal, subtasks, normalized);
        } else {
          await handleEdit(initialDataModal, normalized);
        }
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
    try {
      await completeTask(taskId);
    } finally {
      setTasks((prev) => toggleTaskCompleted(prev, `${taskId}`));
    }
  };

  const handleToggleSubtask = async (taskId: number, subtaskId: number) => {
    const subtask = tasks.find((t) => t.id === subtaskId);
    const wasIncomplete = subtask ? !isTaskCompleted(subtask) : false;

    try {
      await completeTask(subtaskId);

      if (wasIncomplete) {
        const siblings = getTaskSubtasks(tasks, taskId).filter((t) => t.id !== subtaskId);
        const parentTask = tasks.find((t) => t.id === taskId);
        if (parentTask && siblings.every(isTaskCompleted)) {
          await completeTask(taskId);
        }
      }
    } finally {
      setTasks((prev) => toggleSubtaskCompleted(prev, `${taskId}`, `${subtaskId}`));
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

  const filteredDiariasTasks = getFilteredTasks(getDailyTasks(tasks).filter((t) => t.idObjetivo === undefined), activeDiarias, diariasCompletedFilters);
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