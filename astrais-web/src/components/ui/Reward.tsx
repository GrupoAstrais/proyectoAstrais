interface RewardProps {
    recompensa: number
}
export default function Reward({recompensa} : RewardProps) {
    return (
    <div className="text-white rounded-xs font-['Press_Start_2P'] shadow-[4px_4px_0px_0px_rgba(255,217,61,1)] px-2 animate-pulse">
        <p className="font-bold">RECLAMAR {recompensa}</p>
    </div>
  )
}