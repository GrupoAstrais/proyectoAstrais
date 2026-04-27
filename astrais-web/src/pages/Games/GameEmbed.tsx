import { useParams } from 'react-router'
import LudionClickerGame from './LudionClickerGame'
import { getGameById } from './gameCatalog'
import { PRIMARY_CLICKER_GAME_ID } from './gameStorage'

function ComingSoonGame({ title, description }: { title: string; description: string }) {
  return (
    <main className="grid h-screen min-h-112 place-items-center overflow-hidden bg-[radial-gradient(circle_at_top,rgba(139,92,246,0.28),transparent_36%),linear-gradient(145deg,#0f172a,#172554)] p-6 font-['Space_Grotesk'] text-white">
      <section className="max-w-xl rounded-[28px] border border-white/15 bg-white/8 p-8 text-center shadow-[0_24px_60px_rgba(7,12,24,0.42)]">
        <p className="text-[0.68rem] uppercase tracking-[0.28em] text-accent-beige-300">Cabina en montaje</p>
        <h1 className="mt-4 font-['Press_Start_2P'] text-lg leading-8">{title}</h1>
        <p className="mt-4 text-sm leading-6 text-slate-300">{description}</p>
      </section>
    </main>
  )
}

export default function GameEmbed() {
  const { gameId = '' } = useParams()
  const game = getGameById(gameId)

  if (game?.id === PRIMARY_CLICKER_GAME_ID) {
    return <LudionClickerGame gameId={game.id} />
  }

  return (
    <ComingSoonGame
      title={game?.title ?? 'Cabina no encontrada'}
      description={game?.description ?? 'Este minijuego todavia no esta registrado en el catalogo.'}
    />
  )
}
