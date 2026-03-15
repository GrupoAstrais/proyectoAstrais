interface TypographyExampleProps {
  tag: string;
  font: string;
  size: string;
  weight: string;
  lineHeight: string;
  example: string;
  style?: React.CSSProperties;
}

export function TypographyExample({ tag, font, size, weight, lineHeight, example, style }: TypographyExampleProps) {
  return (
    <div className="border-2 border-[#1E1E2E] rounded p-4 bg-white shadow-[4px_4px_0px_0px_rgba(30,30,46,0.2)]">
      <div className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 mb-4 text-sm">
        <span className="text-gray-500 font-medium">Tag:</span>
        <span className="font-mono">{tag}</span>
        
        <span className="text-gray-500 font-medium">Font:</span>
        <span className="font-mono">{font}</span>
        
        <span className="text-gray-500 font-medium">Size:</span>
        <span className="font-mono">{size}</span>
        
        <span className="text-gray-500 font-medium">Weight:</span>
        <span className="font-mono">{weight}</span>
        
        <span className="text-gray-500 font-medium">Line Height:</span>
        <span className="font-mono">{lineHeight}</span>
      </div>
      <div className="pt-4 border-t-2 border-dashed border-gray-300">
        <div style={style}>{example}</div>
      </div>
    </div>
  );
}
