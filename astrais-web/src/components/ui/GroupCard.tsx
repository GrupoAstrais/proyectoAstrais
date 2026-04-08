import React from "react";

interface GroupCardProps {
    onClick: (id: number) => void
    id: number
    activeId: number
    data: {
        name: string
        description: string
        photoUrl?: string | null
        members: Array<{ id: number; name: string; avatar?: string }>
    }
}

export default function GroupCard({ onClick, id, activeId, data }: GroupCardProps) {
    const [active, setIsActive] = React.useState<boolean>(false);

    const changeState = () => {
        setIsActive(!active);
        onClick(id)
    }

    React.useEffect(() => {
        if (activeId !== id) {
            setIsActive(false)
        }
    }, [activeId, id])

    return (
        <div onClick={changeState} className={` ${active ? 'bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)]  text-white ' : ' bg-[linear-gradient(160deg,#0a101ff2,#3c1480d9,#142f42e6)] backdrop-blur-sm p-3.5 shadow-[0_20px_58px_rgba(7,12,24,0.5)] text-white/70'} rounded-lg border border-white/15 px-5 py-10`}>
            <div className="flex flex-row gap-2">
                {data.photoUrl ? (
                    <img
                        src={data.photoUrl}
                        alt={`Foto de ${data.name}`}
                        className="h-16 w-16 rounded-lg border border-primary-900 object-cover"
                    />
                ) : (
                    <div className="flex h-16 w-16 items-center justify-center rounded-lg border border-primary-900 bg-accent-beige-300 text-lg font-bold">
                        {data.name.charAt(0).toUpperCase()}
                    </div>
                )}
                <div className="flex flex-col">
                    <h3 className="font-medium">{data.name}</h3>
                    <p className="text-xs">{data.description}</p>
                    <p className="text-xs opacity-80">
                        {data.members.length} miembro{data.members.length === 1 ? "" : "s"}
                    </p>
                </div>
            </div>
        </div>
    )
}
