import astra from '../../assets/astra2.png'

export default function FriendCard() {
    return (
        <div className="p-2 flex bg-secondary-500 rounded-md justify-between items-center">
            <div className="bg-accent-beige-300 rounded-full border-2 border-secondary-700">
                <img src={astra} className='w-12' />
            </div>
            <div>
                <button className='bg-accent-beige-300 rounded-lg border-2 border-secondary-700 text-primary-900 font-medium p-2 '>Invitar al grupo</button>
            </div>
        </div>
  )
}