import astra from '../../assets/astra2.png'
import FriendCard from '../ui/FriendCard'

export default function FriendsModal() {
        return (
            <div className='astrais-modal-surface flex flex-col rounded-md p-4'>
                <div className='bg-[color-mix(in_srgb,var(--astrais-text)_88%,var(--astrais-primary)_12%)] rounded-full ml-auto'>
                    <img src={astra} className='w-20' />
                </div>
                <h1 className="text-xl font-['Press_Start_2P']">Amigos de Astra</h1>
                <div className='astrais-modal-soft-surface flex flex-col gap-2 rounded-md p-2'>
                    <FriendCard />
                    <FriendCard />
                    <FriendCard />


                </div>
            </div>
    )
}
