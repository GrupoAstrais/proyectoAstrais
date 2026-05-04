interface NotificationModalProps {
    xp: number
    ludiones: number
}
export default function NotificationModal({xp, ludiones} : NotificationModalProps) {
    return (
        <div className="font-['Space_Grotesk'] flex flex-col gap-1 mx-2 bg-[linear-gradient(160deg,#0a101ff2,#3c1480d9,#142f42e6)] backdrop-blur-sm p-3.5 shadow-[0_20px_58px_rgba(7,12,24,0.5)] text-white rounded-lg border border-white/15">
            <p className="text-sm font-semibold">Tarea completada</p>
            <p className="text-sm">+{xp} XP</p>
            <p className="text-sm">+{ludiones} Ludiones</p>
        </div>
  )
}
