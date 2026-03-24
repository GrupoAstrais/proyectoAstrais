import Navbar from "../../components/layout/Navbar"
import bgImage from '../../assets/homeScreenBack.jpg'

export default function Shop() {
    return (
        <div style={{ backgroundImage: `url(${bgImage})` }} className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white">
            <Navbar />
            {/* LUDIONES */}
            <div className="ml-auto flex flex-row items-center">
                <div className="rounded-full w-7 h-7 flex items-center justify-center bg-accent-beige-300">
                    <p className="font-['Press_Start_2P'] text-black">L</p>
                </div>
                <p className="font-['Press_Start_2P']">125</p>
            </div>

            {/* Filtrado */}

            <div className="bg-primary-500 rounded-md flex flex-row justify-around p-2 m-2">
                <h3 className="text-2xl font-medium  p-2">Mascota</h3>
                <h3 className="text-2xl font-medium border-x border-accent-beige-300 px-3 py-2">Temas</h3>
                <h3 className="text-2xl font-medium p-2">Colores</h3>
            </div>

            {/* Buscador */}
                
            {/* Tienda */}

            <div className="bg-primary-500 rounded-md flex flex-row justify-around p-2 m-2 gap-2">
                <h1 className="border-b w-full text-center">Categoria</h1>

            </div>
        </div>
    )
}