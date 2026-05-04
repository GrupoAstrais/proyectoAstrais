interface TypographyExampleProps {
  tag: string;
  font: string;
  webSize: string;
  androidSize: string;
  weight: string;
  lineHeight: string;
  example: string;
  style?: React.CSSProperties;
}

export function TypographyExample({ tag, font, webSize, androidSize, weight, lineHeight, example, style }: TypographyExampleProps) {
  return (
    <div className="border border-[#11161D] rounded-lg p-4 bg-[#0D1117]">
      <div className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 mb-4 text-sm">
        <span className="text-[#6B7280] font-medium">Tag:</span>
        <span className="font-mono text-[#D1D5DB]">{tag}</span>
        
        <span className="text-[#6B7280] font-medium">Font:</span>
        <span className="font-mono text-[#D1D5DB]">{font}</span>
        
        <span className="text-[#6B7280] font-medium">Web:</span>
        <span className="font-mono text-[#D1D5DB]">{webSize}</span>
        
        <span className="text-[#6B7280] font-medium">Android:</span>
        <span className="font-mono text-[#D1D5DB]">{androidSize}</span>
        
        <span className="text-[#6B7280] font-medium">Weight:</span>
        <span className="font-mono text-[#D1D5DB]">{weight}</span>
        
        <span className="text-[#6B7280] font-medium">Line Height:</span>
        <span className="font-mono text-[#D1D5DB]">{lineHeight}</span>
      </div>
      <div className="pt-4 border-t border-dashed border-[#11161D]">
        <div style={style}>{example}</div>
      </div>
    </div>
  );
}
