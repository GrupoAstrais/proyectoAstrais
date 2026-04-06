interface TTProps {
    handleActive : (act: boolean) => void,
    active: boolean
}

export default function TaskType({active, handleActive} : TTProps) {
    return (
        <>
            <button type="button" onClick={() => handleActive(!active)} 
            className={`text-primary-900 rounded-xs shadow-xs shadow-primary-900 font-['Space_Grotesk'] px-2 
            ${active ? 'bg-secondary-700 shadow-none translate-y-1' :  'bg-state-success'} mb-2`}>
                <span className={`${active ? 'text-white' : 'text-primary-900 ' } text-primary-900 font-bold`}>Es compuesta?</span></button>
        </>
  )
}