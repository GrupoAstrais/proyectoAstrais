import React from 'react';
import bgImage from '../../assets/homeScreenBack.jpg'
import Navbar from '../../components/layout/Navbar'
import Achiv from '../../components/ui/Achiv'
import CustomPerfilModal from '../../components/modales/CustomPerfilModal';
import FriendsModal from '../../components/modales/FriendsModal';
import ConfigModal from '../../components/modales/ConfigModal';
import astra from '../../assets/astra2.png';
import astra2 from '../../assets/astra.png';
import ProgressBar from '../../components/ui/Reward';

export default function Profile() {

    const [activeModal, setActiveModal] = React.useState<string>("");

    const handleModal = (active: string) => {
        if(active != activeModal ) {
            setActiveModal(active);
        } else {
            setActiveModal("");
        }
    }

    const handleConfirmCustom = () => {
        {/* aquí se guardan los combias del custom */}
        setActiveModal("");
    }

    return (
        <div style={{ backgroundImage: `url(${bgImage})` }} className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white items-center">
            <Navbar />

            <div className='md:flex :mdflex-row relative mt-25 xs:grid xs:grid-cols-1'>
                <img src={astra2} className='h-32 absolute -top-25 left-45 z-50' />
                 {/* perfil entero*/}
                <div className='bg-accent-beige-300 p-2 rounded-md m-2 flex flex-col gap-2'>
                    {/* primera seccion */}
                    <div className='bg-secondary-500 flex flex-row justify-between p-2 rounded-md relative'>
                        <div className='rounded-full bg-white absolute -top-12 left-2'>
                            <img src={astra} className='w-22' /> 
                        </div>
                        <div className='flex flex-col pt-10'>
                            <h1 className='font-medium text-xl'>Astra</h1>
                            <div className='flex flex-row text-xs gap-2 '>
                                <p>@astra</p>
                                <p>Nivel 3</p>
                            </div>
                            <div className='fill-state-warning flex flex-row items-center gap-3'>
                                <svg version="1.0" xmlns="http://www.w3.org/2000/svg"
                                width="20" height="20" viewBox="0 0 1280.000000 1181.000000"
                                preserveAspectRatio="xMidYMid meet">
                                    <g transform="translate(0.000000,1181.000000) scale(0.100000,-0.100000)" stroke="none">
                                    <path d="M6327 11292 c-60 -180 -161 -489 -227 -687 -65 -198 -233 -709 -373
                                    -1135 -141 -426 -367 -1114 -503 -1527 l-248 -753 -2358 0 c-1297 0 -2358 -3
                                    -2358 -7 0 -5 170 -130 378 -279 207 -149 1057 -758 1887 -1353 831 -596 1518
                                    -1091 1528 -1100 20 -19 55 94 -420 -1346 -187 -570 -344 -1047 -628 -1910
                                    -141 -429 -286 -869 -322 -978 -36 -109 -63 -201 -60 -204 7 -6 -236 -180
                                    1912 1362 1012 726 1855 1331 1872 1343 l33 23 762 -548 c2447 -1758 3053
                                    -2191 3056 -2188 2 2 -46 153 -106 337 -61 183 -216 655 -346 1048 -511 1556
                                    -712 2168 -811 2470 -145 440 -185 563 -185 575 0 6 855 623 1900 1373 1045
                                    750 1900 1368 1900 1373 0 5 -909 10 -2357 11 l-2356 3 -164 500 c-90 275
                                    -272 826 -403 1225 -131 399 -383 1166 -560 1705 -177 539 -325 983 -329 987
                                    -4 5 -55 -139 -114 -320z"/>
                                    </g>
                                </svg>
                                <ProgressBar value={45}/>                
                            </div>
                        </div>
                        <div className="flex flex-col justify-between ms-10">
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
                    <div className='bg-secondary-500 flex flex-col justify-between p-2 rounded-md'>
                        <h1 className='text-xl font-medium'>Estadística</h1>
                        <div className='text-xs'>
                            <p>Registro: 16 diciembre</p>
                            <p>Logros conseguidos: 15</p>
                            <p>Amigos: 7</p>
                        </div>
                    </div>
                    {/* cuarta seccion */}
                    <div className='bg-secondary-500 h-full flex flex-col  p-2 rounded-md'>
                        <h1 className='text-xl font-medium'>Descripción</h1>
                            <div className='text-xs'>
                                <p>Macot de la app Astraïs</p>
                            </div>
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
