import { getTaskPriorityLabel, normalizeTaskPriority, type TTaskPriority } from "../../data/Api";

interface DifProps {
  difficulty: TTaskPriority;
  selectedDifficulty: TTaskPriority;
  onSelect: (difficulty: TTaskPriority) => void;
}

export default function DifficultyModal({ difficulty, selectedDifficulty, onSelect }: DifProps) {
  const normalizedDifficulty = normalizeTaskPriority(difficulty);
  const isActive = selectedDifficulty === normalizedDifficulty;
  const baseColor =
    normalizedDifficulty === 0
      ? "bg-state-success"
      : normalizedDifficulty === 1
        ? "bg-state-warning"
        : "bg-state-error";

  return (
    <button
      onClick={() => onSelect(normalizedDifficulty)}
      type="button"
      className={`mb-2 rounded-xs px-2 font-['Space_Grotesk'] shadow-xs shadow-primary-900 ${
        isActive ? "translate-y-1 bg-secondary-700 text-white shadow-none" : `${baseColor} text-primary-900`
      }`}
    >
      <span className="font-bold">{getTaskPriorityLabel(normalizedDifficulty)}</span>
    </button>
  );
}
