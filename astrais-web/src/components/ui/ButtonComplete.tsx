import React from "react";

interface ButtonComplProps {
    title: string
    active?: boolean
    handleActive?: (title: string) => void
}

// Boton de filtro para alternar tareas completadas o pendientes.
export default function ButtonComplete({title, active, handleActive} : ButtonComplProps) {

    const [isActive, setIsActive] = React.useState<boolean>(false);
    const currentActive = active ?? isActive;

    // Usa estado interno solo cuando el padre no controla el valor.
    const changeState = () => {
        if(active === undefined) {
            setIsActive(!isActive)
        }

        if(handleActive) {
            handleActive(title)
        }
    }

    return (
        <>
        <button onClick={changeState} className={`min-h-10 shrink-0 whitespace-nowrap ${currentActive ? 'bg-secondary-700 shadow-none translate-y-1' : title == "Completadas" ? 'bg-state-success' : 'bg-state-warning'} shadow-[4px_4px_0px_0px_var(--astrais-primary)] rounded-md px-4 mb-2`}><span className={`${currentActive ? 'text-white' : 'text-primary-900 ' } text-primary-900 font-bold`}>{title}</span></button>
        </>
  )
}
