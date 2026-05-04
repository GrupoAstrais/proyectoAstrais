interface ColorSwatchProps {
  name: string;
  hex: string;
  token?: string;
  large?: boolean;
  description?: string;
}

export function ColorSwatch({ name, hex, token, large = false, description }: ColorSwatchProps) {
  return (
    <div className="flex flex-col gap-2">
      <div 
        className={`${large ? 'h-24' : 'h-16'} rounded-lg border border-[#11161D]`}
        style={{ backgroundColor: hex }}
      />
      <div className="flex flex-col gap-0.5">
        <span className="text-sm font-medium text-[#F8FAFC]">{name}</span>
        <span className="text-xs text-[#D1D5DB] font-mono">{hex}</span>
        {token && (
          <span className="text-xs text-[#6B7280] font-mono">{token}</span>
        )}
        {description && (
          <span className="text-xs text-[#D1D5DB]">{description}</span>
        )}
      </div>
    </div>
  );
}
