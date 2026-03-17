import type { ITarea } from "../../types/Interfaces"

interface TareaProps {
    data: ITarea
}

export default function Tarea({data}: TareaProps) {
  return (
    <div className="border border-[#F4E9E9] bg-[#E8DCC4]/35 w-full rounded-md flex flex-row justify-between px-2 py-4">
        <div className="flex flex-col gap-2">
            <h3>{data.title}</h3>
            <div className="flex flex-row gap-4 text-primary-900 font-bold">
                <div className="bg-state-error px-2 rounded-md">
                    <span>{data.dificultad}</span>
                </div>
                <div className="bg-state-info px-2 rounded-md">
                    <span>+{data.recompensa}XP</span>
                </div>
            </div>
        </div>
        <input id="tareaRealizada" type="checkbox" />
    </div>
  )
}