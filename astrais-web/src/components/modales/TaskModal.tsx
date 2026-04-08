import { useState, useRef } from "react";
import DifficultyModal from "../ui/DifficultyModal";
import TaskType from "../ui/TaskType";
import DiaryHabit from "../ui/DiaryHabit";
import { formatTaskDate } from "../../data/Api";

interface ModalProps {
  onSubmit: (data: any) => void;
  onCancel: () => void;
}

export default function Modal({ onSubmit, onCancel }: ModalProps) {
    //variables con datos de la tarea
  const [name, setName] = useState<string>("");
  const [difficulty, setDifficulty] = useState<"EASY" | "MEDIUM" | "HARD">("MEDIUM");
  const [taskType, setTaskType] = useState<"habit" | "diary" | null>(null);
  const [isComposed, setIsComposed] = useState<boolean>(false);
  const [subtasks, setSubtasks] = useState<{ id: string; name: string; completed: boolean }[]>([]);
  const [tags, setTags] = useState<{ name: string; color?: string }[]>([]);
  const [habitFrequency, setHabitFrequency] = useState<"daily" | "weekly" | "monthly" | null>(null);
  const [taskDate, setTaskDate] = useState<string>(formatTaskDate(new Date()));

  //datos obtenidos por los inputs
  const tagInputRef = useRef<HTMLInputElement>(null);
  const subtaskInputRef = useRef<HTMLInputElement>(null);

  // Colores disponibles para tags
  const tagColors = [
    { name: "mint", bg: "bg-accent-mint-500", border: "border-accent-mint-500" },
    { name: "beige", bg: "bg-accent-first-beige-300", border: "border-accent-first-beige-300" },
    { name: "secondary", bg: "bg-secondary-500", border: "border-secondary-500" },
    { name: "success", bg: "bg-state-success", border: "border-state-success" },
    { name: "error", bg: "bg-state-error", border: "border-state-error" },
    { name: "info", bg: "bg-state-info", border: "border-state-info" },
  ];

  const addTag = () => {
    const input = tagInputRef.current;
    if (!input || !input.value.trim()) return;
    const tagName = input.value.trim();
    setTags([...tags, { name: tagName }]);
    input.value = "";
  };

  const removeTag = (index: number) => {
    setTags(tags.filter((_, i) => i !== index));
  };

  const addSubtask = () => {
    const input = subtaskInputRef.current;
    if (!input || !input.value.trim()) return;
    const newName = input.value.trim();
    setSubtasks([
      ...subtasks,
      { id: Date.now().toString(), name: newName, completed: false },
    ]);
    input.value = "";
  };

  const toggleSubtaskCompletion = (id: string) => {
    setSubtasks(subtasks.map(st => st.id === id ? { ...st, completed: !st.completed } : st));
  };

  const removeSubtask = (id: string) => {
    setSubtasks(subtasks.filter(st => st.id !== id));
  };

  const resetForm = () => {
    setName("");
    setDifficulty("MEDIUM"); // valor inicial
    setTaskType(null);
    setIsComposed(false);
    setSubtasks([]);
    setTags([]);
    setHabitFrequency(null);
    setTaskDate(formatTaskDate(new Date()));

    // Reiniciar los inputs de texto si es necesario
    if (tagInputRef.current) tagInputRef.current.value = "";
    if (subtaskInputRef.current) subtaskInputRef.current.value = "";
  };

  const handleSubmit = (e: React.SubmitEvent) => {
    e.preventDefault();

    // Validación: si es hábito, frecuencia es obligatoria
    if (taskType === "habit" && !habitFrequency) {
      alert("Por favor, selecciona la frecuencia del hábito.");
      return;
    }

    if(name == "") {
      alert("El nombre es obligatorio");
      return;
    }

    if(taskType == null) {
      alert("Selecciona si la tarea es hábito o diaria.");
      return;
    }

    const data = {
      name,
      difficulty,
      taskType,
      isComposed,
      subtasks,
      tags,
      habitFrequency,
      taskDate,
    };

    onSubmit(data);

    resetForm();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 overflow-y-auto grow">
    <form onSubmit={handleSubmit}
      className="flex flex-col font-['Space_Grotesk'] h-auto w-1/2 lg:w-1/4 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] rounded-md p-4 gap-3 border-accent-beige-300"
    >
      <h1 className="font-['Press_Start_2P'] text-xl text-center">Añadir tarea</h1>

      {/* Nombre */}
      <div className="bg-accent-beige-300/80 border border-white/15  rounded-md py-4 px-2">
        <input type="text" value={name}onChange={(e) => setName(e.target.value)} placeholder="Nombre" className="w-full text-primary-900 bg-transparent outline-none" required/>
      </div>

      <div className="bg-accent-beige-300/80 border border-white/15  rounded-md py-4 px-2">
        <input type="date" value={taskDate} onChange={(e) => setTaskDate(e.target.value)} className="w-full text-primary-900 bg-transparent outline-none" />
      </div>

      {/* Dificultad */}
      <div className="flex flex-row justify-around bg-accent-beige-300/80 border border-white/15  py-4 px-2 rounded-md">
        <DifficultyModal handleActive={(d) => setDifficulty(d as "EASY" | "MEDIUM" | "HARD")} esOtroActivo={difficulty} dificultad="EASY" />
        <DifficultyModal handleActive={(d) => setDifficulty(d as "EASY" | "MEDIUM" | "HARD")} esOtroActivo={difficulty} dificultad="MEDIUM" />
        <DifficultyModal handleActive={(d) => setDifficulty(d as "EASY" | "MEDIUM" | "HARD")}  esOtroActivo={difficulty} dificultad="HARD" />
      </div>

      {/* Tipo: Hábito / Diaria */}
      <div className="flex flex-row justify-around bg-accent-beige-300/80 border border-white/15  py-4 px-2 rounded-md">
        <DiaryHabit handleActive={(t) => setTaskType(t === "Hábito" ? "habit" : "diary")} titulo="Hábito" esOtroActivo={taskType === "habit" ? "Hábito" : ""}
        />
        <DiaryHabit handleActive={(t) => setTaskType(t === "Diaria" ? "diary" : null)} titulo="Diaria" esOtroActivo={taskType === "diary" ? "Diaria" : ""} />
      </div>

      {/* Es compuesta? */}
      <div className="flex flex-row justify-around bg-accent-beige-300/80 border border-white/15  py-4 px-2 rounded-md">
        <TaskType active={isComposed} handleActive={setIsComposed} />
      </div>

      {/* Sección de subtareas (solo si es compuesta) */}
      {isComposed && (
        <div className="bg-accent-beige-300 rounded-md p-3">
          <h3 className="font-bold mb-2 text-primary-900">Subtareas</h3>
          <div className="flex gap-2 mb-3">
            <input ref={subtaskInputRef} type="text" placeholder="Nueva subtarea" className="flex-1 px-3 py-1 text-primary-900 rounded border border-primary-900" />
            <button type="button" onClick={addSubtask} className="bg-state-success text-[#00371A] px-3 py-1 rounded border border-primary-900 text-sm font-bold" >
              +
            </button>
          </div>
          <ul className="space-y-2 max-h-40 overflow-y-auto">
            {subtasks.length === 0 ? (
              <li className="text-gray-500 italic text-sm">No hay subtareas</li>
            ) : (
              subtasks.map((st) => (
                <li key={st.id} className={`flex items-center gap-2 p-2 rounded text-primary-900 ${ st.completed ? "bg-gray-200" : "" }`} >
                  <input type="checkbox" checked={st.completed} onChange={() => toggleSubtaskCompletion(st.id)} className="w-4 h-4 accent-primary-500" />
                  <span className={st.completed ? "line-through text-gray-600" : ""}> {st.name} </span>
                  <button type="button" onClick={() => removeSubtask(st.id)} className="ml-auto text-red-500 hover:text-red-700" > × </button>
                </li>
              ))
            )}
          </ul>
        </div>
      )}

      {/* Tags */}
      <div className="bg-accent-beige-300/80 border border-white/15 rounded-md p-3">
        <h3 className="font-bold mb-2 text-primary-900">Tags</h3>
        <div className="flex gap-2 mb-3">
          <input ref={tagInputRef} type="text" placeholder="Nombre del tag" className="flex-1 px-3 py-1 text-primary-900 rounded border border-primary-900" />
          <button type="button"  onClick={addTag} className="bg-state-success text-[#00371A] px-3 py-1 rounded border border-primary-900 text-sm font-bold" >  + </button>
        </div>

        {/* Lista de tags existentes */}
        <div className="flex flex-wrap gap-2">
          {tags.map((tag, idx) => (
            <div key={idx} className={`px-3 py-1 rounded-full text-xs font-medium flex items-center gap-1 ${
                tag.color
                  ? `${tag.color} text-white`
                  : "bg-gray-200 text-gray-800"
              }`}  > {tag.name}
              <button type="button" onClick={() => removeTag(idx)} className="text-gray-600 hover:text-gray-900">  × </button>
            </div>
          ))}
 </div>

        {/* Selección de color (opcional) */}
        <div className="mt-2 flex flex-wrap gap-1">
          {tagColors.map((color) => (
            <button key={color.name} type="button"
              onClick={() => {
                if (tags.length > 0) {
                  const lastTag = tags[tags.length - 1];
                  setTags([
                    ...tags.slice(0, -1),
                    { ...lastTag, color: color.bg },
                  ]);  } }} 
                  className={`w-6 h-6 rounded border ${color.border} ${color.bg}`} title={color.name} />
          ))}
        </div>
      </div>

      {/*  Frecuencia (solo si es hábito) */}
      {taskType === "habit" && (
        <div className="bg-accent-beige-300 rounded-md p-3">
          <h3 className="font-bold mb-2 text-primary-900">Frecuencia</h3>
          <div className="grid grid-cols-3 gap-2">
            {[
              { value: "daily", label: "Cada día" },
              { value: "weekly", label: "Cada semana" },
              { value: "monthly", label: "Cada mes" },
            ].map(({ value, label }) => (
              <button key={value} type="button"  onClick={() => setHabitFrequency(value as "daily" | "weekly" | "monthly")}
                className={`py-2 rounded border ${
                  habitFrequency === value
                    ? "bg-primary-500 text-white border-primary-900"
                    : "bg-white text-primary-900 border-primary-900"
                }`}
              > {label} </button>
            ))}
          </div>
          {!habitFrequency && (
            <p className="text-red-500 text-xs mt-2">* Obligatorio para hábitos</p>
          )}
        </div>
      )}

      {/* Botones */}
      <div className="flex flex-row justify-around pt-2">
        <button type="submit" className="bg-state-success p-2 rounded-md border border-primary-900 text-[#00371A] font-bold min-w-25" >  Confirmar </button>
        <button type="button" onClick={onCancel} className="bg-state-error p-2 rounded-md border border-primary-900 text-[#460018] font-bold min-w-25" > Cancelar </button>
      </div>
    </form>
    </div>
  );
}
