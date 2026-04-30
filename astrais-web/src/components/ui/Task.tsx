import type React from "react";
import type { ITarea } from "../../types/Interfaces";
import { isTaskCompleted } from "../../data/Api";
import Dificultad from "./Difficulty";
import XP from "./xp";

interface TareaProps {
  data: ITarea;
  subtasks?: ITarea[];
  onComplete?: (taskId: number) => void | Promise<void>;
  onToggleSubtask?: (taskId: number, subtaskId: number) => void | Promise<void>;
  onToggleConfig?: (id: number) => void;
}

export default function Task({ onToggleConfig, data, subtasks = [], onComplete, onToggleSubtask }: TareaProps) {
  const hasSubtasks = subtasks.length > 0;
  const allSubtasksCompleted = hasSubtasks && subtasks.every((subtask) => isTaskCompleted(subtask));
  const taskChecked = hasSubtasks ? allSubtasksCompleted : isTaskCompleted(data);

  const checkedHandle = (e?: React.ChangeEvent<HTMLInputElement>) => {
    e?.stopPropagation();
    void onComplete?.(data.id);
  };

  const clickHandle = () => {
    void onComplete?.(data.id);
  };

  const subtaskHandle = (subtaskId: number, e: React.ChangeEvent<HTMLInputElement>) => {
    e.stopPropagation();
    void onToggleSubtask?.(data.id, subtaskId);
  };

  const configHandle = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.stopPropagation();
    onToggleConfig?.(data.id);
  };

  return (
    <div
      onClick={clickHandle}
      className={`relative flex w-full flex-row justify-between rounded-md border border-[#F4E9E9]/15 px-2 py-4 font-['Space_Grotesk'] backdrop-blur-sm ${
        taskChecked
          ? "bg-[#595959]/65 text-white/50 line-through decoration-primary-900"
          : "bg-accent-beige-300/35"
      }`}
    >
      {onToggleConfig && (
        <button type="button" onClick={configHandle} className="absolute top-1 right-1 z-20">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="6" r="2" fill="currentColor" />
            <circle cx="12" cy="12" r="2" fill="currentColor" />
            <circle cx="12" cy="18" r="2" fill="currentColor" />
          </svg>
        </button>
      )}

      {hasSubtasks ? (
        <div className="flex w-full flex-col gap-3">
          <div className="flex flex-row items-start justify-between gap-3 border-b border-white/20 pb-2 pr-8">
            <div className="flex flex-col gap-1">
              <h2 className="text-lg">{data.titulo}</h2>
              {data.descripcion && <p className="text-sm text-white/75">{data.descripcion}</p>}
            </div>

            <div className="flex flex-row gap-4 font-bold text-primary-900">
              <Dificultad dificultad={data.prioridad} />
              <XP recompensa={data.recompensaXp} />
            </div>
          </div>

          <div className="flex flex-col gap-2">
            {subtasks.map((subtask) => (
              <label onClick={(e) => e.stopPropagation()} key={subtask.id} className="flex flex-row items-center justify-between gap-3">
                <span  className={`${!isTaskCompleted(subtask) ? '' : 'text-white/50 line-through decoration-primary-700'}`}>{subtask.titulo}</span>
                <input
                  id={`subtask-${subtask.id}`}
                  checked={isTaskCompleted(subtask)}
                  onChange={(e) => subtaskHandle(subtask.id, e)}
                  onClick={(e) => e.stopPropagation()}
                  type="checkbox"
                  className="accent-primary-700"
                />
              </label>
            ))}
          </div>
        </div>
      ) : (
        <>
          <div className="flex flex-col gap-2 pr-8">
            <h3>{data.titulo}</h3>
            {data.descripcion && <p className="text-sm text-white/75">{data.descripcion}</p>}
            <div className="flex flex-row gap-4 font-bold text-primary-900">
              <Dificultad dificultad={data.prioridad} />
              <XP recompensa={data.recompensaXp} />
            </div>
          </div>
          <input
            id={`task-${data.id}`}
            checked={taskChecked}
            onChange={checkedHandle}
            onClick={(e) => e.stopPropagation()}
            type="checkbox"
            className="accent-primary-700"
          />
        </>
      )}
    </div>
  );
}
