interface XPProps {
    recompensa: number
}

// Muestra la recompensa de experiencia de una tarea.
export default function XP({recompensa} : XPProps) {
    return (
    <div className="bg-state-info text-primary-900 rounded-xs shadow-xs shadow-primary-900 px-2">
        <p className="font-bold">+{recompensa} xp</p>
    </div>
  )
}
