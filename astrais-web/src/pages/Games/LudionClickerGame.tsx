import React from 'react'
import astra from '../../assets/astra.png'
import logo from '../../assets/logo_w.svg'
import { GAME_ROUND_COMPLETED_MESSAGE } from './gameMessages'
import { GAME_ROUND_SECONDS, getArenaRank, getLudionReward, type GameStatus } from './gameStorage'

interface LudionClickerGameProps {
  gameId: string
}

export default function LudionClickerGame({ gameId }: LudionClickerGameProps) {
  const [status, setStatus] = React.useState<GameStatus>('idle')
  const [score, setScore] = React.useState(0)
  const [timeLeft, setTimeLeft] = React.useState(GAME_ROUND_SECONDS)
  const [roundReward, setRoundReward] = React.useState(0)
  const [scorePulse, setScorePulse] = React.useState(false)
  const roundReportedRef = React.useRef(false)

  const elapsedSeconds = GAME_ROUND_SECONDS - timeLeft
  const clicksPerSecond = score > 0 ? (score / Math.max(1, elapsedSeconds)).toFixed(1) : '0.0'
  const roundRank = getArenaRank(score)

  const startRound = () => {
    roundReportedRef.current = false
    setStatus('playing')
    setScore(0)
    setTimeLeft(GAME_ROUND_SECONDS)
    setRoundReward(0)
  }

  const resetRound = () => {
    roundReportedRef.current = false
    setStatus('idle')
    setScore(0)
    setTimeLeft(GAME_ROUND_SECONDS)
    setRoundReward(0)
  }

  const handleArenaAction = () => {
    if (status === 'finished') {
      return
    }

    setScorePulse(true)

    if (status === 'idle') {
      roundReportedRef.current = false
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
    if (status !== 'playing' || timeLeft > 0 || roundReportedRef.current) {
      return
    }

    roundReportedRef.current = true
    setRoundReward(getLudionReward(score))
    setStatus('finished')

    window.parent.postMessage(
      {
        type: GAME_ROUND_COMPLETED_MESSAGE,
        gameId,
        score,
        durationSeconds: GAME_ROUND_SECONDS,
      },
      window.location.origin,
    )
  }, [gameId, score, status, timeLeft])

  return (
    <main className="relative h-screen min-h-128 overflow-hidden bg-[radial-gradient(circle_at_top_left,color-mix(in_srgb,var(--astrais-primary)_38%,transparent),transparent_34%),linear-gradient(140deg,var(--astrais-background),color-mix(in_srgb,var(--astrais-primary)_44%,var(--astrais-background))_52%,color-mix(in_srgb,var(--astrais-secondary)_32%,var(--astrais-background)))] p-4 font-['Space_Grotesk'] text-white">
      <div className="retro-grid pointer-events-none absolute inset-0 opacity-30" />
      <div className="scanlines pointer-events-none absolute inset-0 opacity-20" />
      <img
        src={astra}
        alt="Mascota Astrais"
        className="pointer-events-none absolute bottom-1 right-4 h-28 opacity-70 drop-shadow-[0_14px_28px_color-mix(in_srgb,var(--astrais-background)_58%,transparent)]"
      />

      <section className="relative z-10 grid h-full min-h-0 grid-rows-[auto_auto_minmax(0,1fr)_auto] gap-3">
        <header className="grid grid-cols-[minmax(0,1fr)_15rem] gap-3">
          <div>
            <p className="inline-flex items-center rounded-full border border-accent-beige-300/30 bg-white/8 px-3 py-1 text-[0.62rem] font-semibold uppercase tracking-[0.24em] text-accent-beige-300">
              Cabina activa
            </p>
            <h1 className="mt-3 font-['Press_Start_2P'] text-[clamp(1rem,2vw,1.35rem)] leading-tight">
              Atrapa Ludiones
            </h1>
            <p className="mt-3 max-w-xl text-[0.82rem] leading-5 text-slate-200">
              Pulsa el nucleo durante diez segundos. El resultado se envia al catalogo para actualizar logros y reserva.
            </p>
          </div>

          <div className="grid grid-cols-2 gap-2.5 self-center text-center">
            <div className="rounded-2xl border border-white/10 bg-black/24 px-3 py-3">
              <p className="text-[0.58rem] uppercase tracking-[0.22em] text-slate-300">Tiempo</p>
              <p className="mt-2 font-['Press_Start_2P'] text-[0.9rem] text-accent-beige-300">{timeLeft}s</p>
            </div>
            <div className="rounded-2xl border border-white/10 bg-black/24 px-3 py-3">
              <p className="text-[0.58rem] uppercase tracking-[0.22em] text-slate-300">Estado</p>
              <p className="mt-2 text-[0.74rem] font-semibold uppercase tracking-[0.14em]">
                {status === 'playing' ? 'Activo' : status === 'finished' ? 'Finalizado' : 'Listo'}
              </p>
            </div>
          </div>
        </header>

        <div className="grid grid-cols-3 gap-2.5">
          <div className="rounded-2xl border border-white/10 bg-white/8 px-3 py-3">
            <p className="text-[0.58rem] uppercase tracking-[0.2em] text-slate-300">Puntuacion</p>
            <p className={`mt-2 font-['Press_Start_2P'] text-[1rem] ${scorePulse ? 'score-pulse' : ''}`}>{score}</p>
          </div>
          <div className="rounded-2xl border border-white/10 bg-white/8 px-3 py-3">
            <p className="text-[0.58rem] uppercase tracking-[0.2em] text-slate-300">Ritmo</p>
            <p className="mt-2 text-[1rem] font-semibold text-accent-mint-300">{clicksPerSecond} cps</p>
          </div>
          <div className="rounded-2xl border border-white/10 bg-white/8 px-3 py-3">
            <p className="text-[0.58rem] uppercase tracking-[0.2em] text-slate-300">Rango</p>
            <p className="mt-2 text-[0.9rem] font-semibold text-[var(--astrais-rarity-epic)]">{roundRank}</p>
          </div>
        </div>

        <div className="relative flex min-h-0 items-center justify-center">
          <div className="pointer-events-none absolute bottom-8 left-1/2 h-16 w-64 -translate-x-1/2 rounded-full bg-secondary-500/20 blur-3xl" />
          <button
            type="button"
            onClick={handleArenaAction}
            disabled={status === 'finished'}
            className={`arena-ring relative flex h-[clamp(13rem,34vh,18rem)] w-[clamp(13rem,34vh,18rem)] flex-col items-center justify-center rounded-full border px-5 text-center transition duration-200 ${
              status === 'finished'
                ? 'cursor-not-allowed border-white/15 bg-black/28 text-slate-300'
                : 'cursor-pointer border-accent-beige-300/35 bg-[radial-gradient(circle_at_30%_30%,color-mix(in_srgb,var(--astrais-text)_24%,transparent),color-mix(in_srgb,var(--astrais-primary)_34%,transparent),color-mix(in_srgb,var(--astrais-background)_94%,transparent))] hover:scale-[1.015] active:scale-[0.985]'
            }`}
          >
            <img src={logo} alt="Astrais token" className="mb-3 h-14 w-14 drop-shadow-[0_0_14px_color-mix(in_srgb,var(--astrais-text)_35%,transparent)]" />
            <span className="font-['Press_Start_2P'] text-[0.74rem] leading-5">
              {status === 'idle' && 'Pulsa para empezar'}
              {status === 'playing' && '+1 ludion'}
              {status === 'finished' && 'Partida cerrada'}
            </span>
            <span className="mt-3 max-w-44 text-[0.6rem] uppercase tracking-[0.22em] text-slate-200">
              {status === 'playing'
                ? 'Mantente en ritmo'
                : status === 'finished'
                  ? `Premio: ${roundReward}`
                  : 'Primer toque inicia el contador'}
            </span>
          </button>
        </div>

        <footer className="flex items-end justify-between gap-3">
          <div className="max-w-96 rounded-2xl border border-white/10 bg-black/22 px-4 py-3 text-[0.78rem] leading-5 text-slate-200">
            {status === 'playing' && 'La ronda esta en marcha. Cada clic cuenta.'}
            {status === 'idle' && 'Todo listo: el iframe guarda el resultado al terminar.'}
            {status === 'finished' && `Resultado enviado: ${score} puntos y ${roundReward} ludiones calculados.`}
          </div>

          {status !== 'playing' ? (
            <button
              type="button"
              onClick={startRound}
              className="rounded-2xl border-0 [background:var(--astrais-cta-bg)] px-4 py-3 text-[0.78rem] font-semibold text-white shadow-[0_16px_28px_color-mix(in_srgb,var(--astrais-rarity-epic)_24%,transparent)] transition hover:-translate-y-0.5"
            >
              {status === 'finished' ? 'Nueva ronda' : 'Empezar'}
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
        </footer>
      </section>

      <style>{`
        .retro-grid {
          background-image:
            linear-gradient(color-mix(in srgb, var(--astrais-text) 6%, transparent) 1px, transparent 1px),
            linear-gradient(90deg, color-mix(in srgb, var(--astrais-text) 6%, transparent) 1px, transparent 1px);
          background-size: 32px 32px;
          mask-image: linear-gradient(to bottom, color-mix(in srgb, var(--astrais-background) 80%, transparent), transparent);
        }

        .scanlines {
          background-image: repeating-linear-gradient(
            180deg,
            color-mix(in srgb, var(--astrais-text) 6%, transparent) 0,
            color-mix(in srgb, var(--astrais-text) 6%, transparent) 1px,
            transparent 1px,
            transparent 4px
          );
        }

        .arena-ring {
          box-shadow:
            0 0 0 1px color-mix(in srgb, var(--astrais-text) 8%, transparent),
            0 0 36px color-mix(in srgb, var(--astrais-primary) 24%, transparent),
            inset 0 0 34px color-mix(in srgb, var(--astrais-text) 8%, transparent);
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
    </main>
  )
}
