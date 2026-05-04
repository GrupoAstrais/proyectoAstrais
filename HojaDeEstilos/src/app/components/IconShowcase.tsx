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
    <div className="border border-[#11161D] rounded-lg p-4 bg-[#0D1117] hover:bg-[#1A1D2D] transition-colors">
      <div className="flex flex-col items-center gap-3">
        <div className="w-16 h-16 bg-[#1A1D2D] border border-[#8B5CF6]/30 rounded-lg flex items-center justify-center">
          <div className="text-[#F8FAFC]" style={{ width: size, height: size }}>
            {icon}
          </div>
        </div>
        
        <div className="text-center w-full">
          <div className="font-semibold text-sm mb-1 truncate text-[#F8FAFC]">{name}</div>
          <div className="text-xs text-[#D1D5DB] font-mono mb-1">{size}px</div>
          <div className="text-xs text-[#6B7280] font-mono truncate">{token}</div>
          <div className={`inline-block mt-2 px-2 py-0.5 rounded text-xs font-medium ${
            library === 'Lucide' 
              ? 'bg-[#8B5CF6] text-white' 
              : 'bg-[#38BDF8] text-[#0D1117]'
          }`}>
            {library}
          </div>
        </div>
      </div>
    </div>
  );
}
