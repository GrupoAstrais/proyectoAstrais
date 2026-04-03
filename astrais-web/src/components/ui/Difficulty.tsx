
interface DifProps {
    dificultad: string
}

export default function Dificultad({dificultad} : DifProps) {
    return (
    <div className={`text-primary-900 rounded-xs shadow-xs shadow-primary-900 font-['Space_Grotesk'] px-2 ${dificultad == "EASY" ? 'bg-state-success' : dificultad == "MEDIUM" ? 'bg-state-warning' : 'bg-state-error'}`}>
        <p className="font-bold">{dificultad}</p>
    </div>
  )
}