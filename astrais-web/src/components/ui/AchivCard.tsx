import { LiquidGlass } from "@liquidglass/react"

export default function AchivCard() {
  return (
    <div className="bg-linear-to-b from-secondary-700/60 to-secondary-600/50 rounded-xl p-5 border border-secondary-600/50 shadow-lg backdrop-blur-sm">
        <div className="flex flex-col border-b items-center justify-between mb-3">
            <span className="text-sm font-medium text-purple-200">Racha</span>
        </div>
        <div className="flex gap-3">
            <div className="w-10 h-10 rounded-md bg-secondary-500/30"></div>
            <div className="w-10 h-10 rounded-md bg-secondary-500/30"></div>
            <div className="w-10 h-10 rounded-md  flex items-center justify-center">
            <LiquidGlass className="rounded-md">
                <svg xmlns="http://www.w3.org/2000" className="h-4 w-4 text-purple-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 04h8z" />
                </svg>
                </LiquidGlass>
            </div>
        </div>
                        { /*
                <button className="px-3 py-1.5 text-xs font-bold text-yellow-300 border-2 border-yellow-400 rounded-lg hover:bg-yellow-400/20 transition-colors">
                    Reclamar
                    
                </button>
                */}
    </div>
  )
}