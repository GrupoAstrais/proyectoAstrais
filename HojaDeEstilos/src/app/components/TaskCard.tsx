import { Star } from 'lucide-react';

export function TaskCard() {
  return (
    <div className="border border-[#11161D] rounded-xl bg-[#0D1117] overflow-hidden">
      <div className="p-4">
        <div className="flex items-start justify-between mb-3">
          <div className="flex-1">
            <h4 className="font-semibold text-base mb-1 text-[#F8FAFC]">Completar diseno espacial</h4>
            <p className="text-sm text-[#D1D5DB]">Finalizar mockups de la galaxia</p>
          </div>
        </div>
        
        <div className="flex gap-2">
          <span className="px-2 py-1 text-xs bg-[#F43F5E]/20 border border-[#F43F5E] rounded text-[#F43F5E]">HARD</span>
          <span className="px-2 py-1 text-xs bg-[#8B5CF6]/20 border border-[#8B5CF6] rounded text-[#8B5CF6]">+50 XP</span>
        </div>
      </div>
    </div>
  );
}
