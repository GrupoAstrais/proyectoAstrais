import React from "react";

interface DifProps {
    dificultad: string,
    handleActive : (titulo: string) => void,
    esOtroActivo : string
}

export default function DifficultyModal({dificultad, handleActive, esOtroActivo} : DifProps) {
        const [active, setIsActive] = React.useState<boolean>(false);
    
        const changeState = () => {
            setIsActive(!active);
            handleActive(dificultad)
        }
    
        React.useEffect(() => {
            if(esOtroActivo !== dificultad) {
                setIsActive(false)
            }
        },[esOtroActivo, dificultad])
        
    return (
        <>
            <button onClick={changeState} type="button"
            className={`text-primary-900 rounded-xs shadow-xs shadow-primary-900 font-['Space_Grotesk'] px-2 
            ${active ? 'bg-secondary-700 shadow-none translate-y-1' : dificultad == "EASY" ? 'bg-state-success' : dificultad == "MEDIUM" ? 
            'bg-state-warning' : 'bg-state-error'} mb-2`}>
                <span className={`${active ? 'text-white' : 'text-primary-900 ' } text-primary-900 font-bold`}>{dificultad}</span></button>
        </>
  )
}
