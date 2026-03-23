import React from 'react';
import bgImage from '../../assets/homeScreenBack.jpg'
import Navbar from '../../components/layout/Navbar'
import Achiv from '../../components/ui/Achiv'
import CustomPerfilModal from '../../components/modales/CustomPerfilModal';
import FriendsModal from '../../components/modales/FriendsModal';
import ConfigModal from '../../components/modales/ConfigModal';

export default function Profile() {

    const [activeModal, setActiveModal] = React.useState<string>("");

    const handleModal = (active: string) => {
        if(active != activeModal ) {
            setActiveModal(active);
        } else {
            setActiveModal("");
        }
    }

    const handleConfirmCustom = (confirm: boolean) => {
        {/* aquí se guardan los combias del custom */}
        setActiveModal("");
    }

    return (
        <div style={{ backgroundImage: `url(${bgImage})` }} className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white items-center">
            <Navbar />

            <div className='flex flex-row'>
                 {/* perfil entero*/}
                <div className='bg-accent-beige-300 p-2 rounded-md m-2 flex flex-col gap-2'>
                    {/* primera seccion */}
                    <div className='bg-secondary-500 flex flex-row justify-between p-2 rounded-md'>
                        <div className='flex flex-col pt-5'>
                            <h1>Astra</h1>
                            <p>@astra</p>
                            <p>Registro: 16 diciembre</p>
                        </div>
                        <div className="flex flex-col justify-between">
                            <button onClick={() => handleModal("Config")} className='ml-auto rounded-full bg-accent-beige-300 py-2 px-4 text-primary-900 font-medium'>
                                <p>A</p>
                            </button>
                            <div className='flex flex-row gap-2 h-fit'>
                                <button onClick={() => handleModal("Custom")} className='bg-accent-beige-300 h-fit rounded-lg border border-primary-900 text-primary-900 font-medium px-2 '>Editar perfil</button>
                                <button onClick={() => handleModal("Friends")} className='bg-accent-beige-300 h-fit rounded-lg border border-primary-900 text-primary-900 font-medium px-2 '>Ver Amigos</button>
                                <button className='bg-accent-beige-300 rounded-lg border border-primary-900 text-primary-900 font-medium px-2 '>Compartir</button>
                            </div>
                        </div>
                    </div>
                    {/* segunda seccion */}
                    <div className='bg-secondary-500 flex flex-row justify-between p-2 rounded-md'>
                        <Achiv />
                        <Achiv />
                        <Achiv />
                    </div>
                    {/* tercera seccion */}
                    <div className='bg-secondary-500 flex flex-row justify-between p-2 rounded-md'>
                        <h1>Estadística</h1>
                    </div>
                    {/* cuarta seccion */}
                    <div className='bg-secondary-500 flex flex-row justify-between p-2 rounded-md'>
                        <h1>Descripción</h1>
                    </div>
                </div>

                {/* modales */}
                <div className='m-2'>
                    {/* ajustes */}
                    <div className={`${activeModal == 'Config' ? '' : 'hidden'}`}>
                        <ConfigModal />
                    </div>
                    {/* amigos */}
                    <div className={`${activeModal == 'Friends'  ? '' : 'hidden'}`}>
                        <FriendsModal />
                    </div>
                    {/* custom */}
                    <div className={`${activeModal == 'Custom'  ? '' : 'hidden'}`}>
                        <CustomPerfilModal confirmCustom={handleConfirmCustom}/>
                    </div>
                </div>

            </div>
        </div>
    )
}