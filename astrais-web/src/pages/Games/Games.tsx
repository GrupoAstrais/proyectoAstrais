import React from 'react'
import { NavLink } from 'react-router'
import Navbar from '../../components/layout/Navbar'
import bgImage from '../../assets/homeScreenBack.jpg'
import gamePreview from '../../assets/game.png'
import { gameCatalog, getGameById } from './gameCatalog'
import { isGameRoundCompletedMessage } from './gameMessages'
import {
  buildArcadeStatsAfterRound,
  getArenaRank,
  getGameStats,
  readArcadeStats,
  writeArcadeStats,
} from './gameStorage'

interface LastRoundSummary {
  gameTitle: string
  score: number
  reward: number
  durationSeconds: number
}

export default function Games() {
  const [selectedGameId, setSelectedGameId] = React.useState(() => gameCatalog[0]?.id ?? '')
  const [arcadeStats, setArcadeStats] = React.useState(() => readArcadeStats())
  const [lastRound, setLastRound] = React.useState<LastRoundSummary | null>(null)
  const arcadeStatsRef = React.useRef(arcadeStats)

  const selectedGame = getGameById(selectedGameId) ?? gameCatalog[0]
  const selectedGameStats = selectedGame ? getGameStats(arcadeStats, selectedGame.id) : null
  const playableGames = gameCatalog.filter((game) => game.availability === 'available').length
  const careerRank = getArenaRank(arcadeStats.bestScore)
  const selectedRank = selectedGameStats ? getArenaRank(selectedGameStats.bestScore) : getArenaRank(0)

  React.useEffect(() => {
    arcadeStatsRef.current = arcadeStats
  }, [arcadeStats])

  React.useEffect(() => {
    const handleRoundMessage = (event: MessageEvent) => {
      if (event.origin !== window.location.origin || !isGameRoundCompletedMessage(event.data)) {
        return
      }

      const game = getGameById(event.data.gameId)

      if (!game || game.availability !== 'available') {
        return
      }

      const { stats, reward } = buildArcadeStatsAfterRound(arcadeStatsRef.current, event.data)
      arcadeStatsRef.current = stats
      writeArcadeStats(stats)
      setArcadeStats(stats)
      setLastRound({
        gameTitle: game.title,
        score: event.data.score,
        reward,
        durationSeconds: event.data.durationSeconds,
      })
    }

    window.addEventListener('message', handleRoundMessage)
    return () => window.removeEventListener('message', handleRoundMessage)
  }, [])

  return (
    <div
      style={{ backgroundImage: `url(${bgImage})` }}
      className="relative h-screen w-screen overflow-hidden bg-cover bg-center font-['Space_Grotesk'] text-white"
    >
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(139,92,246,0.28),transparent_36%),radial-gradient(circle_at_bottom_right,rgba(34,197,94,0.16),transparent_38%)]" />
      <div className="pointer-events-none absolute inset-0 bg-black/62" />
      <div className="scanlines pointer-events-none absolute inset-0 opacity-25" />

      <div className="relative z-10 flex h-full w-full min-h-0 flex-col">
        <Navbar />

        <main className="flex min-h-0 flex-1 px-3 pb-3 pt-1 md:px-4 md:pb-4 xl:px-6 xl:pb-5">
          <section className="games-stage mx-auto hidden h-full w-full gap-3 lg:grid lg:grid-cols-[minmax(17rem,0.52fr)_minmax(0,1.28fr)_minmax(18rem,0.72fr)] min-[1400px]:gap-4 min-[1400px]:grid-cols-[minmax(20rem,0.5fr)_minmax(0,1.44fr)_minmax(22rem,0.78fr)]">
            <aside className="panel-glow relative grid min-h-0 grid-rows-[auto_minmax(0,1fr)] overflow-hidden rounded-[26px] border border-white/15 bg-[linear-gradient(180deg,rgba(15,23,42,0.88),rgba(76,29,149,0.48))] p-3.5 shadow-[0_20px_54px_rgba(7,12,24,0.45)] min-[1400px]:p-4">
              <div className="pointer-events-none absolute -left-16 top-8 h-40 w-40 rounded-full bg-secondary-500/18 blur-3xl" />
              <header className="relative z-10">
                <p className="text-[0.62rem] uppercase tracking-[0.24em] text-accent-beige-300">Catalogo arcade</p>
                <h1 className="mt-3 font-['Press_Start_2P'] text-[0.94rem] leading-6 text-white min-[1400px]:text-[1.05rem]">
                  Videojuegos
                </h1>
                <p className="mt-3 text-[0.76rem] leading-5 text-slate-300 min-[1400px]:text-[0.84rem]">
                  Cada cabina se abre en el visor y conserva su propia entrada de estadisticas.
                </p>
              </header>

              <div className="relative z-10 mt-4 min-h-0 space-y-2 overflow-y-auto pr-1 catalog-scroll">
                {gameCatalog.map((game, index) => {
                  const isSelected = selectedGame?.id === game.id
                  const gameStats = getGameStats(arcadeStats, game.id)

                  return (
                    <button
                      key={game.id}
                      type="button"
                      onClick={() => setSelectedGameId(game.id)}
                      className={`w-full rounded-[22px] border p-3 text-left transition ${
                        isSelected
                          ? 'border-accent-beige-300/42 bg-white/12 shadow-[0_14px_30px_rgba(15,23,42,0.3)]'
                          : 'border-white/10 bg-black/16 hover:border-white/20 hover:bg-white/8'
                      }`}
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div className="min-w-0">
                          <p className="text-[0.56rem] uppercase tracking-[0.2em] text-slate-400">Cabina {index + 1}</p>
                          <h2 className="mt-1 truncate text-[0.86rem] font-semibold text-white min-[1400px]:text-[0.94rem]">
                            {game.title}
                          </h2>
                        </div>
                        <span
                          className={`rounded-full border px-2 py-1 text-[0.52rem] uppercase tracking-[0.16em] ${
                            game.availability === 'available'
                              ? 'border-accent-mint-300/30 bg-accent-mint-300/10 text-accent-mint-300'
                              : 'border-white/12 bg-white/8 text-slate-400'
                          }`}
                        >
                          {game.statusLabel}
                        </span>
                      </div>

                      <p className="mt-3 text-[0.72rem] leading-5 text-slate-300 min-[1400px]:text-[0.78rem]">
                        {game.description}
                      </p>

                      <div className="mt-3 grid grid-cols-2 gap-2 text-[0.62rem] text-slate-300">
                        <div className="rounded-xl border border-white/10 bg-white/6 px-2 py-2">
                          <span className="block uppercase tracking-[0.16em] text-slate-500">Mejor</span>
                          <strong className="mt-1 block text-[0.78rem] text-white">{gameStats.bestScore}</strong>
                        </div>
                        <div className="rounded-xl border border-white/10 bg-white/6 px-2 py-2">
                          <span className="block uppercase tracking-[0.16em] text-slate-500">Partidas</span>
                          <strong className="mt-1 block text-[0.78rem] text-accent-mint-300">{gameStats.gamesPlayed}</strong>
                        </div>
                      </div>
                    </button>
                  )
                })}
              </div>
            </aside>

            <article className="panel-glow relative grid min-h-0 grid-rows-[auto_minmax(0,1fr)] overflow-hidden rounded-[28px] border border-white/15 bg-[linear-gradient(150deg,rgba(15,23,42,0.9),rgba(30,74,99,0.78))] p-3.5 shadow-[0_22px_60px_rgba(7,12,24,0.52)] min-[1400px]:p-4">
              <div className="retro-grid pointer-events-none absolute inset-0 opacity-28" />
              <div className="pointer-events-none absolute bottom-0 right-0 h-56 w-56 rounded-full bg-primary-500/18 blur-3xl" />

              <header className="relative z-10 grid grid-cols-[minmax(0,1fr)_17rem] gap-3 pb-3 min-[1400px]:grid-cols-[minmax(0,1fr)_20rem]">
                <div>
                  <p className="inline-flex items-center rounded-full border border-accent-beige-300/30 bg-white/8 px-3 py-1 text-[0.62rem] font-semibold uppercase tracking-[0.24em] text-accent-beige-300">
                    Visor iframe
                  </p>
                  <h2 className="mt-3 font-['Press_Start_2P'] text-[clamp(1rem,1.5vw,1.32rem)] leading-tight">
                    {selectedGame?.title ?? 'Cabina'}
                  </h2>
                  <p className="mt-3 max-w-2xl text-[0.8rem] leading-5 text-slate-200 min-[1400px]:text-[0.88rem]">
                    {selectedGame?.description ?? 'Selecciona una cabina del catalogo.'}
                  </p>
                </div>

                <div className="grid grid-cols-2 gap-2.5 self-center text-center">
                  <div className="rounded-2xl border border-white/10 bg-black/22 px-3 py-3">
                    <p className="text-[0.58rem] uppercase tracking-[0.2em] text-slate-400">Mejor</p>
                    <p className="mt-2 font-['Press_Start_2P'] text-[0.86rem] text-accent-beige-300">
                      {selectedGameStats?.bestScore ?? 0}
                    </p>
                  </div>
                  <div className="rounded-2xl border border-white/10 bg-black/22 px-3 py-3">
                    <p className="text-[0.58rem] uppercase tracking-[0.2em] text-slate-400">Rango</p>
                    <p className="mt-2 text-[0.74rem] font-semibold text-[#f5c6ff]">{selectedRank}</p>
                  </div>
                </div>
              </header>

              <div className="relative z-10 min-h-0 overflow-hidden rounded-[22px] border border-white/15 bg-black/35 shadow-[inset_0_0_0_1px_rgba(255,255,255,0.04)]">
                {selectedGame ? (
                  <iframe
                    id="preview-iframe"
                    title={`Jugar a ${selectedGame.title}`}
                    src={selectedGame.embedPath}
                    className="h-full w-full border-0 bg-slate-950"
                  />
                ) : null}
              </div>
            </article>

            <aside className="grid min-h-0 grid-rows-[auto_minmax(0,1fr)] gap-3 min-[1400px]:gap-4">
              <article className="panel-glow relative overflow-hidden rounded-[26px] border border-white/15 bg-[linear-gradient(160deg,rgba(15,23,42,0.88),rgba(30,74,99,0.74))] p-3.5 shadow-[0_18px_44px_rgba(7,12,24,0.42)] min-[1400px]:p-4">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-[0.62rem] uppercase tracking-[0.24em] text-accent-beige-300">Panel de sesion</p>
                    <h2 className="mt-2 font-['Press_Start_2P'] text-[0.92rem] text-white min-[1400px]:text-[1rem]">
                      Arcade activo
                    </h2>
                    <p className="mt-2 text-[0.76rem] leading-5 text-slate-300 min-[1400px]:text-[0.84rem]">
                      {playableGames} cabina jugable de {gameCatalog.length} registradas.
                    </p>
                  </div>
                  <img
                    src={gamePreview}
                    alt="Vista previa del arcade"
                    className="h-16 w-16 rounded-2xl border border-white/5 object-cover pl-3 shadow-[0_14px_24px_rgba(15,23,42,0.2)] min-[1400px]:h-24 min-[1400px]:w-40"
                  />
                </div>

                <div className="mt-4 grid grid-cols-3 gap-2.5">
                  <div className="rounded-2xl border border-white/10 bg-white/8 p-3">
                    <p className="text-[0.56rem] uppercase tracking-[0.2em] text-slate-400">Partidas</p>
                    <p className="mt-2 text-[1.02rem] font-semibold text-white">{arcadeStats.gamesPlayed}</p>
                  </div>
                  <div className="rounded-2xl border border-white/10 bg-white/8 p-3">
                    <p className="text-[0.56rem] uppercase tracking-[0.2em] text-slate-400">Mejor</p>
                    <p className="mt-2 text-[1.02rem] font-semibold text-accent-mint-300">{arcadeStats.bestScore}</p>
                  </div>
                  <div className="rounded-2xl border border-white/10 bg-white/8 p-3">
                    <p className="text-[0.56rem] uppercase tracking-[0.2em] text-slate-400">Ludiones</p>
                    <p className="mt-2 text-[1.02rem] font-semibold text-[#f5c6ff]">{arcadeStats.totalLudionsEarned}</p>
                  </div>
                </div>
              </article>

              <div className="grid min-h-0 grid-rows-[auto_minmax(0,1fr)] gap-3 min-[1400px]:gap-4">
                <article className="panel-glow relative overflow-hidden rounded-3xl border border-white/15 bg-[linear-gradient(180deg,rgba(15,23,42,0.84),rgba(76,29,149,0.42))] p-3.5 shadow-[0_18px_42px_rgba(7,12,24,0.38)] min-[1400px]:p-4">
                  <p className="text-[0.62rem] uppercase tracking-[0.24em] text-accent-beige-300">Ultima ronda</p>
                  <h2 className="mt-2 font-['Press_Start_2P'] text-[0.86rem] text-white min-[1400px]:text-[0.94rem]">
                    Resultado
                  </h2>

                  <div className="mt-4 rounded-2xl border border-white/10 bg-black/18 px-3 py-3">
                    {lastRound ? (
                      <>
                        <p className="text-[0.78rem] font-semibold text-white">{lastRound.gameTitle}</p>
                        <div className="mt-3 grid grid-cols-3 gap-2 text-center">
                          <div>
                            <p className="text-[0.54rem] uppercase tracking-[0.16em] text-slate-500">Puntos</p>
                            <p className="mt-1 text-[0.92rem] font-semibold text-white">{lastRound.score}</p>
                          </div>
                          <div>
                            <p className="text-[0.54rem] uppercase tracking-[0.16em] text-slate-500">Premio</p>
                            <p className="mt-1 text-[0.92rem] font-semibold text-[#f8d089]">{lastRound.reward}</p>
                          </div>
                          <div>
                            <p className="text-[0.54rem] uppercase tracking-[0.16em] text-slate-500">Tiempo</p>
                            <p className="mt-1 text-[0.92rem] font-semibold text-accent-mint-300">{lastRound.durationSeconds}s</p>
                          </div>
                        </div>
                      </>
                    ) : (
                      <p className="text-[0.76rem] leading-5 text-slate-300">
                        Juega una ronda en el visor para registrar el primer resultado de la sesion.
                      </p>
                    )}
                  </div>
                </article>

                <article className="panel-glow relative min-h-0 overflow-hidden rounded-3xl border border-white/15 bg-[linear-gradient(180deg,rgba(15,23,42,0.84),rgba(30,41,59,0.82))] p-3.5 shadow-[0_18px_42px_rgba(7,12,24,0.38)] min-[1400px]:p-4">
                  <p className="text-[0.62rem] uppercase tracking-[0.24em] text-accent-beige-300">Progreso</p>
                  <h2 className="mt-2 font-['Press_Start_2P'] text-[0.86rem] text-white min-[1400px]:text-[0.94rem]">
                    Carrera
                  </h2>

                  <div className="mt-4 space-y-3">
                    <div className="rounded-2xl border border-white/10 bg-white/6 px-3 py-3">
                      <div className="flex items-center justify-between text-[0.58rem] uppercase tracking-[0.18em] text-slate-400">
                        <span>Puntuacion total</span>
                        <span>{arcadeStats.totalScore}</span>
                      </div>
                      <div className="mt-3 h-2 overflow-hidden rounded-full bg-white/10">
                        <div
                          className="h-full rounded-full bg-linear-to-r from-accent-mint-300 via-[#f59e0b] to-[#ec4899] transition-all duration-300"
                          style={{ width: `${Math.min(100, Math.round((arcadeStats.totalScore / 220) * 100))}%` }}
                        />
                      </div>
                    </div>
                    <div className="rounded-2xl border border-white/10 bg-black/18 px-3 py-3">
                      <p className="text-[0.58rem] uppercase tracking-[0.2em] text-slate-400">Rango global</p>
                      <p className="mt-2 text-[0.92rem] font-semibold text-[#f5c6ff]">{careerRank}</p>
                    </div>
                    <NavLink
                      to="/achievements"
                      className="block rounded-2xl border border-white/15 bg-white/8 px-4 py-3 text-center text-[0.78rem] font-semibold text-white transition hover:bg-white/12"
                    >
                      Ver logros
                    </NavLink>
                  </div>
                </article>
              </div>
            </aside>
          </section>

          <section className="mx-auto flex h-full max-w-md items-center justify-center lg:hidden">
            <article className="rounded-[28px] border border-white/15 bg-[rgba(15,23,42,0.84)] p-6 text-center shadow-[0_24px_60px_rgba(7,12,24,0.45)] backdrop-blur-sm">
              <p className="text-[0.72rem] uppercase tracking-[0.28em] text-accent-beige-300">Arcade Astrais</p>
              <h1 className="mt-4 font-['Press_Start_2P'] text-lg text-white">Vista de escritorio</h1>
              <p className="mt-4 text-sm leading-6 text-slate-300">
                Esta cabina esta optimizada para escritorios medianos y grandes. Mejor marca actual: {arcadeStats.bestScore}.
              </p>
            </article>
          </section>
        </main>
      </div>

      <style>{`
        .games-stage {
          min-height: 0;
        }

        .retro-grid {
          background-image:
            linear-gradient(rgba(255, 255, 255, 0.06) 1px, transparent 1px),
            linear-gradient(90deg, rgba(255, 255, 255, 0.06) 1px, transparent 1px);
          background-size: 32px 32px;
          mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.8), transparent);
        }

        .scanlines {
          background-image: repeating-linear-gradient(
            180deg,
            rgba(255, 255, 255, 0.06) 0,
            rgba(255, 255, 255, 0.06) 1px,
            transparent 1px,
            transparent 4px
          );
        }

        .panel-glow::after {
          content: '';
          position: absolute;
          inset: 0;
          border-radius: inherit;
          box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.12);
          pointer-events: none;
        }

        .catalog-scroll {
          scrollbar-width: none;
          -ms-overflow-style: none;
        }

        .catalog-scroll::-webkit-scrollbar {
          display: none;
        }
      `}</style>
    </div>
  )
}
