interface ConfigProps {
    title: string
}
{/* https://liquid-glass-js.com/   <- liquid glass */}
export default function Configuration({title} : ConfigProps) {
    return (
    <div className="flex flex-row gap-2 p">
        <div className="rounded-full p-3 bg-primary-500">
            <svg className="fill-primary-900" xmlns="http://www.w3.org/2000/svg"  viewBox="0 0 32 32" width="32px" height="32px"><path d="M 16 3 C 12.15625 3 9 6.15625 9 10 L 9 13 L 6 13 L 6 29 L 26 29 L 26 13 L 23 13 L 23 10 C 23 6.15625 19.84375 3 16 3 Z M 16 5 C 18.753906 5 21 7.246094 21 10 L 21 13 L 11 13 L 11 10 C 11 7.246094 13.246094 5 16 5 Z M 8 15 L 24 15 L 24 27 L 8 27 Z"/></svg>
        </div>
        <div className="flex items-center  p-2">
            <p>{title}</p>
        </div>
        <div className="flex items-center">
            <p> '-' </p>
        </div>
    </div>
  )
}