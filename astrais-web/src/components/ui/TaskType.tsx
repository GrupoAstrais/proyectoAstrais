import React from "react";

interface TTProps {
    handleActive : (act: boolean) => void
}

export default function TaskType({handleActive} : TTProps) {
        const [active, setIsActive] = React.useState<boolean>(false);
    
        const changeState = () => {
            setIsActive(!active);
            handleActive(active);
        }
        
    return (
        <>
            <button onClick={changeState} 
            className={`text-primary-900 rounded-xs shadow-xs shadow-primary-900 font-['Space_Grotesk'] px-2 
            ${active ? 'bg-secondary-700 shadow-none translate-y-1' :  'bg-state-success'} mb-2`}>
                <span className={`${active ? 'text-white' : 'text-primary-900 ' } text-primary-900 font-bold`}>Es compuesta?</span></button>
        </>
  )
}