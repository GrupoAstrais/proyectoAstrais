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
  getRootTasks,
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

const normalizeObjectiveId = (idObjetivo?: number): number | undefined => {
  return typeof idObjetivo === "number" && idObjetivo >= 0 ? idObjetivo : undefined;
};

const normalizeTaskFormData = (data: ITaskFormData, fallbackObjetivoId?: number): ITaskFormData => ({
  ...data,
  idObjetivo: normalizeObjectiveId(data.idObjetivo) ?? normalizeObjectiveId(fallbackObjetivoId)
});

const recreateTaskChildren = async (gid: number, parentTaskId: number, subtasks: ITarea[]): Promise<ITarea[]> => {
  const recreatedSubtasks: ITarea[] = [];

  for (const subtask of subtasks) {
    const subtaskData = buildTaskFormData(subtask);
    const subtaskId = await createTask(buildCreateTaskRequest(gid, subtaskData, parentTaskId));

    recreatedSubtasks.push(
      createLocalTask(subtaskData, {
        gid,
        id: subtaskId,
        idObjetivo: parentTaskId,
        tipo: "UNIQUE"
      })
    );
  }

  return recreatedSubtasks;
};

export default function Tasks() {
  const [tasks, setTasks] = useState<ITarea[]>([]);
  const [personalGroupId, setPersonalGroupId] = useState<number | null>(null);
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [activeDiarias, setActiveDiarias] = useState<TTaskTimeFilter>("Today");
  const [activeHabitos, setActiveHabitos] = useState<TTaskTimeFilter>("Today");
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [diariasCompletedFilters, setDiariasCompletedFilters] = useState({
    completed: false,
    pending: false
  });
  const [habitosCompletedFilters, setHabitosCompletedFilters] = useState({
    completed: false,
    pending: false
  });
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
        console.error("Error al cargar las tareas:", loadError);
        setError("No se pudieron cargar las tareas.");
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

  const recreateTaskWithChanges = async (currentTask: ITarea, currentSubtasks: ITarea[], data: ITaskFormData) => {
    if (personalGroupId === null) {
      return;
    }

    const normalizedData = normalizeTaskFormData(data, currentTask.idObjetivo);
    const createdTaskId = await createTask(buildCreateTaskRequest(personalGroupId, normalizedData));
    const recreatedTasks: ITarea[] = [
      createLocalTask(normalizedData, {
        gid: personalGroupId,
        id: createdTaskId,
        idObjetivo: normalizedData.idObjetivo
      })
    ];

    if (typeof normalizedData.idObjetivo !== "number") {
      recreatedTasks.push(...(await recreateTaskChildren(personalGroupId, createdTaskId, currentSubtasks)));
    }

    for (const subtask of currentSubtasks) {
      await deleteTask(subtask.id);
    }

    await deleteTask(currentTask.id);

    setTasks((prevTasks) => [...removeTaskWithSubtasks(prevTasks, currentTask.id), ...recreatedTasks]);
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

    if (data.taskType === "objetivo" && typeof normalizedData.idObjetivo !== "number") {
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

      const currentSubtasks = getTaskSubtasks(tasks, initialDataModal.id);

      if (shouldRecreateTaskOnEdit(initialDataModal, normalizedData)) {
        await recreateTaskWithChanges(initialDataModal, currentSubtasks, normalizedData);
      } else {
        await editTaskWithSubtasks(initialDataModal, normalizedData);
      }

      closeModalHandle();
    } catch (submitError) {
      console.error("Error al guardar la tarea:", submitError);
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
      console.error("Error al borrar la tarea:", deleteError);
      setError("No se pudo borrar la tarea.");
    }
  };

  const handleActiveDiarias = (active: string) => {
    setSelectedDate(null);
    setActiveDiarias(active as TTaskTimeFilter);
  };

  const handleActiveHabitos = (active: string) => {
    setSelectedDate(null);
    setActiveHabitos(active as TTaskTimeFilter);
  };

  const handleActiveDiariasCompleted = (active: string) => {
    if (active === "Completadas") {
      setDiariasCompletedFilters((prev) => ({
        ...prev,
        completed: !prev.completed
      }));
      return;
    }

    setDiariasCompletedFilters((prev) => ({
      ...prev,
      pending: !prev.pending
    }));
  };

  const handleActiveHabitosCompleted = (active: string) => {
    if (active === "Completadas") {
      setHabitosCompletedFilters((prev) => ({
        ...prev,
        completed: !prev.completed
      }));
      return;
    }

    setHabitosCompletedFilters((prev) => ({
      ...prev,
      pending: !prev.pending
    }));
  };

  const handleToggleTaskCompleted = async (taskId: number) => {
    try {
      await completeTask(taskId);
    } catch (completeError) {
      console.error("Error al cambiar el estado de la tarea:", completeError);
    } finally {
      setTasks((prevTasks) => toggleTaskCompleted(prevTasks, `${taskId}`));
    }
  };

  const handleToggleSubtaskCompleted = async (taskId: number, subtaskId: number) => {
    const subtask = tasks.find((task) => task.id === subtaskId);
    const shouldSyncParent = subtask ? !isTaskCompleted(subtask) : false;

    try {
      await completeTask(subtaskId);
      if (shouldSyncParent) {
        const parentTask = tasks.find((task) => task.id === taskId);
        const siblingSubtasks = getTaskSubtasks(tasks, taskId).filter((task) => task.id !== subtaskId);

        if (parentTask && siblingSubtasks.every((task) => isTaskCompleted(task))) {
          await completeTask(taskId);
        }
      }
    } catch (completeError) {
      console.error("Error al cambiar el estado de la subtarea:", completeError);
    } finally {
      setTasks((prevTasks) => toggleSubtaskCompleted(prevTasks, `${taskId}`, `${subtaskId}`));
    }
  };

  const handleSelectedDate = (date: Date) => {
    setSelectedDate(date);
  };

  const diariasTasks = getDailyTasks(tasks);
  const habitosTasks = getHabitTasks(tasks);

  const filteredDiariasTasks = sortTasksByCompleted(
    filterTasksByCompleted(filterTasksByTime(diariasTasks, activeDiarias, selectedDate), diariasCompletedFilters)
  );

  const filteredHabitosTasks = sortTasksByCompleted(
    filterTasksByCompleted(filterTasksByTime(habitosTasks, activeHabitos, selectedDate), habitosCompletedFilters)
  );

  const availableObjectives = getRootTasks(tasks).filter((task) => task.id !== initialDataModal?.id);

  const editTaskHandle = (id: number) => {
    const taskToEdit = tasks.find((task) => task.id === id);

    if (!taskToEdit) {
      return;
    }

    setInitialDataModal(taskToEdit);
    setIsOpen(true);
  };

  return (
    <div
      style={{ backgroundImage: `url(${bgImage})` }}
      className="relative flex min-h-screen flex-col gap-4 bg-cover bg-center font-['Space_Grotesk'] text-white"
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

      <div className="flex flex-col gap-6 px-2">
        <button
          onClick={() => {
            setInitialDataModal(null);
            setIsOpen(true);
          }}
          className="ml-auto w-full rounded-md border border-[#F4E9E9]/15 bg-accent-beige-300/25 px-4 py-2 backdrop-blur-sm md:w-1/5"
        >
          <span className="text-2xl font-bold">+ Anadir tarea</span>
        </button>

        {error && <p className="px-10 text-center text-sm text-red-200">{error}</p>}

        <div className="grid grid-cols-1 gap-4 px-10 pt-5 sm:grid-cols-2 md:flex md:flex-row">
          <div className="pb-2 md:w-1/3">
            <h1 className="pb-5 text-3xl">Diarias</h1>
            <div className="flex flex-col justify-center gap-2">
              <div className="flex flex-col gap-2.5">
                <div className="flex flex-row justify-center gap-2.5">
                  <ButtonFilter esOtroActivo={selectedDate ? "" : activeDiarias} active={!selectedDate && activeDiarias === "Today"} handleActive={handleActiveDiarias} titulo="Today" />
                  <ButtonFilter esOtroActivo={selectedDate ? "" : activeDiarias} active={!selectedDate && activeDiarias === "Tomorrow"} handleActive={handleActiveDiarias} titulo="Tomorrow" />
                  <ButtonFilter esOtroActivo={selectedDate ? "" : activeDiarias} active={!selectedDate && activeDiarias === "All"} handleActive={handleActiveDiarias} titulo="All" />
                </div>
                <div className="flex flex-row justify-center gap-2.5">
                  <ButtonComplete title="Completadas" active={diariasCompletedFilters.completed} handleActive={handleActiveDiariasCompleted} />
                  <ButtonComplete title="Pendientes" active={diariasCompletedFilters.pending} handleActive={handleActiveDiariasCompleted} />
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
                    onComplete={handleToggleTaskCompleted}
                    onToggleSubtask={handleToggleSubtaskCompleted}
                    onToggleConfig={editTaskHandle}
                  />
                ))
              )}
            </div>
          </div>

          <div className="pb-2 md:w-1/3">
            <h1 className="pb-5 text-3xl">Habitos</h1>
            <div className="flex flex-col justify-center gap-2">
              <div className="flex flex-col gap-2.5">
                <div className="flex flex-row justify-center gap-2.5">
                  <ButtonFilter esOtroActivo={selectedDate ? "" : activeHabitos} active={!selectedDate && activeHabitos === "Today"} handleActive={handleActiveHabitos} titulo="Today" />
                  <ButtonFilter esOtroActivo={selectedDate ? "" : activeHabitos} active={!selectedDate && activeHabitos === "Tomorrow"} handleActive={handleActiveHabitos} titulo="Tomorrow" />
                  <ButtonFilter esOtroActivo={selectedDate ? "" : activeHabitos} active={!selectedDate && activeHabitos === "All"} handleActive={handleActiveHabitos} titulo="All" />
                </div>
                <div className="flex flex-row justify-center gap-2.5">
                  <ButtonComplete title="Completadas" active={habitosCompletedFilters.completed} handleActive={handleActiveHabitosCompleted} />
                  <ButtonComplete title="Pendientes" active={habitosCompletedFilters.pending} handleActive={handleActiveHabitosCompleted} />
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
                    onComplete={handleToggleTaskCompleted}
                    onToggleSubtask={handleToggleSubtaskCompleted}
                    onToggleConfig={editTaskHandle}
                  />
                ))
              )}
            </div>
          </div>

          <div className="flex flex-col md:w-1/3">
            <Calendar selectedDate={selectedDate} onSelectDate={handleSelectedDate} />
          </div>
        </div>
      </div>
    </div>
  );
}
