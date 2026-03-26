import Navbar from "../../components/layout/Navbar"
import bgImage from '../../assets/homeScreenBack.jpg'

export default function games() {
    return (
<div
      style={{ backgroundImage: `url(${bgImage})` }}
      className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white"
    >
      <Navbar />

      {/* Header right: timer + profile */}
      <div className="absolute top-6 right-6 flex items-center gap-2 z-10">
        <div className="rounded-full w-7 h-7 flex items-center justify-center bg-white">
          <span className="font-['Press_Start_2P'] text-black text-xs">P</span>
        </div>
        <p className="font-['Press_Start_2P'] text-sm">30 min</p>
      </div>

      {/* Main content */}
      <main className="mx-auto w-full max-w-7xl px-4 py-6 flex flex-col gap-6">
        {/* Large card (top) — compact height */}
        <div className="h-[30vh] md:h-[35vh] rounded-xl overflow-hidden shadow-2xl border border-purple-700/50">
          <div
            className="w-full h-full bg-cover bg-center"
            style={{
              backgroundImage: "url('https://placehold.co/800x450/4a207a/ffffff?text=Космический+Кот')",
            }}
          />
        </div>

        {/* Grid: 2 columns on md+, stacked on mobile */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Left column: 2 cards (small + locked) */}
            <div className="flex flex-col gap-4">
                {/* Small card 1 */}
                <div className="h-64 rounded-xl overflow-hidden shadow-xl border border-purple-700/50">
                <div
                    className="w-full h-full bg-cover bg-center"
                    style={{
                    backgroundImage: "url('https://placehold.co/400x400/6d28d9/ffffff?text=Космический+Кот')",
                    }} />
                </div>

                {/* Locked card */}
                <div className="h-64 rounded-xl bg-linear-to-br from-purple-900/70 to-indigo-900/70 flex items-center justify-center shadow-xl border border-purple-700/50">
                    <div className="text-center">
                        <div className="w-16 h-16 mx-auto mb-4 bg-white/20 rounded-lg flex items-center justify-center">
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                className="h-8 w-8 text-white"
                                fill="none"
                                viewBox="0 0 24 24"
                                stroke="currentColor"
                            >
                                <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                                />
                            </svg>
                        </div>
                    </div>
                </div>
            </div>

          {/* Right column: 1 card (small) */}
        <div className="rounded-xl overflow-hidden shadow-xl h-full border border-purple-7050">
            <div
                className=" h-full bg-cover bg-center"
                style={{
                    backgroundImage: "url('https://placehold.co/400x400/7c3aed/ffffff?text=Космический+Кот')",
                }}
            />
            </div>
        </div>
      </main>
    </div>
    )
}

