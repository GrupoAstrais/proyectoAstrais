import { Star } from 'lucide-react';

export function TaskCard() {
  return (
    <div className="border-4 border-[#1E1E2E] rounded-lg bg-white shadow-[6px_6px_0px_0px_rgba(30,30,46,1)] overflow-hidden">
      <div className="p-4">
        <div className="flex items-start justify-between mb-3">
          <div className="flex-1">
            <h4 className="font-['Space_Grotesk'] font-semibold text-base mb-1">Completar diseño espacial</h4>
            <p className="text-sm text-gray-600">Finalizar mockups de la galaxia</p>
          </div>
        </div>
        
        <div className="flex gap-2">
          <span className="px-2 py-1 text-xs bg-[#FF6B9D] border-2 border-[#fc266e] rounded font-medium text-[#78042b]">HARD</span>
          <span className="px-2 py-1 text-xs bg-[#FEF3C7] border-2 border-[#F59E0B] rounded font-medium text-[#92400E]">+50</span>
        </div>
      </div>
    </div>
  );
}