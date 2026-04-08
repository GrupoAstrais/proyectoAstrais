import React from "react";
import type { ITarea } from "../../types/Interfaces";
import Dificultad from "./Difficulty";
import XP from "./xp";

interface TareaProps {
  data: ITarea;
  onComplete?: (taskId: string) => void;
  onToggleSubtask?: (taskId: string, subtaskId: string) => void;
}

export default function Task({ data, onComplete, onToggleSubtask }: TareaProps) {
  const [checked, setChecked] = React.useState<boolean>(data.completed ?? false);
  const [localSubtasks, setLocalSubtasks] = React.useState(data.subtasks);
  const subtasks = onToggleSubtask ? data.subtasks : localSubtasks;
  const allSubtasksCompleted =
    data.isComposed &&
    subtasks.length > 0 &&
    subtasks.every((subtask) => subtask.completed);
  const taskChecked =
    allSubtasksCompleted || (data.completed !== undefined ? data.completed : checked);

  React.useEffect(() => {
    setChecked(data.completed ?? false);
  }, [data.completed]);

  React.useEffect(() => {
    setLocalSubtasks(data.subtasks);
  }, [data.subtasks]);

  const checkedHandle = (e?: React.ChangeEvent<HTMLInputElement>) => {
    if (e) {
      e.stopPropagation();
    }

    if (onComplete && data.id) {
      onComplete(data.id);
      return;
    }

    setChecked(!taskChecked);
  };

  const ClickHandle = () => {
    if (onComplete && data.id) {
      onComplete(data.id);
      return;
    }

    const newChecked = !taskChecked;
    setChecked(newChecked);
    
    if(localSubtasks.length > 0) {
      if(newChecked) {
        const updated = localSubtasks.map(t => ({
          ...t,
          completed: true
        }));

        setLocalSubtasks(updated);

      } else {
        const updated = localSubtasks.map(t => ({
          ...t,
          completed: false
        }));

        setLocalSubtasks(updated);
      }
    }
  };

  const subtaskHandle = (subtaskId: string, e: React.ChangeEvent<HTMLInputElement>) => {
    e.stopPropagation();

    if (onToggleSubtask && data.id) {
      onToggleSubtask(data.id, subtaskId);
      return;
    }

    setLocalSubtasks((prevSubtasks) => {
      const nextSubtasks = prevSubtasks.map((subtask) =>
        subtask.id === subtaskId
          ? { ...subtask, completed: !subtask.completed }
          : subtask
      );

      setChecked(nextSubtasks.length > 0 && nextSubtasks.every((subtask) => subtask.completed));

      return nextSubtasks;
    });
  };

  return (
    <div onClick={ClickHandle} className={`border border-[#F4E9E9]/15 font-['Space_Grotesk'] relative backdrop-blur-sm
        ${taskChecked
          ? "bg-[#918C84]/55 line-through decoration-primary-900 text-white/50"
          : "bg-accent-beige-300/35"
      } w-full rounded-md flex flex-row justify-between px-2 py-4`}
    >
        {/* Tags en esquina superior derecha son opcionales */}
        {data.tags && (
            <div className="absolute top-1 right-1 flex flex-wrap gap-1">
            {data.tags.map((tag, id) => (
                <span
                key={id}
                className={`text-xs px-1.5 py-0.5 rounded ${
                    tag.color
                    ? `${tag.color} text-white`
                    : "bg-gray-200 text-primary-900"
                }`}
                >
                {tag.name}
                </span>
            ))}
            </div>
        )}

      {/* si existen subtareas se dibuja esto*/}
        {(localSubtasks.length > 0 && data.isComposed == true) && (
            <div className="flex flex-col w-full">
                <div className="flex flex-row items-center gap-2 pb-2 border-b">
                    <h2 className="text-lg">{data.title}</h2>
                    <div className="flex flex-row gap-4 text-primary-900 font-bold">
                        <Dificultad dificultad={data.dificultad} />
                        <XP recompensa={data.recompensa} />
                    </div>
                </div>
                {localSubtasks.map((s) => (
                <div key={s.id} className="flex flex-row justify-between">
                    <h3 className="px-2">{s.name}</h3>
                    <input id="subtareaRealizada" checked={s.completed} onChange={(e) => subtaskHandle(s.id, e)} onClick={(e) => e.stopPropagation()} type="checkbox" className="accent-primary-700"/>
                </div>
                ))}
            </div>
        )}
            
            {/* si no existen subtareas */}
        {(data.isComposed == false || subtasks.length == 0) && (
            <>
                <div className="flex flex-col gap-2">
                    <h3>{data.title}</h3>
                    <div className="flex flex-row gap-4 text-primary-900 font-bold">
                        <Dificultad dificultad={data.dificultad} />
                        <XP recompensa={data.recompensa} />
                    </div>
                </div>
                <input id="tareaRealizada" checked={taskChecked} onChange={checkedHandle} onClick={(e) => e.stopPropagation()} type="checkbox" className="accent-primary-700"/>
            </>
        )}

    </div>
  );
}
