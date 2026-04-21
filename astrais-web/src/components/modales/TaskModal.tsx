import { useEffect, useRef, useState } from "react";
import type { ITarea } from "../../types/Interfaces";
import {
  buildTaskFormData,
  formatTaskDate,
  type ITaskFormData,
  type ITaskFormSubtask,
  type THabitFrequency,
  type TTaskPriority
} from "../../data/Api";
import DifficultyModal from "../ui/DifficultyModal";
import TaskType from "../ui/TaskType";
import DiaryHabit from "../ui/DiaryHabit";

interface ModalProps {
  onSubmit: (data: ITaskFormData) => void | Promise<void>;
  onCancel: () => void;
  onDelete?: (() => void | Promise<void>) | null;
  initialData?: ITarea | null;
  subtasks?: ITarea[];
}

const getDefaultFormData = (): ITaskFormData => ({
  name: "",
  description: "",
  difficulty: 1,
  taskType: "daily",
  isComposed: false,
  subtasks: [],
  habitFrequency: null,
  taskDate: formatTaskDate(new Date())
});

export default function Modal({
  onSubmit,
  onCancel,
  onDelete,
  initialData,
  subtasks = []
}: ModalProps) {
  const [formData, setFormData] = useState<ITaskFormData>(getDefaultFormData);
  const subtaskInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!initialData) {
      setFormData(getDefaultFormData());
      if (subtaskInputRef.current) {
        subtaskInputRef.current.value = "";
      }
      return;
    }

    setFormData(buildTaskFormData(initialData, subtasks));

    if (subtaskInputRef.current) {
      subtaskInputRef.current.value = "";
    }
  }, [initialData, subtasks]);

  useEffect(() => {
    if (formData.taskType === "habit" && formData.isComposed) {
      setFormData((prev) => ({
        ...prev,
        isComposed: false,
        subtasks: []
      }));
    }
  }, [formData.isComposed, formData.taskType]);

  const setDifficulty = (difficulty: TTaskPriority) => {
    setFormData((prev) => ({
      ...prev,
      difficulty
    }));
  };

  const setTaskType = (taskType: ITaskFormData["taskType"]) => {
    setFormData((prev) => ({
      ...prev,
      taskType,
      isComposed: taskType === "habit" ? false : prev.isComposed,
      subtasks: taskType === "habit" ? [] : prev.subtasks
    }));
  };

  const addSubtask = () => {
    const input = subtaskInputRef.current;

    if (!input || !input.value.trim()) {
      return;
    }

    const nextSubtask: ITaskFormSubtask = {
      id: `new-${Date.now()}`,
      name: input.value.trim()
    };

    setFormData((prev) => ({
      ...prev,
      subtasks: [...prev.subtasks, nextSubtask]
    }));

    input.value = "";
  };

  const updateSubtaskName = (subtaskId: number | string, name: string) => {
    setFormData((prev) => ({
      ...prev,
      subtasks: prev.subtasks.map((subtask) =>
        subtask.id === subtaskId
          ? {
              ...subtask,
              name
            }
          : subtask
      )
    }));
  };

  const removeSubtask = (subtaskId: number | string) => {
    setFormData((prev) => ({
      ...prev,
      subtasks: prev.subtasks.filter((subtask) => subtask.id !== subtaskId)
    }));
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      alert("El nombre es obligatorio.");
      return;
    }

    if (formData.taskType === "habit" && !formData.habitFrequency) {
      alert("Selecciona una frecuencia para el habito.");
      return;
    }

    if (formData.isComposed && formData.subtasks.length === 0) {
      alert("Las tareas compuestas necesitan al menos una subtarea.");
      return;
    }

    void onSubmit({
      ...formData,
      name: formData.name.trim(),
      description: formData.description.trim(),
      subtasks: formData.subtasks
        .map((subtask) => ({
          ...subtask,
          name: subtask.name.trim()
        }))
        .filter((subtask) => subtask.name.length > 0)
    });
  };

  const renderFrequencyButton = (value: THabitFrequency, label: string) => {
    const isActive = formData.habitFrequency === value;

    return (
      <button
        key={value}
        type="button"
        onClick={() =>
          setFormData((prev) => ({
            ...prev,
            habitFrequency: value
          }))
        }
        className={`rounded border py-2 ${
          isActive
            ? "border-primary-900 bg-primary-500 text-white"
            : "border-primary-900 bg-white text-primary-900"
        }`}
      >
        {label}
      </button>
    );
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-black/50 p-4">
      <form
        onSubmit={handleSubmit}
        className="flex h-auto w-full max-w-2xl flex-col gap-3 rounded-md border border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-4 font-['Space_Grotesk']"
      >
        <h1 className="text-center font-['Press_Start_2P'] text-xl">
          {initialData ? "Editar tarea" : "Anadir tarea"}
        </h1>

        <div className="rounded-md border border-white/15 bg-accent-beige-300/80 px-2 py-4">
          <input
            type="text"
            value={formData.name}
            onChange={(e) =>
              setFormData((prev) => ({
                ...prev,
                name: e.target.value
              }))
            }
            placeholder="Nombre"
            className="w-full bg-transparent text-primary-900 outline-none"
            required
          />
        </div>

        <div className="rounded-md border border-white/15 bg-accent-beige-300/80 px-2 py-4">
          <textarea
            value={formData.description}
            onChange={(e) =>
              setFormData((prev) => ({
                ...prev,
                description: e.target.value
              }))
            }
            placeholder="Descripcion"
            className="min-h-24 w-full resize-none bg-transparent text-primary-900 outline-none"
          />
        </div>

        {formData.taskType === "daily" && (
          <div className="rounded-md border border-white/15 bg-accent-beige-300/80 px-2 py-4">
            <input
              type="date"
              value={formData.taskDate}
              onChange={(e) =>
                setFormData((prev) => ({
                  ...prev,
                  taskDate: e.target.value
                }))
              }
              className="w-full bg-transparent text-primary-900 outline-none"
            />
          </div>
        )}

        <div className="flex flex-row justify-around rounded-md border border-white/15 bg-accent-beige-300/80 px-2 py-4">
          <DifficultyModal difficulty={0} selectedDifficulty={formData.difficulty} onSelect={setDifficulty} />
          <DifficultyModal difficulty={1} selectedDifficulty={formData.difficulty} onSelect={setDifficulty} />
          <DifficultyModal difficulty={2} selectedDifficulty={formData.difficulty} onSelect={setDifficulty} />
        </div>

        <div className="flex flex-row justify-around rounded-md border border-white/15 bg-accent-beige-300/80 px-2 py-4">
          <DiaryHabit
            handleActive={() => setTaskType("habit")}
            titulo="Habito"
            esOtroActivo={formData.taskType === "habit" ? "Habito" : ""}
          />
          <DiaryHabit
            handleActive={() => setTaskType("daily")}
            titulo="Diaria"
            esOtroActivo={formData.taskType === "daily" ? "Diaria" : ""}
          />
        </div>

        {formData.taskType === "daily" && (
          <div className="flex flex-row justify-around rounded-md border border-white/15 bg-accent-beige-300/80 px-2 py-4">
            <TaskType
              active={formData.isComposed}
              handleActive={(active) =>
                setFormData((prev) => ({
                  ...prev,
                  isComposed: active,
                  subtasks: active ? prev.subtasks : []
                }))
              }
            />
          </div>
        )}

        {formData.isComposed && (
          <div className="rounded-md bg-accent-beige-300 p-3">
            <h3 className="mb-2 font-bold text-primary-900">Subtareas</h3>
            <div className="mb-3 flex gap-2">
              <input
                ref={subtaskInputRef}
                type="text"
                placeholder="Nueva subtarea"
                className="flex-1 rounded border border-primary-900 px-3 py-1 text-primary-900"
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    e.preventDefault();
                    addSubtask();
                  }
                }}
              />
              <button
                type="button"
                onClick={addSubtask}
                className="rounded border border-primary-900 bg-state-success px-3 py-1 text-sm font-bold text-[#00371A]"
              >
                +
              </button>
            </div>
            <ul className="max-h-40 space-y-2 overflow-y-auto">
              {formData.subtasks.length === 0 ? (
                <li className="text-sm italic text-gray-600">No hay subtareas</li>
              ) : (
                formData.subtasks.map((subtask) => (
                  <li key={subtask.id} className="flex items-center gap-2 rounded bg-white/60 p-2 text-primary-900">
                    <input
                      type="text"
                      value={subtask.name}
                      onChange={(e) => updateSubtaskName(subtask.id, e.target.value)}
                      className="flex-1 bg-transparent outline-none"
                    />
                    <button
                      type="button"
                      onClick={() => removeSubtask(subtask.id)}
                      className="font-bold text-red-600 hover:text-red-800"
                    >
                      x
                    </button>
                  </li>
                ))
              )}
            </ul>
          </div>
        )}

        {formData.taskType === "habit" && (
          <div className="rounded-md bg-accent-beige-300 p-3">
            <h3 className="mb-2 font-bold text-primary-900">Frecuencia</h3>
            <div className="grid grid-cols-3 gap-2">
              {renderFrequencyButton("daily", "Cada dia")}
              {renderFrequencyButton("weekly", "Cada semana")}
              {renderFrequencyButton("monthly", "Cada mes")}
            </div>
          </div>
        )}

        <div className="flex flex-row flex-wrap justify-between gap-3 pt-2">
          {initialData && onDelete ? (
            <button
              type="button"
              onClick={() => void onDelete()}
              className="min-w-25 rounded-md border border-primary-900 bg-state-error p-2 font-bold text-[#460018]"
            >
              Borrar tarea
            </button>
          ) : (
            <div />
          )}

          <div className="flex flex-row gap-3">
            <button
              type="submit"
              className="min-w-25 rounded-md border border-primary-900 bg-state-success p-2 font-bold text-[#00371A]"
            >
              {initialData ? "Guardar cambios" : "Confirmar"}
            </button>
            <button
              type="button"
              onClick={onCancel}
              className="min-w-25 rounded-md border border-primary-900 bg-white p-2 font-bold text-primary-900"
            >
              Cancelar
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
