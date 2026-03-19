import React from "react";
import type { ITarea } from "../../types/Interfaces"
import Dificultad from "./Difficulty"
import XP from "./xp"

interface TareaProps {
    data: ITarea
}

export default function Task({data}: TareaProps) {


    const [checked, setChecked] = React.useState<boolean>(false);

    const checkedHandle = () => {
        setChecked(!checked);
    }

  return (
    <div className={`border border-[#F4E9E9] font-['Space_Grotesk'] ${checked ? 'bg-[#918C84]/55 line-through decoration-primary-900 text-white/50' : 'bg-accent-beige-300/35' }  w-full rounded-md flex flex-row justify-between px-2 py-4`}>
        <div className="flex flex-col gap-2">
            <h3>{data.title}</h3>
            <div className="flex flex-row gap-4 text-primary-900 font-bold">
                <Dificultad dificultad={data.dificultad} />
                <XP recompensa={data.recompensa} />
            </div>
        </div>
        <input id="tareaRealizada" checked={checked} onChange={checkedHandle} type="checkbox" className="accent-primary-700" />
    </div>
  )
}