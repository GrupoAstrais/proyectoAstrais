import React from "react";

interface ButtonProps {
    handleActive : (titulo: string) => void,
    titulo: string,
    esOtroActivo : string
}

export default function ButtonFilter({handleActive, titulo, esOtroActivo} : ButtonProps) {

    const [active, setIsActive] = React.useState<boolean>(false);

    const changeState = () => {
        setIsActive(!active);
        handleActive(titulo)
    }

    React.useEffect(() => {
        if(esOtroActivo !== titulo) {
            setIsActive(false)
        }
    },[esOtroActivo, titulo])

    return (
        <>
            <button onClick={changeState} className={`rounded-xs px-2 shadow-xs shadow-primary-900 ${active ?  'translate-y-1 bg-secondary-700 text-white shadow-none' : 'bg-state-success text-primary-900' }`}>
                <span className="font-bold">{titulo}</span></button>
        </>
  )
}
