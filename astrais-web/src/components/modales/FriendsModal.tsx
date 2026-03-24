import astra from '../../assets/astra2.png'
import FriendCard from '../ui/FriendCard'

export default function FriendsModal() {
        return (
            <div className='flex flex-col rounded-md '>
                <div className='bg-accent-beige-300 rounded-full ml-auto'>
                    <img src={astra} className='w-20' />
                </div>
                <h1 className="text-xl font-['Press_Start_2P']">Amigos de Astra</h1>
                <div className='flex flex-col gap-2 rounded-md bg-accent-beige-300 p-2'>
                    <FriendCard />
                    <FriendCard />
                    <FriendCard />


                </div>
            </div>
    )
}