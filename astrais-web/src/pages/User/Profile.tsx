import bgImage from '../../assets/homeScreenBack.jpg'
import Navbar from '../../components/layout/Navbar'
import Achiv from '../../components/ui/Achiv'

export default function Profile() {
    return (
        <div style={{ backgroundImage: `url(${bgImage})` }} className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white">
            <Navbar />

            <div className='flex flex-row'>
                 {/* perfil entero*/}
                <div className='bg-accent-beige-300 p-2 rounded-md m-2 w-1/2 flex flex-col gap-2'>
                    {/* primera seccion */}
                    <div className='bg-secondary-500 flex flex-row justify-between p-2 rounded-md'>
                        <div className='flex flex-col pt-5'>
                            <h1>Astra</h1>
                            <p>@astra</p>
                            <p>Registro: 16 diciembre</p>
                        </div>
                        <div className="flex flex-col justify-between">
                            <div className='ml-auto rounded-full bg-accent-beige-300 py-2 px-4 text-primary-900 font-medium'>
                                <p>A</p>
                            </div>
                            <div className='flex flex-row gap-2 h-1/3'>
                                <button className='bg-accent-beige-300 rounded-lg border border-primary-900 text-primary-900 font-medium px-2 '>Editar perfil</button>
                                <button className='bg-accent-beige-300 rounded-lg border border-primary-900 text-primary-900 font-medium px-2 '>Ver Amigos</button>
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
                <div className='hidden'>
                    {/* ajustes */}
                    <div>

                    </div>
                    {/* amigos */}
                    <div>

                    </div>
                    {/* custom */}
                    <div>

                    </div>
                </div>

            </div>
        </div>
    )
}