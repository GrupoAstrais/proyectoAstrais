interface ProgressBarProps {
    value: number
}

// Barra de progreso para recompensas o avance visual.
export default function ProgressBar({ value } : ProgressBarProps) {
  return (
    <div className="w-full h-2.5 bg-gray-200 rounded-md overflow-hidden">
      <div
        className="h-full bg-state-warning transition-all duration-300 ease-in-out"
        style={{ width: `${value}%` }}
      />
    </div>
  );
}
