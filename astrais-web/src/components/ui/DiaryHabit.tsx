import React from "react";

interface ButtonProps {
    handleActive : (titulo: string) => void,
    titulo: string,
    esOtroActivo : string,
    active?: boolean
}

export default function ButtonFilter({handleActive, titulo, esOtroActivo, active: activeProp} : ButtonProps) {

    const [active, setIsActive] = React.useState<boolean>(false);
    const currentActive = activeProp ?? active;

    const changeState = () => {
        if(activeProp === undefined) {
            setIsActive(!active);
        }

        handleActive(titulo)
    }

    React.useEffect(() => {
        if(activeProp !== undefined) {
            return;
        }

        if(esOtroActivo !== titulo) {
            setIsActive(false)
        }
    },[activeProp, esOtroActivo, titulo])

    return (
        <>
            <button type="button" onClick={changeState} className={`min-h-10 shrink-0 whitespace-nowrap rounded-xs px-2 shadow-xs shadow-primary-900 ${currentActive ?  'translate-y-1 bg-secondary-700 text-white shadow-none' : 'bg-state-success text-primary-900' }`}>

                <span className="font-bold">{titulo}</span></button>
        </>
  )
}
