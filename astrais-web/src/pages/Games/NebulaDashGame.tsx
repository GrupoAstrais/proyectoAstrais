import React from 'react'
import { GAME_ROUND_COMPLETED_MESSAGE } from './gameMessages'

type DashStatus = 'idle' | 'playing' | 'finished'
type FallingItemKind = 'meteor' | 'fragment'

interface FallingItem {
  id: number
  lane: number
  top: number
  kind: FallingItemKind
  scored: boolean
}

interface NebulaDashGameProps {
  gameId: string
}

const DASH_SECONDS = 30
const LANES = 5
const ITEM_SPEED = 12
const PLAYER_ROW = 82
const HIT_ZONE = 9

function clampLane(lane: number) {
  return Math.max(0, Math.min(LANES - 1, lane))
}

export default function NebulaDashGame({ gameId }: NebulaDashGameProps) {
  const [status, setStatus] = React.useState<DashStatus>('idle')
  const [lane, setLane] = React.useState(2)
  const [timeLeft, setTimeLeft] = React.useState(DASH_SECONDS)
  const [score, setScore] = React.useState(0)
  const [shields, setShields] = React.useState(3)
  const [items, setItems] = React.useState<FallingItem[]>([])
  const [lastEvent, setLastEvent] = React.useState('Motores en espera')
  const nextItemId = React.useRef(1)
  const reportedRound = React.useRef(false)

  const finishRound = React.useCallback((finalScore: number) => {
    if (reportedRound.current) {
      return
    }

    reportedRound.current = true
    setStatus('finished')
    setLastEvent('Resultado enviado al catalogo')
    window.parent.postMessage(
      {
        type: GAME_ROUND_COMPLETED_MESSAGE,
        gameId,
        score: Math.max(0, finalScore),
        durationSeconds: DASH_SECONDS,
      },
      window.location.origin,
    )
  }, [gameId])

  const startRound = () => {
    reportedRound.current = false
    nextItemId.current = 1
    setStatus('playing')
    setLane(2)
    setTimeLeft(DASH_SECONDS)
    setScore(0)
    setShields(3)
    setItems([])
    setLastEvent('Carrera iniciada')
  }

  const moveLane = React.useCallback((direction: -1 | 1) => {
    setLane((currentLane) => clampLane(currentLane + direction))
  }, [])

  React.useEffect(() => {
    if (status !== 'playing') {
      return
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'ArrowLeft' || event.key.toLowerCase() === 'a') {
        event.preventDefault()
        moveLane(-1)
      }

      if (event.key === 'ArrowRight' || event.key.toLowerCase() === 'd') {
        event.preventDefault()
        moveLane(1)
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [moveLane, status])

  React.useEffect(() => {
    if (status !== 'playing') {
      return
    }

    const timer = window.setInterval(() => {
      setTimeLeft((currentTime) => {
        if (currentTime <= 1) {
          finishRound(score)
          return 0
        }

        setScore((currentScore) => currentScore + 2)
        return currentTime - 1
      })
    }, 1000)

    return () => window.clearInterval(timer)
  }, [finishRound, score, status])

  React.useEffect(() => {
    if (status !== 'playing') {
      return
    }

    const spawner = window.setInterval(() => {
      setItems((currentItems) => [
        ...currentItems,
        {
          id: nextItemId.current++,
          lane: Math.floor(Math.random() * LANES),
          top: -8,
          kind: Math.random() > 0.28 ? 'meteor' : 'fragment',
          scored: false,
        },
      ])
    }, 760)

    return () => window.clearInterval(spawner)
  }, [status])

  React.useEffect(() => {
    if (status !== 'playing') {
      return
    }

    const ticker = window.setInterval(() => {
      setItems((currentItems) => {
        const nextItems: FallingItem[] = []
        let scoreDelta = 0
        let shieldHits = 0
        let eventText = ''

        currentItems.forEach((item) => {
          const movedItem = { ...item, top: item.top + ITEM_SPEED }
          const inHitZone = Math.abs(movedItem.top - PLAYER_ROW) <= HIT_ZONE

          if (inHitZone && movedItem.lane === lane) {
            if (movedItem.kind === 'fragment') {
              scoreDelta += 8
              eventText = '+8 fragmento estelar'
            } else {
              shieldHits += 1
              eventText = 'Impacto: escudo perdido'
            }
            return
          }

          if (movedItem.top > 100) {
            if (movedItem.kind === 'meteor' && !movedItem.scored) {
              scoreDelta += 3
              eventText = '+3 meteorito esquivado'
            }
            return
          }

          nextItems.push(movedItem)
        })

        if (scoreDelta > 0) {
          setScore((currentScore) => currentScore + scoreDelta)
        }

        if (shieldHits > 0) {
          setShields((currentShields) => {
            const nextShields = Math.max(0, currentShields - shieldHits)

            if (nextShields <= 0) {
              setScore((currentScore) => {
                finishRound(currentScore)
                return currentScore
              })
            }

            return nextShields
          })
        }

        if (eventText) {
          setLastEvent(eventText)
        }

        return nextItems
      })
    }, 280)

    return () => window.clearInterval(ticker)
  }, [finishRound, lane, status])

  return (
    <main className="relative h-screen min-h-112 overflow-hidden bg-transparent p-4 font-['Space_Grotesk'] text-white">
      <div className="dash-stars pointer-events-none absolute inset-0 opacity-40" />
      <section className="relative z-10 grid h-full min-h-0 grid-rows-[auto_minmax(0,1fr)_auto] gap-3">
        <header className="grid grid-cols-[minmax(0,1fr)_18rem] gap-3">
          <div>
            <p className="inline-flex rounded-full border border-accent-mint-300/30 bg-accent-mint-300/10 px-3 py-1 text-[0.62rem] font-semibold uppercase tracking-[0.24em] text-accent-mint-300">
              Carrera orbital
            </p>
            <h1 className="mt-3 font-['Press_Start_2P'] text-[clamp(1rem,2vw,1.35rem)] leading-tight">Nebula Dash</h1>
            <p className="mt-3 max-w-xl text-[0.82rem] leading-5 text-slate-200">
              Cambia de carril con A/D o flechas, recoge fragmentos y deja que los meteoritos pasen lejos de tu nave.
            </p>
          </div>

          <div className="grid grid-cols-3 gap-2 self-center text-center">
            <div className="rounded-2xl border border-white/10 bg-black/24 p-3">
              <p className="text-[0.55rem] uppercase tracking-[0.18em] text-slate-400">Tiempo</p>
              <p className="mt-2 font-['Press_Start_2P'] text-[0.82rem] text-accent-beige-300">{timeLeft}s</p>
            </div>
            <div className="rounded-2xl border border-white/10 bg-black/24 p-3">
              <p className="text-[0.55rem] uppercase tracking-[0.18em] text-slate-400">Puntos</p>
              <p className="mt-2 text-[1rem] font-semibold text-white">{score}</p>
            </div>
            <div className="rounded-2xl border border-white/10 bg-black/24 p-3">
              <p className="text-[0.55rem] uppercase tracking-[0.18em] text-slate-400">Escudos</p>
              <p className="mt-2 text-[1rem] font-semibold text-accent-mint-300">{shields}</p>
            </div>
          </div>
        </header>

        <div className="relative min-h-0 overflow-hidden rounded-[26px] border border-white/15 bg-[linear-gradient(180deg,color-mix(in_srgb,var(--astrais-background)_80%,transparent),color-mix(in_srgb,var(--astrais-secondary)_24%,var(--astrais-surface)_76%))]">
          <div className="absolute inset-x-5 top-0 bottom-0 grid grid-cols-5 gap-2">
            {Array.from({ length: LANES }).map((_, laneIndex) => (
              <div key={laneIndex} className="relative border-x border-white/8 bg-white/[0.025]">
                <div className="absolute inset-x-0 top-[82%] h-px bg-accent-mint-300/35" />
              </div>
            ))}
          </div>

          {items.map((item) => (
            <div
              key={item.id}
              className={`absolute flex h-9 w-9 -translate-x-1/2 items-center justify-center rounded-full border text-lg shadow-[0_0_18px_color-mix(in_srgb,var(--astrais-text)_16%,transparent)] ${
                item.kind === 'fragment'
                  ? 'border-accent-mint-300/55 bg-accent-mint-300/18 text-accent-mint-300'
                  : 'border-[color-mix(in_srgb,var(--astrais-rarity-legendary)_45%,transparent)] bg-[color-mix(in_srgb,var(--astrais-rarity-legendary)_16%,transparent)] text-[var(--astrais-rarity-legendary)]'
              }`}
              style={{ left: `${10 + item.lane * 20}%`, top: `${item.top}%` }}
            >
              {item.kind === 'fragment' ? '*' : 'o'}
            </div>
          ))}

          <div
            className="absolute top-[82%] flex h-12 w-12 -translate-x-1/2 -translate-y-1/2 items-center justify-center rounded-[18px] border border-accent-mint-300/55 bg-[radial-gradient(circle_at_35%_30%,color-mix(in_srgb,var(--astrais-text)_35%,transparent),color-mix(in_srgb,var(--astrais-tertiary)_30%,transparent),color-mix(in_srgb,var(--astrais-background)_90%,transparent))] font-['Press_Start_2P'] text-[0.7rem] text-white shadow-[0_0_28px_color-mix(in_srgb,var(--astrais-tertiary)_28%,transparent)] transition-all duration-150"
            style={{ left: `${10 + lane * 20}%` }}
          >
            A
          </div>

          {status !== 'playing' ? (
            <div className="absolute inset-0 grid place-items-center bg-black/32 backdrop-blur-[1px]">
              <div className="max-w-md rounded-[26px] border border-white/15 bg-slate-950/82 p-6 text-center shadow-[0_20px_50px_color-mix(in_srgb,var(--astrais-background)_42%,transparent)]">
                <p className="text-[0.64rem] uppercase tracking-[0.24em] text-accent-beige-300">
                  {status === 'finished' ? 'Carrera registrada' : 'Preparado para despegar'}
                </p>
                <h2 className="mt-4 font-['Press_Start_2P'] text-lg">{status === 'finished' ? `${score} puntos` : '30 segundos'}</h2>
                <button
                  type="button"
                  onClick={startRound}
                  className="mt-5 rounded-2xl bg-linear-to-r from-accent-mint-300 via-accent-mint-500 to-secondary-500 px-5 py-3 text-[0.78rem] font-semibold text-slate-950 transition hover:-translate-y-0.5"
                >
                  {status === 'finished' ? 'Nueva carrera' : 'Empezar'}
                </button>
              </div>
            </div>
          ) : null}
        </div>

        <footer className="flex items-center justify-between gap-3">
          <div className="rounded-2xl border border-white/10 bg-black/24 px-4 py-3 text-[0.76rem] text-slate-200">
            {lastEvent}
          </div>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => moveLane(-1)}
              disabled={status !== 'playing'}
              className="rounded-2xl border border-white/15 bg-white/10 px-5 py-3 font-semibold text-white transition hover:bg-white/15 disabled:cursor-not-allowed disabled:opacity-45"
            >
              Left
            </button>
            <button
              type="button"
              onClick={() => moveLane(1)}
              disabled={status !== 'playing'}
              className="rounded-2xl border border-white/15 bg-white/10 px-5 py-3 font-semibold text-white transition hover:bg-white/15 disabled:cursor-not-allowed disabled:opacity-45"
            >
              Right
            </button>
          </div>
        </footer>
      </section>

      <style>{`
        .dash-stars {
          background-image:
            radial-gradient(circle, color-mix(in srgb, var(--astrais-text) 55%, transparent) 1px, transparent 1px),
            radial-gradient(circle, color-mix(in srgb, var(--astrais-tertiary) 50%, transparent) 1px, transparent 1px);
          background-position: 0 0, 18px 28px;
          background-size: 54px 54px, 72px 72px;
        }
      `}</style>
    </main>
  )
}
