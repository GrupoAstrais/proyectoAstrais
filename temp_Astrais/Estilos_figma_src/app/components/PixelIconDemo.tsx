import { Square } from 'lucide-react';

export function PixelIconDemo() {
  return (
    <div className="border-4 border-[#1E1E2E] rounded-lg p-6 bg-gradient-to-br from-[#F9FAFB] to-[#F3F4F6]">
      <h4 className="font-['Press_Start_2P'] text-xs mb-6 text-center">Adaptación Pixel Art</h4>
      
      <div className="grid md:grid-cols-2 gap-6">
        {/* Original Icon */}
        <div className="border-2 border-[#E5E7EB] rounded-lg p-4 bg-white">
          <div className="text-center mb-3">
            <span className="text-xs font-['Space_Grotesk'] font-medium text-gray-600">Original (Vector)</span>
          </div>
          <div className="flex justify-center mb-4">
            <div className="w-20 h-20 bg-gradient-to-br from-[#E5E7EB] to-[#D1D5DB] rounded flex items-center justify-center">
              <Square className="w-12 h-12 text-[#5865F2]" strokeWidth={2} />
            </div>
          </div>
          <div className="text-xs text-gray-600 font-['Space_Grotesk'] space-y-1">
            <p>✗ Bordes suaves</p>
            <p>✗ Anti-aliasing</p>
            <p>✗ Stroke: 2px</p>
          </div>
        </div>

        {/* Pixel Art Adapted */}
        <div className="border-4 border-[#5865F2] rounded-lg p-4 bg-white shadow-[4px_4px_0px_0px_rgba(88,101,242,0.2)]">
          <div className="text-center mb-3">
            <span className="text-xs font-['Space_Grotesk'] font-semibold text-[#5865F2]">Pixel Art Style ✨</span>
          </div>
          <div className="flex justify-center mb-4">
            <div className="w-20 h-20 bg-gradient-to-br from-[#0F3547] to-[#1E4A63] border-4 border-[#5865F2] rounded flex items-center justify-center shadow-[0_0_12px_rgba(88,101,242,0.3)]">
              <Square className="w-12 h-12 text-white" strokeWidth={3} style={{ shapeRendering: 'crispEdges' }} />
            </div>
          </div>
          <div className="text-xs font-['Space_Grotesk'] space-y-1">
            <p className="text-[#10B981]">✓ Bordes sharp</p>
            <p className="text-[#10B981]">✓ crispEdges CSS</p>
            <p className="text-[#10B981]">✓ Stroke: 3-4px</p>
          </div>
        </div>
      </div>

      <div className="mt-6 p-4 bg-[#FEF3C7] border-2 border-[#F59E0B] rounded">
        <p className="text-xs font-['Space_Grotesk'] text-gray-800">
          <strong className="text-[#92400E]">Tip:</strong> Usar <code className="px-1 py-0.5 bg-white border border-gray-300 rounded text-xs font-mono">style={`{shapeRendering: 'crispEdges'}`}</code> y aumentar strokeWidth a 3-4px para efecto pixel art.
        </p>
      </div>
    </div>
  );
}