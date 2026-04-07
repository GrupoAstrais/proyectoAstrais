import React from "react";
import type { ITarea } from "../../types/Interfaces";
import Dificultad from "./Difficulty";
import XP from "./xp";

interface TareaProps {
  data: ITarea;
  onComplete?: (taskId: string) => void;
}

export default function Task({ data, onComplete }: TareaProps) {
  const [checked, setChecked] = React.useState<boolean>(data.completed ?? false);
  const taskChecked = data.completed ?? checked;

  React.useEffect(() => {
    setChecked(data.completed ?? false);
  }, [data.completed]);

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

    setChecked(!taskChecked);
  };

  return (
    <div onClick={ClickHandle} className={`border border-[#F4E9E9] font-['Space_Grotesk'] relative 
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

        {(data.subtasks.length > 0 && data.isComposed == true) && (
            <div className="flex flex-col w-full">
                <div className="flex flex-row items-center gap-2 pb-2 border-b">
                    <h2 className="text-lg">{data.title}</h2>
                    <div className="flex flex-row gap-4 text-primary-900 font-bold">
                        <Dificultad dificultad={data.dificultad} />
                        <XP recompensa={data.recompensa} />
                    </div>
                </div>
                {data.subtasks.map((s) => (
                <div key={s.id} className="flex flex-row justify-between">
                    <h3 className="px-2">{s.name}</h3>
                    <input id="subtareaRealizada" checked={s.completed} onClick={(e) => e.stopPropagation()} readOnly type="checkbox" className="accent-primary-700"/>
                </div>
                ))}
            </div>
        )}
            
        {(data.isComposed == false || data.subtasks.length == 0) && (
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
