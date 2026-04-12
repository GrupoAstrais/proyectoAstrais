import { useEffect, useRef, useState } from "react";
import type { ITarea } from "../../types/Interfaces";
import { formatTaskDate } from "../../data/Api";
import DifficultyModal from "../ui/DifficultyModal";
import TaskType from "../ui/TaskType";
import DiaryHabit from "../ui/DiaryHabit";

interface ModalSubmitData {
  name: string;
  difficulty: "EASY" | "MEDIUM" | "HARD";
  taskType: "habit" | "diary";
  isComposed: boolean;
  subtasks: { id: string; name: string; completed: boolean }[];
  tags: { name: string; color?: string }[];
  habitFrequency: "daily" | "weekly" | "monthly" | null;
  taskDate: string;
}

interface ModalProps {
  onSubmit: (data: ModalSubmitData) => void;
  onCancel: () => void;
  initialData?: ITarea | null;
}

export default function Modal({ onSubmit, onCancel, initialData }: ModalProps) {
  const [name, setName] = useState<string>("");
  const [difficulty, setDifficulty] = useState<"EASY" | "MEDIUM" | "HARD">("MEDIUM");
  const [taskType, setTaskType] = useState<"habit" | "diary" | null>(null);
  const [isComposed, setIsComposed] = useState<boolean>(false);
  const [subtasks, setSubtasks] = useState<{ id: string; name: string; completed: boolean }[]>([]);
  const [tags, setTags] = useState<{ name: string; color?: string }[]>([]);
  const [habitFrequency, setHabitFrequency] = useState<"daily" | "weekly" | "monthly" | null>(null);
  const [taskDate, setTaskDate] = useState<string>(formatTaskDate(new Date()));

  const tagInputRef = useRef<HTMLInputElement>(null);
  const subtaskInputRef = useRef<HTMLInputElement>(null);

  const tagColors = [
    { name: "mint", bg: "bg-accent-mint-500", border: "border-accent-mint-500" },
    { name: "beige", bg: "bg-accent-first-beige-300", border: "border-accent-first-beige-300" },
    { name: "secondary", bg: "bg-secondary-500", border: "border-secondary-500" },
    { name: "success", bg: "bg-state-success", border: "border-state-success" },
    { name: "error", bg: "bg-state-error", border: "border-state-error" },
    { name: "info", bg: "bg-state-info", border: "border-state-info" }
  ];

  const resetForm = () => {
    setName("");
    setDifficulty("MEDIUM");
    setTaskType(null);
    setIsComposed(false);
    setSubtasks([]);
    setTags([]);
    setHabitFrequency(null);
    setTaskDate(formatTaskDate(new Date()));

    if (tagInputRef.current) tagInputRef.current.value = "";
    if (subtaskInputRef.current) subtaskInputRef.current.value = "";
  };

  useEffect(() => {
    setName(initialData?.title ?? "");
    setDifficulty(initialData?.dificultad ?? "MEDIUM");
    setTaskType(initialData?.taskType ?? null);
    setIsComposed(initialData?.isComposed ?? false);
    setSubtasks(initialData?.subtasks ?? []);
    setTags(initialData?.tags ?? []);
    setHabitFrequency(initialData?.habitFrequency ?? null);
    setTaskDate(initialData?.taskDate ?? formatTaskDate(new Date()));

    if (tagInputRef.current) tagInputRef.current.value = "";
    if (subtaskInputRef.current) subtaskInputRef.current.value = "";
  }, [initialData]);

  const addTag = () => {
    const input = tagInputRef.current;
    if (!input || !input.value.trim()) return;

    const tagName = input.value.trim();
    setTags((prevTags) => [...prevTags, { name: tagName }]);
    input.value = "";
  };

  const removeTag = (index: number) => {
    setTags((prevTags) => prevTags.filter((_, i) => i !== index));
  };

  const addSubtask = () => {
    const input = subtaskInputRef.current;
    if (!input || !input.value.trim()) return;

    const newName = input.value.trim();
    setSubtasks((prevSubtasks) => [
      ...prevSubtasks,
      { id: Date.now().toString(), name: newName, completed: false }
    ]);
    input.value = "";
  };

  const toggleSubtaskCompletion = (id: string) => {
    setSubtasks((prevSubtasks) =>
      prevSubtasks.map((subtask) =>
        subtask.id === id
          ? { ...subtask, completed: !subtask.completed }
          : subtask
      )
    );
  };

  const removeSubtask = (id: string) => {
    setSubtasks((prevSubtasks) => prevSubtasks.filter((subtask) => subtask.id !== id));
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!name.trim()) {
      alert("El nombre es obligatorio");
      return;
    }

    if (!taskType) {
      alert("Selecciona si la tarea es habito o diaria.");
      return;
    }

    if (taskType === "habit" && !habitFrequency) {
      alert("Por favor, selecciona la frecuencia del habito.");
      return;
    }

    onSubmit({
      name: name.trim(),
      difficulty,
      taskType,
      isComposed,
      subtasks,
      tags,
      habitFrequency,
      taskDate
    });

    resetForm();
  };

  return (
    <div className="fixed inset-0 z-50 flex grow items-center justify-center overflow-y-auto bg-black/50 p-4">
      <form
        onSubmit={handleSubmit}
        className="flex h-auto w-1/2 flex-col gap-3 rounded-md border-accent-beige-300 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-4 font-['Space_Grotesk'] lg:w-1/4"
      >
        <h1 className="text-center font-['Press_Start_2P'] text-xl">
          {initialData ? "Editar tarea" : "Anadir tarea"}
        </h1>

        <div className="rounded-md border border-white/15 bg-accent-beige-300/80 px-2 py-4">
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Nombre"
            className="w-full bg-transparent text-primary-900 outline-none"
            required
          />
        </div>

        <div className="rounded-md border border-white/15 bg-accent-beige-300/80 px-2 py-4">
          <input
            type="date"
            value={taskDate}
            onChange={(e) => setTaskDate(e.target.value)}
            className="w-full bg-transparent text-primary-900 outline-none"
          />
        </div>

        <div className="flex flex-row justify-around rounded-md border border-white/15 bg-accent-beige-300/80 px-2 py-4">
          <DifficultyModal handleActive={(d) => setDifficulty(d as "EASY" | "MEDIUM" | "HARD")} esOtroActivo={difficulty} dificultad="EASY" />
          <DifficultyModal handleActive={(d) => setDifficulty(d as "EASY" | "MEDIUM" | "HARD")} esOtroActivo={difficulty} dificultad="MEDIUM" />
          <DifficultyModal handleActive={(d) => setDifficulty(d as "EASY" | "MEDIUM" | "HARD")} esOtroActivo={difficulty} dificultad="HARD" />
        </div>

        <div className="flex flex-row justify-around rounded-md border border-white/15 bg-accent-beige-300/80 px-2 py-4">
          <DiaryHabit
            handleActive={(title) => setTaskType(title === "Habito" ? "habit" : "diary")}
            titulo="Habito"
            esOtroActivo={taskType === "habit" ? "Habito" : ""}
          />
          <DiaryHabit
            handleActive={(title) => setTaskType(title === "Diaria" ? "diary" : null)}
            titulo="Diaria"
            esOtroActivo={taskType === "diary" ? "Diaria" : ""}
          />
        </div>

        <div className="flex flex-row justify-around rounded-md border border-white/15 bg-accent-beige-300/80 px-2 py-4">
          <TaskType active={isComposed} handleActive={setIsComposed} />
        </div>

        {isComposed && (
          <div className="rounded-md bg-accent-beige-300 p-3">
            <h3 className="mb-2 font-bold text-primary-900">Subtareas</h3>
            <div className="mb-3 flex gap-2">
              <input
                ref={subtaskInputRef}
                type="text"
                placeholder="Nueva subtarea"
                className="flex-1 rounded border border-primary-900 px-3 py-1 text-primary-900"
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
              {subtasks.length === 0 ? (
                <li className="text-sm italic text-gray-500">No hay subtareas</li>
              ) : (
                subtasks.map((subtask) => (
                  <li
                    key={subtask.id}
                    className={`flex items-center gap-2 rounded p-2 text-primary-900 ${subtask.completed ? "bg-gray-200" : ""}`}
                  >
                    <input
                      type="checkbox"
                      checked={subtask.completed}
                      onChange={() => toggleSubtaskCompletion(subtask.id)}
                      className="h-4 w-4 accent-primary-500"
                    />
                    <span className={subtask.completed ? "line-through text-gray-600" : ""}>
                      {subtask.name}
                    </span>
                    <button
                      type="button"
                      onClick={() => removeSubtask(subtask.id)}
                      className="ml-auto text-red-500 hover:text-red-700"
                    >
                      x
                    </button>
                  </li>
                ))
              )}
            </ul>
          </div>
        )}

        <div className="rounded-md border border-white/15 bg-accent-beige-300/80 p-3">
          <h3 className="mb-2 font-bold text-primary-900">Tags</h3>
          <div className="mb-3 flex gap-2">
            <input
              ref={tagInputRef}
              type="text"
              placeholder="Nombre del tag"
              className="flex-1 rounded border border-primary-900 px-3 py-1 text-primary-900"
            />
            <button
              type="button"
              onClick={addTag}
              className="rounded border border-primary-900 bg-state-success px-3 py-1 text-sm font-bold text-[#00371A]"
            >
              +
            </button>
          </div>

          <div className="flex flex-wrap gap-2">
            {tags.map((tag, idx) => (
              <div
                key={idx}
                className={`flex items-center gap-1 rounded-full px-3 py-1 text-xs font-medium ${
                  tag.color ? `${tag.color} text-white` : "bg-gray-200 text-gray-800"
                }`}
              >
                {tag.name}
                <button
                  type="button"
                  onClick={() => removeTag(idx)}
                  className="text-gray-600 hover:text-gray-900"
                >
                  x
                </button>
              </div>
            ))}
          </div>

          <div className="mt-2 flex flex-wrap gap-1">
            {tagColors.map((color) => (
              <button
                key={color.name}
                type="button"
                onClick={() => {
                  if (tags.length === 0) return;

                  const lastTag = tags[tags.length - 1];
                  setTags([
                    ...tags.slice(0, -1),
                    { ...lastTag, color: color.bg }
                  ]);
                }}
                className={`h-6 w-6 rounded border ${color.border} ${color.bg}`}
                title={color.name}
              />
            ))}
          </div>
        </div>

        {taskType === "habit" && (
          <div className="rounded-md bg-accent-beige-300 p-3">
            <h3 className="mb-2 font-bold text-primary-900">Frecuencia</h3>
            <div className="grid grid-cols-3 gap-2">
              {[
                { value: "daily", label: "Cada dia" },
                { value: "weekly", label: "Cada semana" },
                { value: "monthly", label: "Cada mes" }
              ].map(({ value, label }) => (
                <button
                  key={value}
                  type="button"
                  onClick={() => setHabitFrequency(value as "daily" | "weekly" | "monthly")}
                  className={`rounded border py-2 ${
                    habitFrequency === value
                      ? "border-primary-900 bg-primary-500 text-white"
                      : "border-primary-900 bg-white text-primary-900"
                  }`}
                >
                  {label}
                </button>
              ))}
            </div>
            {!habitFrequency && (
              <p className="mt-2 text-xs text-red-500">* Obligatorio para habitos</p>
            )}
          </div>
        )}

        <div className="flex flex-row justify-around pt-2">
          <button
            type="submit"
            className="min-w-25 rounded-md border border-primary-900 bg-state-success p-2 font-bold text-[#00371A]"
          >
            {initialData ? "Guardar cambios" : "Confirmar"}
          </button>
          <button
            type="button"
            onClick={onCancel}
            className="min-w-25 rounded-md border border-primary-900 bg-state-error p-2 font-bold text-[#460018]"
          >
            Cancelar
          </button>
        </div>
      </form>
    </div>
  );
}
