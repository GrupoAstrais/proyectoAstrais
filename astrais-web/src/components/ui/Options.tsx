interface OptionProps {
    isDisable : boolean
}

// Representa una opcion bloqueada o disponible del editor.
export default function Option ({isDisable} : OptionProps) {
    return (
        <>
            <div className={`${isDisable ? 'bg-accent-beige-300/60' : 'bg-accent-beige-300'} flex h-12 w-12 shrink-0 justify-center rounded-md border border-primary-700 p-2`}>
                <p className={`${isDisable ? 'hidden' : ''}`}>a</p>
                <svg className={`${isDisable ? '' : 'hidden'} fill-primary-900`} xmlns="http://www.w3.org/2000/svg"  viewBox="0 0 32 32" width="32px" height="32px"><path d="M 16 3 C 12.15625 3 9 6.15625 9 10 L 9 13 L 6 13 L 6 29 L 26 29 L 26 13 L 23 13 L 23 10 C 23 6.15625 19.84375 3 16 3 Z M 16 5 C 18.753906 5 21 7.246094 21 10 L 21 13 L 11 13 L 11 10 C 11 7.246094 13.246094 5 16 5 Z M 8 15 L 24 15 L 24 27 L 8 27 Z"/></svg>
            </div>
        </>
    )
}
