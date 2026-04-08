import type { ITarea } from "../../types/Interfaces";
import ButtonComplete from "../ui/ButtonComplete";
import Task from "../ui/Task";

interface GroupModalProps {
    data: ITarea[]
    groupName: string
    activeCompleted: boolean
    activePending: boolean
    handleActiveFilter: (title: string) => void
    handleToggleComplete: (taskId: string) => void
    handleToggleSubtask: (taskId: string, subtaskId: string) => void
}

export default function GroupModal({ data, groupName, activeCompleted, activePending, handleActiveFilter, handleToggleComplete, handleToggleSubtask }: GroupModalProps) {
    return (
        <div className="font-['Space_Grotesk'] flex flex-col gap-2 mx-2">
            <div className="flex flex-row gap-2 items-center">
                <div className="bg-accent-beige-300/35 rounded-md px-4 py-2 font-bold font-['Press_Start_2P']">
                    <h2>{groupName}</h2>
                </div>

                <div className="rounded-full px-2 border border-white bg-black">
                    <p className="text-white">Filtrar:</p>
                </div>

                <ButtonComplete title="Completadas" active={activeCompleted} handleActive={handleActiveFilter} />
                <ButtonComplete title="Pendientes" active={activePending} handleActive={handleActiveFilter} />
            </div>

            <div className="flex flex-col gap-2">
                {data.length === 0 ? (
                    <p className="text-gray-400 italic text-center py-4">No hay tareas</p>
                ) : (
                    data.map((t, i) => (
                        <Task key={t.id ?? i} data={t} onComplete={handleToggleComplete} onToggleSubtask={handleToggleSubtask} />
                    )))}
            </div>
        </div>
    )
}
