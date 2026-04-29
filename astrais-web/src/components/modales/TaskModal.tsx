import { useEffect, useState } from "react";
import type { ITarea } from "../../types/Interfaces";
import {
  buildTaskFormData,
  formatTaskDate,
  type ITaskFormData,
  type THabitFrequency,
  type TTaskPriority
} from "../../data/Api";
import DifficultyModal from "../ui/DifficultyModal";
import DiaryHabit from "../ui/DiaryHabit";

interface ModalProps {
  onSubmit: (data: ITaskFormData) => void | Promise<void>;
  onCancel: () => void;
  onDelete?: (() => void | Promise<void>) | null;
  initialData?: ITarea | null;
  subtasks?: ITarea[];
  tareasObjetivos: ITarea[];
}

const getDefaultFormData = (): ITaskFormData => ({
  name: "",
  description: "",
  difficulty: 1,
  taskType: "UNICO",
  habitFrequency: null,
  taskDate: formatTaskDate(new Date())
});

export default function Modal({
  onSubmit,
  onCancel,
  onDelete,
  initialData,
  tareasObjetivos
}: ModalProps) {
  const [formData, setFormData] = useState<ITaskFormData>(getDefaultFormData);
  const [objetivo, setObjetivo] = useState<number>();

  useEffect(() => {
    if (!initialData) {
      setFormData(getDefaultFormData());
      setObjetivo(undefined);
      return;
    }

    setFormData(buildTaskFormData(initialData));
    setObjetivo(initialData.idObjetivo);
  }, [initialData]);


  const setDifficulty = (difficulty: TTaskPriority) => {
    setFormData((prev) => ({
      ...prev,
      difficulty
    }));
  };

  const setTaskType = (taskType: ITaskFormData["taskType"]) => {
    setFormData((prev) => ({
      ...prev,
      taskType
    }));
  };


  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      alert("El nombre es obligatorio.");
      return;
    }

    if (formData.taskType === "HABITO" && !formData.habitFrequency) {
      alert("Selecciona una frecuencia para el habito.");
      return;
    }


    await void onSubmit({
      ...formData,
      name: formData.name.trim(),
      description: formData.description.trim(),
      idObjetivo: objetivo
    });

    if (!initialData) {
      setFormData(getDefaultFormData());
      setObjetivo(undefined);
    }
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

        {formData.taskType === "UNICO" && (
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

        {!initialData &&
          <div className="flex flex-row justify-around rounded-md border border-white/15 bg-accent-beige-300/80 px-2 py-4">
            <DiaryHabit
                handleActive={() => setTaskType("HABITO")}
                titulo="Habito"
                active={formData.taskType === "HABITO"}
                esOtroActivo={formData.taskType}
            />
            <DiaryHabit
                handleActive={() => setTaskType("UNICO")}
                titulo="Diaria"
                active={formData.taskType === "UNICO"}
                esOtroActivo={formData.taskType}
            />
            <DiaryHabit
                handleActive={() => setTaskType("OBJETIVO")}
                titulo="Objetivo"
                active={formData.taskType === "OBJETIVO"}
                esOtroActivo={formData.taskType}
            />
          </div>}

        {formData.taskType == "UNICO" && !initialData && (
          <div className="rounded-md bg-accent-beige-300 p-3">
            <h3 className="mb-2 font-bold text-primary-900">Elegir objetivo</h3>
            <div className="mb-3 flex gap-2">
              <select  className="text-primary-900"  id="objetivos" name="tareasObjetivos" value={objetivo ?? ""} onChange={(e) => setObjetivo(Number(e.target.value))}>
                <option  key={-1} value={""}>Elige tu objetivo</option>
                {
                  tareasObjetivos && tareasObjetivos.map((obj) => (
                    <option  key={obj.id} value={obj.id}>{obj.titulo}</option>
                  ))
                }
              </select>
            </div>
          </div>
        )}

        {formData.taskType === "HABITO"  && !initialData && (
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
              className="min-w-25 rounded-md border border-primary-900 bg-state-error p-2 font-bold text-primary-900"
            >
              Cancelar
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
