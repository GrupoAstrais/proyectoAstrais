import { useState } from 'react';

interface ButtonShowcaseProps {
  variant: 'primary' | 'secondary' | 'tertiary';
  label: string;
}

export function ButtonShowcase({ variant, label }: ButtonShowcaseProps) {
  const [state, setState] = useState<'default' | 'hover' | 'pressed' | 'disabled'>('default');

  const getButtonStyles = () => {
    const baseStyles = "px-6 py-3 font-mono text-sm transition-all duration-150 rounded-lg cursor-pointer select-none";
    
    if (variant === 'primary') {
      if (state === 'disabled') return `${baseStyles} bg-[#6B7280] text-[#D1D5DB] cursor-not-allowed`;
      if (state === 'pressed') return `${baseStyles} bg-[#7C3AED] text-white scale-95`;
      if (state === 'hover') return `${baseStyles} bg-[#9B6CFF] text-white`;
      return `${baseStyles} bg-[#8B5CF6] text-white`;
    }
    
    if (variant === 'secondary') {
      if (state === 'disabled') return `${baseStyles} bg-[#6B7280] text-[#D1D5DB] cursor-not-allowed`;
      if (state === 'pressed') return `${baseStyles} bg-[#0284C7] text-white scale-95`;
      if (state === 'hover') return `${baseStyles} bg-[#7DD3FC] text-[#0D1117]`;
      return `${baseStyles} bg-[#38BDF8] text-[#0D1117]`;
    }
    
    // tertiary
    if (state === 'disabled') return `${baseStyles} bg-transparent text-[#6B7280] cursor-not-allowed border border-[#6B7280]`;
    if (state === 'pressed') return `${baseStyles} bg-[#10B981]/20 text-[#10B981] border border-[#10B981] scale-95`;
    if (state === 'hover') return `${baseStyles} bg-[#10B981]/10 text-[#10B981] border border-[#10B981]`;
    return `${baseStyles} bg-transparent text-[#10B981] border border-[#10B981]`;
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex justify-center p-8 bg-[#0D1117] rounded-lg">
        <button className={getButtonStyles()}>
          {label}
        </button>
      </div>
      <div className="flex gap-2 justify-center">
        <button 
          onClick={() => setState('default')}
          className={`px-3 py-1 text-xs rounded ${state === 'default' ? 'bg-[#8B5CF6] text-white' : 'bg-[#1A1D2D] text-[#D1D5DB]'}`}
        >
          Default
        </button>
        <button 
          onClick={() => setState('hover')}
          className={`px-3 py-1 text-xs rounded ${state === 'hover' ? 'bg-[#8B5CF6] text-white' : 'bg-[#1A1D2D] text-[#D1D5DB]'}`}
        >
          Hover
        </button>
        <button 
          onClick={() => setState('pressed')}
          className={`px-3 py-1 text-xs rounded ${state === 'pressed' ? 'bg-[#8B5CF6] text-white' : 'bg-[#1A1D2D] text-[#D1D5DB]'}`}
        >
          Pressed
        </button>
        <button 
          onClick={() => setState('disabled')}
          className={`px-3 py-1 text-xs rounded ${state === 'disabled' ? 'bg-[#8B5CF6] text-white' : 'bg-[#1A1D2D] text-[#D1D5DB]'}`}
        >
          Disabled
        </button>
      </div>
    </div>
  );
}
