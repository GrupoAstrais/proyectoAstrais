import { useState } from "react";
import DifficultyModal from "../ui/DifficultyModal";
import TaskType from "../ui/TaskType";
import DiaryHabit from "../ui/DiaryHabit";

interface ModalProps {
    onPress: (opcion: string) => void
}

export default function Modal({onPress} : ModalProps) {
    const [active, setActive] = useState<string>("");
    
    const handleActive = (ac: string) => {
        setActive(ac);
    }

    const [, setIsComposed] = useState<boolean>(false);

    const handleCompuesta = (ac: boolean) => {
        setIsComposed(ac);
    }

    const [isDiary, setIsDiary] = useState<string>("");

    const handleDiary = (ac: string) => {
        setIsDiary(ac);
    }
        
  return (
    <div className="flex flex-col font-['Space_Grotesk'] h-auto w-1/2 lg:w-1/4 bg-secondary-500 rounded-md p-4 gap-3 border-2 border-accent-beige-300">
        <h1 className="font-['Press_Start_2P']">Editar tarea</h1>
        <div className="bg-accent-beige-300 rounded-md py-4 px-2">
            <input className="text-primary-900" id="nombre" placeholder="Nombre" />
        </div>
        <div className="flex flex-row justify-around bg-accent-beige-300  py-4 px-2 rounded-md">
            <DifficultyModal handleActive={handleActive} esOtroActivo={active} dificultad={"EASY"} />
            <DifficultyModal handleActive={handleActive} esOtroActivo={active}  dificultad={"MEDIUM"} />
            <DifficultyModal handleActive={handleActive} esOtroActivo={active}  dificultad={"HARD"} />
        </div>
        <div className="flex flex-row justify-around bg-accent-beige-300  py-4 px-2 rounded-md">
            <DiaryHabit handleActive={handleDiary} titulo="Hábito" esOtroActivo={isDiary} />
            <DiaryHabit handleActive={handleDiary} titulo="Diaria" esOtroActivo={isDiary} />
        </div>
        <div className="flex flex-row justify-around bg-accent-beige-300  py-4 px-2 rounded-md">
            <TaskType handleActive={handleCompuesta}/>
        </div>
        <div className="flex bg-accent-beige-300 rounded-md py-4 px-2">
            <input className="text-primary-900" id="tags" placeholder="Tags"/>
        </div>
        <div className="flex bg-accent-beige-300 rounded-md py-4 px-2">
            <p>contador en desarrollo</p>
        </div>
        <div className="flex flex-row justify-around">
            <button onClick={() => onPress("Confirmar")} className="bg-state-success p-2 rounded-md border border-primary-900 text-[#00371A] font-bold">Confirmar</button>
            <button onClick={() => onPress("Cancelar")} className="bg-state-error p-2 rounded-md border border-primary-900 text-[#460018] font-bold">Cancelar</button>
       </div>
    </div>
  )
}
