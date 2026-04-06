import React from "react";

interface GroupCardProps {
    onClick : (id: number) => void
    id: number
    activeId: number
}

export default function GroupCard({onClick, id, activeId} : GroupCardProps) {
    const [active, setIsActive] = React.useState<boolean>(false);
    
    const changeState = () => {
            setIsActive(!active);
            onClick(id)
    }
    
    React.useEffect(() => {
            if(activeId !== id) {
                setIsActive(false)
            }
    },[activeId, id])
        
    return (

    <div onClick={changeState} className={` ${active ? 'border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] text-white ' : 'border-primary-900 bg-primary-500 text-black'} rounded-lg border px-5 py-10`}>
        <div className="flex flex-row gap-2">
            <div className="px-5 py-3 rounded-lg border border-primary-900 bg-accent-beige-300">
                {/* no se si metemos una imagen o que pero de momento lo dejo así */}
            </div>
            <div className="flex flex-col">
                <h3 className="font-medium">Grupo Astraïs</h3>
                <p className="text-xs">Proyecto TFG</p>
            </div>
        </div>
    </div>
  )
}
