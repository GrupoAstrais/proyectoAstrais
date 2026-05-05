export function XPBar() {
  return (
    <div className="border border-[#11161D] rounded-xl bg-[#0D1117] p-4">
      <div className="flex justify-between items-center mb-2">
        <span className="font-mono text-xs text-[#8B5CF6]">Nivel 12</span>
        <span className="font-mono text-xs text-[#D1D5DB]">850/1000 XP</span>
      </div>
      
      <div className="relative h-8 bg-[#1A1D2D] border border-[#11161D] rounded-full overflow-hidden">
        <div 
          className="absolute inset-0 bg-gradient-to-r from-[#8B5CF6] to-[#38BDF8] transition-all duration-300"
          style={{ width: '85%' }}
        >
          <div className="absolute inset-0 bg-gradient-to-b from-white/20 to-transparent" />
        </div>
      </div>
      
      <div className="flex justify-between items-center mt-2">
        <span className="text-xs text-[#D1D5DB]">Siguiente: <span className="font-semibold text-[#F8FAFC]">Navegante Cosmico</span></span>
        <span className="text-xs font-mono text-[#10B981]">+150 XP</span>
      </div>
    </div>
  );
}
