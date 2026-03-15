import { ReactNode } from 'react';

interface IconShowcaseProps {
  icon: ReactNode;
  name: string;
  token: string;
  library: 'Lucide' | 'Phosphor';
  size: number;
}

export function IconShowcase({ icon, name, token, library, size }: IconShowcaseProps) {
  return (
    <div className="border-2 border-[#1E1E2E] rounded-lg p-4 bg-white hover:bg-[#F9FAFB] transition-colors">
      <div className="flex flex-col items-center gap-3">
        {/* Icon Display */}
        <div className="w-16 h-16 bg-gradient-to-br from-[#0F3547] to-[#1E4A63] border-4 border-[#5865F2] rounded-lg flex items-center justify-center shadow-[4px_4px_0px_0px_rgba(88,101,242,0.3)]">
          <div className="text-white" style={{ width: size, height: size }}>
            {icon}
          </div>
        </div>
        
        {/* Info */}
        <div className="text-center w-full">
          <div className="font-['Space_Grotesk'] font-semibold text-sm mb-1 truncate">{name}</div>
          <div className="text-xs text-gray-500 font-mono mb-1">{size}px</div>
          <div className="text-xs text-gray-400 font-mono truncate">{token}</div>
          <div className={`inline-block mt-2 px-2 py-0.5 rounded text-xs font-medium ${
            library === 'Lucide' 
              ? 'bg-[#5865F2] text-white' 
              : 'bg-[#8B5CF6] text-white'
          }`}>
            {library}
          </div>
        </div>
      </div>
    </div>
  );
}