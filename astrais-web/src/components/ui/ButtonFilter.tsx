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
            <button onClick={changeState} className={`rounded-full px-4 ${currentActive ?  'bg-white text-black' : 'bg-black text-white' }`}><span className="font-bold">{titulo}</span></button>
        </>
  )
}
