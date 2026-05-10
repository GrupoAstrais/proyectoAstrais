import { getTaskPriorityLabel, normalizeTaskPriority } from "../../data/Api";

interface DifProps {
  dificultad: number;
}

// Etiqueta visual para la dificultad normalizada de una tarea.
export default function Dificultad({ dificultad }: DifProps) {
  const normalizedDifficulty = normalizeTaskPriority(dificultad);
  const label = getTaskPriorityLabel(normalizedDifficulty);
  const backgroundClass =
    normalizedDifficulty === 0
      ? "bg-state-success"
      : normalizedDifficulty === 1
        ? "bg-state-warning"
        : "bg-state-error";

  return (
    <div className={`rounded-xs px-2 font-['Space_Grotesk'] text-primary-900 shadow-xs shadow-primary-900 ${backgroundClass}`}>
      <p className="font-bold">{label}</p>
    </div>
  );
}
