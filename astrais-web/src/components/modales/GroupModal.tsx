import type { ITarea } from "../../types/Interfaces";
import ButtonComplete from "../ui/ButtonComplete";
import Task from "../ui/Task";

interface GroupModalProps {
    data: ITarea[]
}

export default function GroupModal({data} : GroupModalProps) {
    return (
    <div className="font-['Space_Grotesk'] flex flex-col gap-2 mx-2">
        <div className="flex flex-row gap-2 items-center">
            <div className="bg-accent-beige-300/35 rounded-md px-4 py-2 font-bold font-['Press_Start_2P']">
                <h2>Astraïs</h2>
            </div>

            <div className="rounded-full px-2 border border-white bg-black">
                <p className="text-white">Filtrar:</p>
            </div>

            <ButtonComplete title="Completadas"/>
            <ButtonComplete title="Pendientes"/>
        </div>
        
        <div className="flex flex-col gap-2">
            {data.length === 0 ? (
                <p className="text-gray-400 italic text-center py-4">No hay tareas</p>
            ) : (
                data.map((t, i) => (
            <Task key={i} data={t}  />
            )))}
        </div>
    </div>
  )
}