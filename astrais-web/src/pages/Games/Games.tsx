import React from 'react'
import { NavLink } from 'react-router'
import Navbar from '../../components/layout/Navbar'
import astra from '../../assets/astra.png'
import bgImage from '../../assets/homeScreenBack.jpg'
import gamePreview from '../../assets/game.png'
import logo from '../../assets/logo_w.svg'
import {
  GAME_ROUND_SECONDS,
  getArenaRank,
  getLudionReward,
  readClickerStats,
  type ClickerStats,
  type GameStatus,
  writeClickerStats,
} from './gameStorage'

const upcomingGames = [
  {
    title: 'Nebula Dash',
    description: 'Meteoros, aceleracion y premios por reflejos.',
  },
  {
    title: 'Astra Memory',
    description: 'Patrones rapidos para sesiones de precision.',
  },
]

export default function Games() {
  const [careerStats, setCareerStats] = React.useState<ClickerStats>(() => readClickerStats())
  const [status, setStatus] = React.useState<GameStatus>('idle')
  const [score, setScore] = React.useState(0)
  const [timeLeft, setTimeLeft] = React.useState(GAME_ROUND_SECONDS)
  const [roundReward, setRoundReward] = React.useState(() => getLudionReward(readClickerStats().lastScore))
  const [scorePulse, setScorePulse] = React.useState(false)

  const elapsedSeconds = GAME_ROUND_SECONDS - timeLeft
  const clicksPerSecond = score > 0 ? (score / Math.max(1, elapsedSeconds)).toFixed(1) : '0.0'
  const sessionHeat = Math.min(100, Math.round((score / Math.max(1, GAME_ROUND_SECONDS)) * 100))
  const roundRank = getArenaRank(score)
  const careerRank = getArenaRank(careerStats.bestScore)

  const startRound = () => {
    setStatus('playing')
    setScore(0)
    setTimeLeft(GAME_ROUND_SECONDS)
    setRoundReward(0)
  }

  const resetRound = () => {
    setStatus('idle')
    setScore(0)
    setTimeLeft(GAME_ROUND_SECONDS)
  }

  const handleArenaAction = () => {
    if (status === 'finished') {
      return
    }

    setScorePulse(true)

    if (status === 'idle') {
      setStatus('playing')
      setTimeLeft(GAME_ROUND_SECONDS)
      setRoundReward(0)
      setScore(1)
      return
    }

    setScore((currentScore) => currentScore + 1)
  }

  React.useEffect(() => {
    if (!scorePulse) {
      return
    }

    const pulseTimeout = window.setTimeout(() => setScorePulse(false), 180)
    return () => window.clearTimeout(pulseTimeout)
  }, [scorePulse])

  React.useEffect(() => {
    if (status !== 'playing' || timeLeft <= 0) {
      return
    }

    const timer = window.setTimeout(() => {
      setTimeLeft((remainingTime) => Math.max(remainingTime - 1, 0))
    }, 1000)

    return () => window.clearTimeout(timer)
  }, [status, timeLeft])

  React.useEffect(() => {
    if (status !== 'playing' || timeLeft > 0) {
      return
    }

    const reward = getLudionReward(score)
    setRoundReward(reward)
    setStatus('finished')

    setCareerStats((previousStats) => {
      const updatedStats = {
        bestScore: Math.max(previousStats.bestScore, score),
        lastScore: score,
        gamesPlayed: previousStats.gamesPlayed + 1,
        totalClicks: previousStats.totalClicks + score,
        totalLudionsEarned: previousStats.totalLudionsEarned + reward,
      }

      writeClickerStats(updatedStats)
      return updatedStats
    })
  }, [score, status, timeLeft])

  return (
    <div
      style={{ backgroundImage: `url(${bgImage})` }}
      className="relative h-screen w-screen overflow-hidden bg-cover bg-center font-['Space_Grotesk'] text-white"
    >
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(139,92,246,0.30),transparent_36%),radial-gradient(circle_at_bottom_right,rgba(59,130,246,0.24),transparent_40%)]" />
      <div className="pointer-events-none absolute inset-0 bg-black/60" />
      <div className="scanlines pointer-events-none absolute inset-0 opacity-25" />

      <div className="relative z-10 flex h-full w-full min-h-0 flex-col">
        <Navbar />

        <main className="flex min-h-0 flex-1 px-3 pb-3 pt-1 md:px-4 md:pb-4 xl:px-6 xl:pb-5">
          <section className="games-stage mx-auto hidden h-full w-full gap-3 lg:grid lg:grid-cols-[minmax(0,1.18fr)_minmax(18.5rem,0.88fr)] min-[1400px]:gap-4 min-[1400px]:grid-cols-[minmax(0,1.36fr)_minmax(21rem,0.92fr)]">
            <article className="relative grid grid-cols-12 overflow-hidden rounded-[28px] 
            border border-white/15 
            bg-[linear-gradient(150deg,rgba(15,23,42,0.86),rgba(76,29,149,0.62),rgba(30,74,99,0.74))] 
            shadow-[0_20px_60px_rgba(7,12,24,0.52)] min-[1400px]:p-5">
              <div className="retro-grid absolute inset-0 opacity-35" />
              <div className="pointer-events-none absolute -left-16 top-5 h-44 w-44 rounded-full bg-secondary-500/18 blur-3xl" />
              <div className="pointer-events-none absolute bottom-0 right-0 h-48 w-48 rounded-full bg-primary-500/18 blur-3xl" />

              <div className='col-span-3 h-full'>
                Sección de videojuegos
              </div>

              <div className='col-span-9 h-full grid content-between'>
                <header className="relativ z-10 grid grid-cols-[minmax(0,1fr)_15.5rem] gap-3 min-[1400px]:grid-cols-[minmax(0,1fr)_18rem] min-[1400px]:gap-4">
                  <div>
                    <p className="inline-flex items-center rounded-full border border-accent-beige-300/30 bg-white/8 px-3 py-1 text-[0.64rem] font-semibold uppercase tracking-[0.28em] text-accent-beige-300">
                      Arcade Astrais
                    </p>
                    <h1 className="mt-3 font-['Press_Start_2P'] text-[clamp(1rem,1.6vw,1.4rem)] leading-tight text-white">
                      Atrapa Ludiones
                    </h1>
                    <p className="mt-3 max-w-xl text-[0.84rem] leading-5 text-slate-200 xl:text-[0.92rem] xl:leading-6">
                      Una cabina arcade corta y directa para estas ventanas de escritorio. Diez segundos, ritmo alto y
                      recompensa instantanea sin salir del panel.
                    </p>
                  </div>

                  <div className="grid grid-cols-2 my-auto text-center gap-2.5 min-[1400px]:gap-3">
                    <div className="rounded-2xl border border-white/10 bg-black/22 px-3 py-3">
                      <p className="text-[0.62rem] uppercase tracking-[0.24em] text-slate-300">Tiempo</p>
                      <p className="mt-2 font-['Press_Start_2P'] text-[0.88rem] text-accent-beige-300 xl:text-[1rem]">{timeLeft}s</p>
                    </div>
                    <div className="rounded-2xl border border-white/10 bg-black/22 px-3 py-3">
                      <p className="text-[0.62rem] uppercase tracking-[0.24em] text-slate-300">Estado</p>
                      <p className="mt-2 text-[0.75rem] font-semibold uppercase tracking-[0.14em] text-white xl:text-[0.82rem]">
                        {status === 'playing' ? 'Activo' : status === 'finished' ? 'Finalizado' : 'Listo'}
                      </p>
                    </div>
                  </div>
                </header>

                <div className="relative z-10 mt-3 grid grid-cols-3 gap-2.5 min-[1400px]:gap-3">
                  <div className="rounded-2xl border border-white/10 bg-white/8 px-3 py-3">
                    <p className="text-[0.62rem] uppercase tracking-[0.22em] text-slate-300">Puntuacion</p>
                    <p className={`mt-2 font-['Press_Start_2P'] text-[1rem] text-white xl:text-[1.2rem] ${scorePulse ? 'score-pulse' : ''}`}>
                      {score}
                    </p>
                  </div>
                  <div className="rounded-2xl border border-white/10 bg-white/8 px-3 py-3">
                    <p className="text-[0.62rem] uppercase tracking-[0.22em] text-slate-300">Ritmo</p>
                    <p className="mt-2 text-[0.95rem] font-semibold text-accent-mint-300 xl:text-[1.05rem]">{clicksPerSecond} cps</p>
                  </div>
                  <div className="rounded-2xl border border-white/10 bg-white/8 px-3 py-3">
                    <p className="text-[0.62rem] uppercase tracking-[0.22em] text-slate-300">Rango</p>
                    <p className="mt-2 text-[0.92rem] font-semibold text-[#f5c6ff] xl:text-[1rem]">{roundRank}</p>
                  </div>
                </div>

                <div className="relative z-10 mt-3 flex min-h-0 items-center justify-center">
                  <img
                    src={astra}
                    alt="Mascota Astrais"
                    className="pointer-events-none absolute -bottom-24 right-8 hidden h-[clamp(6.5rem,15vh,9rem)] opacity-75 drop-shadow-[0_14px_26px_rgba(15,23,42,0.55)] min-[1400px]:block"
                  />
                  <div className="pointer-events-none absolute bottom-4 left-1/2 h-14 w-60 -translate-x-1/2 rounded-full bg-secondary-500/18 blur-3xl" />

                  <button
                    type="button"
                    onClick={handleArenaAction}
                    disabled={status === 'finished'}
                    className={`arena-ring relative flex h-[clamp(12.5rem,29vh,17rem)] w-[clamp(12.5rem,29vh,17rem)] flex-col items-center justify-center rounded-full border px-4 text-center transition duration-200 ${
                      status === 'finished'
                        ? 'cursor-not-allowed border-white/15 bg-black/25 text-slate-300'
                        : 'cursor-pointer border-accent-beige-300/35 bg-[radial-gradient(circle_at_30%_30%,rgba(255,255,255,0.22),rgba(139,92,246,0.30),rgba(15,23,42,0.92))] text-white hover:scale-[1.015] active:scale-[0.985]'
                    }`}
                  >
                    <img src={logo} alt="Astrais token" className="mb-3 h-12 w-12 drop-shadow-[0_0_14px_rgba(255,255,255,0.35)] xl:h-14 xl:w-14" />
                    <span className="font-['Press_Start_2P'] text-[0.72rem] leading-5 xl:text-[0.78rem]">
                      {status === 'idle' && 'Pulsa para empezar'}
                      {status === 'playing' && '+1 ludion'}
                      {status === 'finished' && 'Partida cerrada'}
                    </span>
                    <span className="mt-3 max-w-44 text-[0.6rem] uppercase tracking-[0.22em] text-slate-200 xl:text-[0.64rem]">
                      {status === 'playing'
                        ? 'Mantente en ritmo'
                        : status === 'finished'
                          ? 'Lanza otra ronda'
                          : 'Primer toque inicia el contador'}
                    </span>
                  </button>
                </div>

                <footer className="relative z-10 mt-3 flex items-end justify-between gap-2.5 min-[1400px]:gap-3">
                  <div className="max-w-88 rounded-2xl border border-white/10 bg-black/18 px-4 py-3 text-[0.8rem] leading-5 text-slate-200 xl:text-[0.88rem] xl:leading-6">
                    {status === 'playing' && 'La ronda esta en marcha. Cada clic cuenta.'}
                    {status === 'idle' && 'Todo listo para que empieces la partida.'}
                    {status === 'finished' &&
                      `Premio actual: ${roundReward} ludiones.`}
                  </div>

                  <div className="flex items-center gap-2.5 min-[1400px]:gap-3">
                    {status !== 'playing' ? (
                      <button
                        type="button"
                        onClick={startRound}
                        className="rounded-2xl border-0 bg-linear-to-r from-[#f97316] via-[#ec4899] to-[#8b5cf6] px-4 py-3 text-[0.78rem] font-semibold text-white shadow-[0_16px_28px_rgba(236,72,153,0.24)] transition hover:-translate-y-0.5"
                      >
                        {status === 'finished' ? 'Nueva ronda' : '¡Empezar!'}
                      </button>
                    ) : (
                      <button
                        type="button"
                        onClick={resetRound}
                        className="rounded-2xl border border-white/20 bg-white/10 px-4 py-3 text-[0.78rem] font-semibold text-white transition hover:bg-white/15"
                      >
                        Cancelar
                      </button>
                    )}

                    <NavLink
                      to="/achievements"
                      className="rounded-2xl border border-white/20 bg-black/18 px-4 py-3 text-[0.78rem] font-semibold text-white transition hover:border-accent-beige-300/50 hover:bg-white/10"
                    >
                      Ver logros
                    </NavLink>
                  </div>
                </footer>
              </div>
              
            </article>

            <aside className="grid min-h-0 grid-rows-[auto_minmax(0,1fr)] gap-3 min-[1400px]:gap-4">
              <article className="panel-glow relative overflow-hidden rounded-[26px] border border-white/15 bg-[linear-gradient(160deg,rgba(15,23,42,0.88),rgba(30,74,99,0.74))] p-3.5 shadow-[0_18px_44px_rgba(7,12,24,0.42)] min-[1400px]:p-4">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-[0.62rem] uppercase tracking-[0.24em] text-accent-beige-300">Panel de sesion</p>
                    <h2 className="mt-2 font-['Press_Start_2P'] text-[0.92rem] text-white xl:text-[1rem]">Cabina activa</h2>
                    <p className="mt-2 text-[0.78rem] leading-5 text-slate-300 xl:text-[0.84rem]">
                      Todo queda guardado en local y listo para tu siguiente visita.
                    </p>
                  </div>
                  <img
                    src={gamePreview}
                    alt="Vista previa del minijuego"
                    className="h-16 w-16 rounded-2xl pl-3 border-white/5 object-cover 
                              shadow-[0_14px_24px_rgba(15,23,42,0.2)] min-[1400px]:h-24 min-[1400px]:w-42
                               transform scale-x-[-1]"
                  />
                </div>

                <div className="mt-4 grid grid-cols-3 gap-2.5 min-[1400px]:gap-3">
                  <div className="rounded-2xl border border-white/10 bg-white/8 p-3">
                    <p className="text-[0.58rem] uppercase tracking-[0.22em] text-slate-400">Partidas</p>
                    <p className="mt-2 text-[1.05rem] font-semibold text-white xl:text-[1.18rem]">{careerStats.gamesPlayed}</p>
                  </div>
                  <div className="rounded-2xl border border-white/10 bg-white/8 p-3">
                    <p className="text-[0.58rem] uppercase tracking-[0.22em] text-slate-400">Mejor</p>
                    <p className="mt-2 text-[1.05rem] font-semibold text-accent-mint-300 xl:text-[1.18rem]">{careerStats.bestScore}</p>
                  </div>
                  <div className="rounded-2xl border border-white/10 bg-white/8 p-3">
                    <p className="text-[0.58rem] uppercase tracking-[0.22em] text-slate-400">Reserva</p>
                    <p className="mt-2 text-[1.05rem] font-semibold text-[#f5c6ff] xl:text-[1.18rem]">{careerStats.totalLudionsEarned}</p>
                  </div>
                </div>
              </article>

              <div className="grid min-h-0 grid-cols-2 gap-3 min-[1400px]:gap-4">
                <article className="panel-glow relative min-h-0 overflow-hidden rounded-3xl border border-white/15 bg-[linear-gradient(180deg,rgba(15,23,42,0.82),rgba(76,29,149,0.46))] p-3.5 shadow-[0_18px_42px_rgba(7,12,24,0.38)] min-[1400px]:p-4">
                  <p className="text-[0.62rem] uppercase tracking-[0.24em] text-accent-beige-300">Recompensas</p>
                  <h2 className="mt-2 font-['Press_Start_2P'] text-[0.86rem] text-white xl:text-[0.94rem]">Historial</h2>

                  <div className="mt-4 space-y-3">
                    <div className="rounded-2xl border border-white/10 bg-black/18 px-3 py-3">
                      <p className="text-[0.58rem] uppercase tracking-[0.22em] text-slate-400">Ultima ronda</p>
                      <p className="mt-2 text-[0.95rem] font-semibold text-white xl:text-[1.05rem]">{careerStats.lastScore} puntos</p>
                    </div>
                    <div className="rounded-2xl border border-white/10 bg-black/18 px-3 py-3">
                      <p className="text-[0.58rem] uppercase tracking-[0.22em] text-slate-400">Rango de carrera</p>
                      <p className="mt-2 text-[0.92rem] font-semibold text-[#f5c6ff] xl:text-[1rem]">{careerRank}</p>
                    </div>
                    <div className="rounded-2xl border border-white/10 bg-black/18 px-3 py-3">
                      <div className="flex items-center justify-between text-[0.58rem] uppercase tracking-[0.22em] text-slate-400">
                        <span>Intensidad</span>
                        <span>{sessionHeat}%</span>
                      </div>
                      <div className="mt-3 h-2 overflow-hidden rounded-full bg-white/10">
                        <div
                          className="h-full rounded-full bg-linear-to-r from-accent-mint-300 via-[#f59e0b] to-[#ec4899] transition-all duration-300"
                          style={{ width: `${sessionHeat}%` }}
                        />
                      </div>
                    </div>
                  </div>
                </article>

                <article className="panel-glow relative min-h-0 overflow-hidden rounded-3xl border border-white/15 bg-[linear-gradient(180deg,rgba(15,23,42,0.84),rgba(30,41,59,0.82))] p-3.5 shadow-[0_18px_42px_rgba(7,12,24,0.38)] min-[1400px]:p-4">
                  <p className="text-[0.62rem] uppercase tracking-[0.24em] text-accent-beige-300">Proximamente</p>
                  <h2 className="mt-2 font-['Press_Start_2P'] text-[0.86rem] text-white xl:text-[0.94rem]">Siguientes cabinas</h2>

                  <div className="mt-4 space-y-3">
                    {upcomingGames.map((upcomingGame) => (
                      <div key={upcomingGame.title} className="rounded-2xl border border-white/10 bg-white/6 px-3 py-3">
                        <div className="flex items-start justify-between gap-3">
                          <div>
                            <p className="text-[0.82rem] font-semibold text-white xl:text-[0.88rem]">{upcomingGame.title}</p>
                            <p className="mt-2 text-[0.74rem] leading-5 text-slate-300 xl:text-[0.8rem]">{upcomingGame.description}</p>
                          </div>
                          <span className="rounded-full border border-white/15 bg-black/25 px-2 py-1 text-[0.58rem] uppercase tracking-[0.18em] text-slate-400">
                            Locked
                          </span>
                        </div>
                      </div>
                    ))}
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
                Esta cabina esta optimizada para escritorios medianos y grandes. Mejor marca actual: {careerStats.bestScore}.
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

        .arena-ring {
          box-shadow:
            0 0 0 1px rgba(255, 255, 255, 0.08),
            0 0 36px rgba(168, 85, 247, 0.24),
            inset 0 0 34px rgba(255, 255, 255, 0.08);
        }

        .score-pulse {
          animation: scorePulse 180ms ease-out;
        }

        @keyframes scorePulse {
          0% { transform: scale(1); }
          45% { transform: scale(1.08); }
          100% { transform: scale(1); }
        }
      `}</style>
    </div>
  )
}
