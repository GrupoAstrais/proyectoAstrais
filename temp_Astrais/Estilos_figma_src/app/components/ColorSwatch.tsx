interface ColorSwatchProps {
  name: string;
  hex: string;
  token?: string;
  large?: boolean;
}

export function ColorSwatch({ name, hex, token, large = false }: ColorSwatchProps) {
  return (
    <div className="flex flex-col gap-2">
      <div 
        className={`${large ? 'h-24' : 'h-16'} rounded border-2 border-[#1E1E2E] shadow-[4px_4px_0px_0px_rgba(30,30,46,1)]`}
        style={{ backgroundColor: hex }}
      />
      <div className="flex flex-col gap-0.5">
        <span className="text-sm font-medium">{name}</span>
        <span className="text-xs text-gray-600 font-mono">{hex}</span>
        {token && (
          <span className="text-xs text-gray-500 font-mono">{token}</span>
        )}
      </div>
    </div>
  );
}
