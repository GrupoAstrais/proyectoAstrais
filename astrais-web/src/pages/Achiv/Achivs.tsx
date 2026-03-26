import Navbar from "../../components/layout/Navbar"
import bgImage from '../../assets/homeScreenBack.jpg'
import AchivCard from "../../components/ui/AchivCard"

export default function Achivs() {
    return (
        <div style={{ backgroundImage: `url(${bgImage})` }} className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white">
            <Navbar />

            <div className="flex flex-col justify-center items-center">
             {/* Grid: 2x2 */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-4xl w-full">

                    {/* Card */}
                    <AchivCard />
                    <AchivCard />
                    <AchivCard />
                    <AchivCard />
                </div>
            </div>
        </div>
    )
}