import { Star } from 'lucide-react';

export function IconSizeDemo() {
  const sizes = [
    { name: 'SM', size: 16, stroke: 2, token: 'Icon/Size/SM' },
    { name: 'MD', size: 24, stroke: 2, token: 'Icon/Size/MD' },
    { name: 'LG', size: 32, stroke: 2, token: 'Icon/Size/LG' },
    { name: 'XL', size: 48, stroke: 2, token: 'Icon/Size/XL' },
  ];

  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
      {sizes.map((item) => (
        <div key={item.name} className="border border-[#11161D] rounded-lg p-6 bg-[#0D1117] flex flex-col items-center gap-3">
          <div className="w-20 h-20 bg-[#1A1D2D] border border-[#10B981]/30 rounded-lg flex items-center justify-center">
            <Star 
              className="text-[#F8FAFC]" 
              size={item.size} 
              strokeWidth={item.stroke}
            />
          </div>
          <div className="text-center">
            <div className="font-mono text-xs mb-1 text-[#F8FAFC]">{item.name}</div>
            <div className="text-sm font-semibold text-[#D1D5DB]">{item.size}px</div>
            <div className="text-xs text-[#6B7280]">Stroke: {item.stroke}px</div>
            <div className="font-mono text-xs text-[#6B7280] mt-1">{item.token}</div>
          </div>
        </div>
      ))}
    </div>
  );
}
