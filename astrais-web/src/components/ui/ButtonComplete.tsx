import React from "react";

interface ButtonComplProps {
    title: string
    active?: boolean
    handleActive?: (title: string) => void
}

export default function ButtonComplete({title, active, handleActive} : ButtonComplProps) {

    const [isActive, setIsActive] = React.useState<boolean>(false);
    const currentActive = active ?? isActive;

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
        <button onClick={changeState} className={` ${currentActive ? 'bg-secondary-700 shadow-none translate-y-1' : title == "Completadas" ? 'bg-state-success' : 'bg-state-warning'} shadow-[4px_4px_0px_0px_var(--astrais-primary)]  rounded-md px-4 mb-2`}><span className={`${currentActive ? 'text-white' : 'text-primary-900 ' } text-primary-900 font-bold`}>{title}</span></button>
        </>
  )
}
