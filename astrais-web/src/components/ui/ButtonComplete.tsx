import React from "react";

interface ButtonComplProps {
    title: string
}

export default function ButtonComplete({title} : ButtonComplProps) {

    const [isActive, setIsActive] = React.useState<boolean>(false);

    return (
        <>
            <button onClick={() => setIsActive(!isActive)} className={` ${isActive ? 'bg-secondary-700 shadow-none translate-y-1' : title == "Completadas" ? 'bg-state-success' : 'bg-state-warning'} shadow-[4px_4px_0px_0px_rgba(88,101,242,1)]  rounded-md px-4 mb-2`}><span className={`${isActive ? 'text-white' : 'text-primary-900 ' } text-primary-900 font-bold`}>{title}</span></button>
        </>
  )
}