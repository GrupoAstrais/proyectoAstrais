export function XPBar() {
  return (
    <div className="border-4 border-[#1E1E2E] rounded-lg bg-white shadow-[6px_6px_0px_0px_rgba(30,30,46,1)] p-4">
      <div className="flex justify-between items-center mb-2">
        <span className="font-['Press_Start_2P'] text-xs text-[#5865F2]">Nivel 12</span>
        <span className="font-['Press_Start_2P'] text-xs text-gray-600">850/1000 XP</span>
      </div>
      
      <div className="relative h-8 bg-[#E5E7EB] border-4 border-[#1E1E2E] rounded-full overflow-hidden shadow-inner">
        <div 
          className="absolute inset-0 bg-gradient-to-r from-[#8B5CF6] via-[#5865F2] to-[#4ECDC4] border-r-4 border-[#1E1E2E] transition-all duration-300"
          style={{ width: '85%' }}
        >
          <div className="absolute inset-0 bg-gradient-to-b from-white/30 to-transparent" />
          <div className="absolute inset-0 animate-pulse bg-gradient-to-r from-transparent via-white/20 to-transparent" />
        </div>
      </div>
      
      <div className="flex justify-between items-center mt-2">
        <span className="text-xs text-gray-500 font-['Space_Grotesk']">Siguiente: <span className="font-semibold">Navegante Cósmico</span></span>
        <span className="text-xs font-['Press_Start_2P'] text-[#22A39A]">+150 XP</span>
      </div>
    </div>
  );
}