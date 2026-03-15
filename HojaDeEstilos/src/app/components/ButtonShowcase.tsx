import { useState } from 'react';

interface ButtonShowcaseProps {
  variant: 'primary' | 'secondary' | 'reward';
  label: string;
}

export function ButtonShowcase({ variant, label }: ButtonShowcaseProps) {
  const [state, setState] = useState<'default' | 'hover' | 'pressed' | 'disabled'>('default');

  const getButtonStyles = () => {
    const baseStyles = "px-6 py-3 font-['Press_Start_2P'] text-xs transition-all duration-100 border-4 cursor-pointer select-none";
    
    if (variant === 'primary') {
      if (state === 'disabled') return `${baseStyles} bg-gray-400 border-gray-600 text-gray-600 cursor-not-allowed shadow-none`;
      if (state === 'pressed') return `${baseStyles} bg-[#4752C4] border-[#1E1E2E] text-white shadow-none translate-y-1`;
      if (state === 'hover') return `${baseStyles} bg-[#6D75FF] border-[#1E1E2E] text-white shadow-[6px_6px_0px_0px_rgba(30,30,46,1)]`;
      return `${baseStyles} bg-[#5865F2] border-[#1E1E2E] text-white shadow-[4px_4px_0px_0px_rgba(30,30,46,1)]`;
    }
    
    if (variant === 'secondary') {
      if (state === 'disabled') return `${baseStyles} bg-gray-200 border-gray-400 text-gray-500 cursor-not-allowed shadow-none`;
      if (state === 'pressed') return `${baseStyles} bg-[#C0B08C] border-[#1E1E2E] text-[#1E1E2E] shadow-none translate-y-1`;
      if (state === 'hover') return `${baseStyles} bg-[#D4C4A8] border-[#1E1E2E] text-[#1E1E2E] shadow-[6px_6px_0px_0px_rgba(30,30,46,1)]`;
      return `${baseStyles} bg-[#E8DCC4] border-[#1E1E2E] text-[#1E1E2E] shadow-[4px_4px_0px_0px_rgba(30,30,46,1)]`;
    }
    
    // reward
    if (state === 'disabled') return `${baseStyles} bg-gray-300 border-gray-500 text-gray-600 cursor-not-allowed shadow-none`;
    if (state === 'pressed') return `${baseStyles} bg-[#6D28D9] border-[#FFD700] text-white shadow-[0_0_12px_rgba(255,215,0,0.4)] translate-y-1`;
    if (state === 'hover') return `${baseStyles} bg-[#9B6CFF] border-[#FFD700] text-white shadow-[0_0_20px_rgba(255,215,0,0.6),4px_4px_0px_0px_rgba(30,30,46,1)]`;
    return `${baseStyles} bg-[#8B5CF6] border-[#FFD700] text-white shadow-[0_0_16px_rgba(255,215,0,0.5),4px_4px_0px_0px_rgba(30,30,46,1)]`;
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex justify-center p-8 bg-gradient-to-br from-[#0F3547] to-[#1E4A63] rounded">
        <button className={getButtonStyles()}>
          {label}
        </button>
      </div>
      <div className="flex gap-2 justify-center">
        <button 
          onClick={() => setState('default')}
          className={`px-3 py-1 text-xs rounded ${state === 'default' ? 'bg-[#5865F2] text-white' : 'bg-gray-200'}`}
        >
          Default
        </button>
        <button 
          onClick={() => setState('hover')}
          className={`px-3 py-1 text-xs rounded ${state === 'hover' ? 'bg-[#5865F2] text-white' : 'bg-gray-200'}`}
        >
          Hover
        </button>
        <button 
          onClick={() => setState('pressed')}
          className={`px-3 py-1 text-xs rounded ${state === 'pressed' ? 'bg-[#5865F2] text-white' : 'bg-gray-200'}`}
        >
          Pressed
        </button>
        <button 
          onClick={() => setState('disabled')}
          className={`px-3 py-1 text-xs rounded ${state === 'disabled' ? 'bg-[#5865F2] text-white' : 'bg-gray-200'}`}
        >
          Disabled
        </button>
      </div>
    </div>
  );
}
