interface NotificationModalProps {
    xp: number
    ludiones: number
}
export default function NotificationModal({xp, ludiones} : NotificationModalProps) {
    return (
    <div className="font-['Space_Grotesk'] flex flex-col gap-1 mx-2 bg-[var(--astrais-panel-bg)] backdrop-blur-sm p-3.5 shadow-[0_20px_58px_color-mix(in_srgb,var(--astrais-background)_52%,transparent)] text-white rounded-lg border border-white/15">
            <p className="text-sm font-semibold">Tarea completada</p>
            <p className="text-sm">+{xp} XP</p>
            <p className="text-sm">+{ludiones} Ludiones</p>
        </div>
  )
}
