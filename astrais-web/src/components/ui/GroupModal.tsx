import type { ITarea } from "../../types/Interfaces";
import ButtonComplete from "./ButtonComplete";
import Task from "./Task";

export default function XP() {
    const tarea: ITarea = {
        title: "Estudiar TypeScript",
        dificultad: "HARD",
        recompensa: 50
    };

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
        <div>
            <Task data={tarea} />
        </div>
    </div>
  )
}