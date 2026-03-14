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
          <div className="flex items-center gap-1 px-2 py-1 bg-[#FEF3C7] border-2 border-[#F59E0B] rounded">
            <Star className="w-3 h-3 fill-[#F59E0B] text-[#F59E0B]" />
            <span className="text-xs font-['Press_Start_2P'] text-[#92400E]">+50</span>
          </div>
        </div>
        
        <div className="flex items-center gap-2 mb-3">
          <div className="flex-1 h-3 bg-[#E5E7EB] border-2 border-[#1E1E2E] rounded-full overflow-hidden">
            <div className="h-full bg-gradient-to-r from-[#4ECDC4] to-[#22A39A] border-r-2 border-[#1E1E2E]" style={{ width: '60%' }} />
          </div>
          <span className="text-xs font-['Press_Start_2P'] text-gray-600">60%</span>
        </div>
        
        <div className="flex gap-2">
          <span className="px-2 py-1 text-xs bg-[#DBEAFE] border-2 border-[#3B82F6] rounded font-medium">Diseño</span>
          <span className="px-2 py-1 text-xs bg-[#FCE7F3] border-2 border-[#EC4899] rounded font-medium">Alta</span>
        </div>
      </div>
    </div>
  );
}