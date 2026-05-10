import FriendCard from '../ui/FriendCard'
import AstraisMascot from '../ui/AstraisMascot'

// Lista visual de amigos disponibles para invitar.
export default function FriendsModal() {
        return (
            <div className='astrais-modal-surface flex flex-col rounded-md p-4'>
                {/* Encabezado visual de amigos */}
                <div className='bg-[color-mix(in_srgb,var(--astrais-text)_88%,var(--astrais-primary)_12%)] rounded-full ml-auto'>
                    <AstraisMascot fallback="secondary" className='w-20' />
                </div>
                <h1 className="text-xl font-['Press_Start_2P']">Amigos de Astra</h1>
                <div className='astrais-modal-soft-surface flex flex-col gap-2 rounded-md p-2'>
                    {/* Lista temporal de tarjetas de amigos */}
                    <FriendCard />
                    <FriendCard />
                    <FriendCard />


                </div>
            </div>
    )
}
