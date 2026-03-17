interface XPProps {
    recompensa: number
}
export default function XP({recompensa} : XPProps) {
    return (
    <div className="bg-state-info text-primary-900 rounded-xs shadow-xs shadow-primary-900 px-2">
        <p>+{recompensa} xp</p>
    </div>
  )
}