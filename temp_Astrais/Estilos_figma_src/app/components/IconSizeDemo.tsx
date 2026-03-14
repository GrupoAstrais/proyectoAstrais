import { Star } from 'lucide-react';

export function IconSizeDemo() {
  const sizes = [
    { name: 'SM', size: 16, stroke: 2, token: 'Icon/Size/SM' },
    { name: 'MD', size: 24, stroke: 2.5, token: 'Icon/Size/MD' },
    { name: 'LG', size: 32, stroke: 3, token: 'Icon/Size/LG' },
    { name: 'XL', size: 48, stroke: 3.5, token: 'Icon/Size/XL' },
  ];

  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
      {sizes.map((item) => (
        <div key={item.name} className="border-2 border-[#1E1E2E] rounded-lg p-6 bg-[#F9FAFB] flex flex-col items-center gap-3">
          <div className="w-20 h-20 bg-gradient-to-br from-[#0F3547] to-[#1E4A63] border-4 border-[#4ECDC4] rounded-lg flex items-center justify-center">
            <Star 
              className="text-[#FFD700] fill-[#FFD700]" 
              size={item.size} 
              strokeWidth={item.stroke}
              style={{ shapeRendering: 'crispEdges' }}
            />
          </div>
          <div className="text-center">
            <div className="font-['Press_Start_2P'] text-xs mb-1">{item.name}</div>
            <div className="font-['Space_Grotesk'] text-sm font-semibold text-gray-700">{item.size}px</div>
            <div className="font-['Space_Grotesk'] text-xs text-gray-500">Stroke: {item.stroke}px</div>
            <div className="font-mono text-xs text-gray-400 mt-1">{item.token}</div>
          </div>
        </div>
      ))}
    </div>
  );
}