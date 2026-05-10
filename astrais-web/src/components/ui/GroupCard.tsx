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

// Tarjeta seleccionable con resumen de grupo.
export default function GroupCard({ onClick, id, activeId, data }: GroupCardProps) {
    const active = Number.isFinite(id) && activeId === id;

    const changeState = () => {
        // Ignora ids invalidos antes de avisar al padre.
        if (!Number.isFinite(id)) return;
        onClick(id);
    };

    return (
    <div onClick={changeState} className={` ${active ? 'bg-[linear-gradient(150deg,color-mix(in_srgb,var(--astrais-primary)_74%,transparent),color-mix(in_srgb,var(--astrais-secondary)_56%,transparent))]  text-white ' : 'astrais-primary-panel-bg backdrop-blur-sm p-3.5 shadow-[0_20px_58px_color-mix(in_srgb,var(--astrais-background)_52%,transparent)] text-white/70'} rounded-lg border border-white/15 px-5 py-10`}>
            <div className="flex flex-row gap-2">
                {data.photoUrl ? (
                    <img
                        src={data.photoUrl}
                        alt={`Foto de ${data.name}`}
                        className="h-16 w-16 rounded-lg border border-primary-900 object-cover"
                    />
                ) : (
                    <div className="flex h-16 w-16 items-center justify-center rounded-lg border border-primary-900 bg-accent-beige-300 text-lg font-bold">
                        {data?.name?.charAt(0).toUpperCase()}
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
