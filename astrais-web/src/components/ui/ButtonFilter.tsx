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
            <button onClick={changeState} className={`rounded-full px-4 ${active ?  'bg-white text-black' : 'bg-black text-white' }`}><span className="font-bold">{titulo}</span></button>
        </>
  )
}
