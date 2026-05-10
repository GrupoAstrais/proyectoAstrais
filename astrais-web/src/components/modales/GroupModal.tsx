import type { ITarea } from "../../types/Interfaces";
import ButtonComplete from "../ui/ButtonComplete";
import Task from "../ui/Task";

interface GroupModalProps {
    data: ITarea[]
    groupName: string
    activeCompleted: boolean
    activePending: boolean
    handleActiveFilter: (title: string) => void
    handleToggleComplete: (taskId: number) => void | Promise<void>
    handleToggleSubtask: (taskId: number, subtaskId: number) => void | Promise<void>
}

// Presenta las tareas de un grupo junto a sus filtros de estado.
export default function GroupModal({ data, groupName, activeCompleted, activePending, handleActiveFilter, handleToggleComplete, handleToggleSubtask }: GroupModalProps) {
    return (
        <div className="font-['Space_Grotesk'] flex flex-col gap-2 mx-2">
            {/* Barra de filtros del grupo */}
            <div className="tabs-scroll items-center pb-1">
                <div className="shrink-0 bg-accent-beige-300/35 rounded-md px-4 py-2 font-bold font-['Press_Start_2P']">
                    <h2>{groupName}</h2>
                </div>

                <div className="shrink-0 rounded-full px-2 border border-white/15 bg-[color-mix(in_srgb,var(--astrais-background)_72%,transparent)]">
                    <p className="text-white">Filtrar:</p>
                </div>

                <ButtonComplete title="Completadas" active={activeCompleted} handleActive={handleActiveFilter} />
                <ButtonComplete title="Pendientes" active={activePending} handleActive={handleActiveFilter} />
            </div>

            <div className="flex flex-col gap-2">
                {/* Tareas del grupo: cada tarea viene del array filtrado por el padre */}
                {data.length === 0 ? (
                    <p className="text-white/55 italic text-center py-4">No hay tareas</p>
                ) : (
                    data.map((t, i) => (
                        <Task key={t.id ?? i} data={t} onComplete={handleToggleComplete} onToggleSubtask={handleToggleSubtask} />
                    )))}
            </div>
        </div>
    )
}
