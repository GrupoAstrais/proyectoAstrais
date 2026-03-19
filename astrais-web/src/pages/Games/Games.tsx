import Navbar from "../../components/layout/Navbar"
import bgImage from '../../assets/homeScreenBack.jpg'

export default function games() {
    return (
        <div style={{ backgroundImage: `url(${bgImage})` }} className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white">
            <Navbar />
        </div>
    )
}