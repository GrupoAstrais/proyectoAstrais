import Navbar from "../../components/layout/Navbar";
import Task from "../../components/ui/Task";
import bgImage from '../../assets/homeScreenBack.jpg';
import type { ITarea } from "../../types/Interfaces";
import Calendar from "../../components/layout/Calendar";
import { useState } from "react";
import Modal from "../../components/modales/TaskModal";
import ButtonFilter from "../../components/ui/ButtonFilter";
import ButtonComplete from "../../components/ui/ButtonComplete";
import { createLocalTask, filterTasksByCompleted, filterTasksByTime, sortTasksByCompleted, toggleSubtaskCompleted, toggleTaskCompleted, type TTaskTimeFilter } from "../../data/Api";

export default function Tasks() {
  const [tasks, setTasks] = useState<ITarea[]>([]);
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [activeDiarias, setActiveDiarias] = useState<TTaskTimeFilter>("Today");
  const [activeHabitos, setActiveHabitos] = useState<TTaskTimeFilter>("Today");
  const [diariasCompletedFilters, setDiariasCompletedFilters] = useState({
    completed: false,
    pending: false
  });
  const [habitosCompletedFilters, setHabitosCompletedFilters] = useState({
    completed: false,
    pending: false
  });

  const handleModalSubmit = (data: any) => {
    const newTask: ITarea = createLocalTask(data);

    setTasks([...tasks, newTask]);
    setIsOpen(false);
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
    if(active == "Completadas") {
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
  }

  const handleActiveHabitosCompleted = (active: string) => {
    if(active == "Completadas") {
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
  }

  const handleToggleTaskCompleted = (taskId: string) => {
    setTasks((prevTasks) => toggleTaskCompleted(prevTasks, taskId));
  }

  const handleToggleSubtaskCompleted = (taskId: string, subtaskId: string) => {
    setTasks((prevTasks) => toggleSubtaskCompleted(prevTasks, taskId, subtaskId));
  }

  const handleSelectedDate = (date: Date) => {
    setSelectedDate(date);
  }

  const diariasTasks = tasks.filter((t) => t.taskType === "diary");
  const habitosTasks = tasks.filter((t) => t.taskType === "habit");

  const filteredDiariasTasks = sortTasksByCompleted(
    filterTasksByCompleted(
      filterTasksByTime(diariasTasks, activeDiarias, selectedDate),
      diariasCompletedFilters
    )
  );

  const filteredHabitosTasks = sortTasksByCompleted(
    filterTasksByCompleted(
      filterTasksByTime(habitosTasks, activeHabitos, selectedDate),
      habitosCompletedFilters
    )
  );

  return (
    <div
      style={{ backgroundImage: `url(${bgImage})` }}
      className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white"
    >
      <div className={`${isOpen ? "" : "hidden"} fixed inset-0 z-50 flex items-center justify-center`}>
        <Modal
          onSubmit={handleModalSubmit}
          onCancel={() => setIsOpen(false)}
        />
      </div>

      <Navbar />

      <div className="flex flex-col gap-6 px-2">
        <button
          onClick={() => setIsOpen(true)}
          className="ml-auto border border-[#F4E9E9]/15 bg-accent-beige-300/25 rounded-md px-4 py-2 w-1/5 backdrop-blur-sm"
        >
          <span className="font-bold text-2xl ">+ Añadir tarea</span>
        </button>

        <div className="md:flex md:flex-row gap-4 px-10 pt-5 sm:grid sm:grid-cols-2">
          <div className="pb-2 md:w-1/3">
            <h1 className="pb-5 text-3xl">Diarias</h1>
            <div className="flex flex-col gap-2 justify-center">
              <div className="flex flex-col gap-2.5">
                <div className="flex flex-row gap-2.5 justify-center">
                  <ButtonFilter esOtroActivo={selectedDate ? "" : activeDiarias} active={!selectedDate && activeDiarias == "Today"} handleActive={handleActiveDiarias} titulo={"Today"} />
                  <ButtonFilter esOtroActivo={selectedDate ? "" : activeDiarias} active={!selectedDate && activeDiarias == "Tomorrow"} handleActive={handleActiveDiarias} titulo={"Tomorrow"} />
                  <ButtonFilter esOtroActivo={selectedDate ? "" : activeDiarias} active={!selectedDate && activeDiarias == "All"} handleActive={handleActiveDiarias} titulo={"All"} />
                </div>
                <div className="flex flex-row gap-2.5 justify-center">
                  <ButtonComplete title="Completadas" active={diariasCompletedFilters.completed} handleActive={handleActiveDiariasCompleted} />
                  <ButtonComplete title="Pendientes" active={diariasCompletedFilters.pending} handleActive={handleActiveDiariasCompleted} />
                </div>
              </div>

              {filteredDiariasTasks.length === 0 ?
              (
                <p className="text-gray-400 italic text-center py-4">No hay tareas diarias</p>
              ) : (
                filteredDiariasTasks.map((t, i) => (
                  <Task key={t.id ?? i} data={t} onComplete={handleToggleTaskCompleted} onToggleSubtask={handleToggleSubtaskCompleted} />
                ))
              )}
            </div>
          </div>

          <div className="pb-2 md:w-1/3">
            <h1 className="pb-5 text-3xl">Hábitos</h1>
            <div className="flex flex-col gap-2 justify-center">
              <div className="flex flex-col gap-2.5">
                <div className="flex flex-row gap-2.5 justify-center">
                  <ButtonFilter esOtroActivo={selectedDate ? "" : activeHabitos} active={!selectedDate && activeHabitos == "Today"} handleActive={handleActiveHabitos} titulo={"Today"} />
                  <ButtonFilter esOtroActivo={selectedDate ? "" : activeHabitos} active={!selectedDate && activeHabitos == "Tomorrow"} handleActive={handleActiveHabitos} titulo={"Tomorrow"} />
                  <ButtonFilter esOtroActivo={selectedDate ? "" : activeHabitos} active={!selectedDate && activeHabitos == "All"} handleActive={handleActiveHabitos} titulo={"All"} />
                </div>
                <div className="flex flex-row gap-2.5 justify-center">
                  <ButtonComplete title="Completadas" active={habitosCompletedFilters.completed} handleActive={handleActiveHabitosCompleted} />
                  <ButtonComplete title="Pendientes" active={habitosCompletedFilters.pending} handleActive={handleActiveHabitosCompleted} />
                </div>
              </div>

              {filteredHabitosTasks.length === 0 ?
              (
                <p className="text-gray-400 italic text-center py-4">No hay hábitos</p>
              ) : (
                filteredHabitosTasks.map((t, i) => (
                  <Task key={t.id ?? i} data={t} onComplete={handleToggleTaskCompleted} onToggleSubtask={handleToggleSubtaskCompleted} />
                ))
              )}
            </div>
          </div>

          <div className="md:w-1/3 flex flex-col">
            <Calendar selectedDate={selectedDate} onSelectDate={handleSelectedDate} />
          </div>
        </div>
      </div>
    </div>
  );
}
