import astra from '../../assets/astra.png'
import Option from '../ui/Options'

interface CustomProps {
    confirmCustom: (confirm: boolean) => void
}

export default function CustomPerfilModal({confirmCustom} : CustomProps) {

    const handleClose = () => {
        confirmCustom(true);
    }
  
    return (
    <div className="flex flex-col rounded-md">
        <div className="flex flex-row justify-between items-center py-2 px-4">
            <h1 className="font-['Press_Start_2P'] text-2xl" >Editar</h1>
            <button onClick={handleClose} className='rounded-full p-2 bg-accent-beige-300'>
                <svg xmlns="http://www.w3.org/2000/svg"  viewBox="0 0 50 50" width="40px" height="40px"><path d="M 42.875 8.625 C 42.84375 8.632813 42.8125 8.644531 42.78125 8.65625 C 42.519531 8.722656 42.292969 8.890625 42.15625 9.125 L 21.71875 40.8125 L 7.65625 28.125 C 7.410156 27.8125 7 27.675781 6.613281 27.777344 C 6.226563 27.878906 5.941406 28.203125 5.882813 28.597656 C 5.824219 28.992188 6.003906 29.382813 6.34375 29.59375 L 21.25 43.09375 C 21.46875 43.285156 21.761719 43.371094 22.050781 43.328125 C 22.339844 43.285156 22.59375 43.121094 22.75 42.875 L 43.84375 10.1875 C 44.074219 9.859375 44.085938 9.425781 43.875 9.085938 C 43.664063 8.746094 43.269531 8.566406 42.875 8.625 Z"/></svg>
            </button>
        </div>
        <div className="h-1/2 bg-secondary-500/80 rounded-t-md flex items-center justify-center">
            <img src={astra} className='w-2/3 '/> 
        </div>
        <div className='bg-accent-beige-300 rounded-b-md'>
            <div className='bg-secondary-500 flex flex-row justify-around p-2 m-2 rounded-md'>
                <Option isDisable={false} />
                <Option isDisable={true} />
                <Option isDisable={true} />
                <Option isDisable={true} />
                <Option isDisable={true} />
                <Option isDisable={true} />
            </div>
            <div className='grid grid-cols-4 justify-items-center gap-2 p-2'>
                <Option isDisable={false} />
                <Option isDisable={true} />
                <Option isDisable={true} />
                <Option isDisable={true} />
                <Option isDisable={true} />
                <Option isDisable={true} />
            </div>
        </div>
    </div>
  )
}