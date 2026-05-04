import { Star, Sparkles, Zap } from 'lucide-react';

export function BadgeShowcase() {
  return (
    <div className="grid grid-cols-3 gap-4">
      <div className="flex flex-col items-center gap-2">
        <div className="w-12 h-12 bg-[#FCD34D] border-2 border-[#92400E] rounded-full flex items-center justify-center">
          <Star className="w-6 h-6 fill-[#92400E] text-[#92400E]" />
        </div>
        <span className="text-xs font-mono text-center text-[#F8FAFC]">Estrella</span>
      </div>
      
      <div className="flex flex-col items-center gap-2">
        <div className="w-12 h-12 bg-gradient-to-br from-[#C084FC] to-[#9333EA] border-2 border-[#6B21A8] rounded-full flex items-center justify-center">
          <Sparkles className="w-6 h-6 text-white" />
        </div>
        <span className="text-xs font-mono text-center text-[#F8FAFC]">Nebulosa</span>
      </div>
      
      <div className="flex flex-col items-center gap-2">
        <div className="w-12 h-12 bg-gradient-to-br from-[#4ECDC4] to-[#22A39A] border-2 border-[#0F3547] rounded-full flex items-center justify-center">
          <Zap className="w-6 h-6 fill-white text-white" />
        </div>
        <span className="text-xs font-mono text-center text-[#F8FAFC]">Cometa</span>
      </div>
    </div>
  );
}
