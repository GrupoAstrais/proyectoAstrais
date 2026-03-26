import Navbar from "../../components/layout/Navbar"
import bgImage from '../../assets/homeScreenBack.jpg'

export default function Shop() {
    return (
        <div style={{ backgroundImage: `url(${bgImage})` }} className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white">
            <Navbar />
            {/* LUDIONES */}
            <div className="ml-auto flex flex-row items-center gap-2">
                <div className="rounded-full w-7 h-7 flex items-center justify-center bg-white">
                    <p className="font-['Press_Start_2P'] text-black">L</p>
                </div>
                <p className="font-['Press_Start_2P']">125</p>
            </div>

            <div className="flex flex-col justify-center items-center">
                <div className=" w-2/3">
                    {/* Filtrado */}

                    <div className="bg-primary-500 rounded-md flex flex-row w-full justify-around p-2 m-2">
                        <h3 className="text-2xl font-medium  p-2">Mascota</h3>
                        <h3 className="text-2xl font-medium border-x border-accent-beige-300 px-3 py-2">Temas</h3>
                        <h3 className="text-2xl font-medium p-2">Colores</h3>
                    </div>

                    {/* Buscador */}
                        
                    {/* Tienda */}

                    <div className="bg-primary-500 rounded-md w-full flex flex-col justify-around p-2 m-2 gap-2">
                        <h1 className="border-b w-full text-center">Categoria</h1>

                        {/* Grid: 2 rows × 3 cols */}
                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-5 max-w-4xl w-full">
                            {/* Row 1 */}
                            <div className="h-32 rounded-xl bg-linear-to-b from-primary-500/30 to-primary-600/20 border border-primary-500/20 shadow-sm"></div>
                            <div className="h-32 rounded-xl bg-linear-to-b from-primary-700 to-primary-900 border border-primary-700/30 shadow-sm"></div>
                            <div className="h-32 rounded-xl bg-linear-to-b from-primary-500/30 to-primary-600/20 border border-primary-500/20 shadow-sm"></div>
                        
                            {/* Row 2 */}
                            <div className="h-32 rounded-xl bg-linear-to-b from-primary-700 to-primary-900 border border-primary-700/30 shadow-sm"></div>
                            <div className="h-32 rounded-xl bg-linear-to-b from-primary-500/30 to-primary-600/20 border border-primary-500/20 shadow-sm"></div>
                            <div className="h-32 rounded-xl bg-linear-to-b from-primary-700 to-primary-900 border border-primary-700/30 shadow-sm"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}