import Dificultad from "./Dificultad";

interface ModalProps {
    onPress: (opcion: string) => void
}

export default function Modal({onPress} : ModalProps) {
  return (
    <div className="flex flex-col font-['Space_Grotesk'] h-auto w-1/5 bg-secondary-500 rounded-md p-4 gap-3 border-2 border-accent-beige-300">
        <h1 className="font-['Press_Start_2P']">Editar tarea</h1>
        <div className="bg-accent-beige-300 rounded-md py-4 px-2">
            <input className="text-primary-900" id="nombre" placeholder="Nombre" />
        </div>
        <div className="flex flex-row justify-around bg-accent-beige-300  py-4 px-2 rounded-md">
            <Dificultad dificultad={"EASY"} />
            <Dificultad dificultad={"HARD"} />
            <Dificultad dificultad={"MEDIUM"} />
        </div>
        <div className="flex flex-row justify-around bg-accent-beige-300  py-4 px-2 rounded-md">
            <div className="bg-state-success text-primary-900 rounded-xs shadow-xs shadow-primary-900 px-2">
                <p className="font-bold">Hábito</p>
            </div>
            <div className="bg-state-success text-primary-900 rounded-xs shadow-xs shadow-primary-900 px-2">
                <p className="font-bold">Diaria</p>
            </div>
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