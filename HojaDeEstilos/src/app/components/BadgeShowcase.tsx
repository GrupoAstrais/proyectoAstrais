import { Star, Sparkles, Zap } from 'lucide-react';

export function BadgeShowcase() {
  return (
    <div className="grid grid-cols-3 gap-4">
      <div className="flex flex-col items-center gap-2">
        <div className="w-12 h-12 bg-[#FCD34D] border-4 border-[#92400E] rounded-full flex items-center justify-center shadow-[0_0_12px_rgba(252,211,77,0.5),4px_4px_0px_0px_rgba(30,30,46,1)]">
          <Star className="w-6 h-6 fill-[#92400E] text-[#92400E]" />
        </div>
        <span className="text-xs font-['Press_Start_2P'] text-center">Estrella</span>
      </div>
      
      <div className="flex flex-col items-center gap-2">
        <div className="w-12 h-12 bg-gradient-to-br from-[#C084FC] to-[#9333EA] border-4 border-[#6B21A8] rounded-full flex items-center justify-center shadow-[0_0_12px_rgba(147,51,234,0.5),4px_4px_0px_0px_rgba(30,30,46,1)]">
          <Sparkles className="w-6 h-6 text-white" />
        </div>
        <span className="text-xs font-['Press_Start_2P'] text-center">Nebulosa</span>
      </div>
      
      <div className="flex flex-col items-center gap-2">
        <div className="w-12 h-12 bg-gradient-to-br from-[#4ECDC4] to-[#22A39A] border-4 border-[#0F3547] rounded-full flex items-center justify-center shadow-[0_0_12px_rgba(78,205,196,0.5),4px_4px_0px_0px_rgba(30,30,46,1)]">
          <Zap className="w-6 h-6 fill-white text-white" />
        </div>
        <span className="text-xs font-['Press_Start_2P'] text-center">Cometa</span>
      </div>
    </div>
  );
}
