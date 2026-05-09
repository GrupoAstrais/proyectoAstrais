import React from 'react'
import { GAME_ROUND_COMPLETED_MESSAGE } from './gameMessages'

type MemoryStatus = 'idle' | 'showing' | 'input' | 'finished'

interface AstraMemoryGameProps {
  gameId: string
}

const PAD_COUNT = 4
const INITIAL_SEQUENCE_LENGTH = 3
const MAX_MISTAKES = 3
const SHOW_STEP_MS = 560
const SHOW_GAP_MS = 180

const pads = [
  {
    label: 'Norte',
    shortLabel: 'N',
    className: 'border-[color-mix(in_srgb,var(--astrais-secondary)_50%,transparent)] bg-[color-mix(in_srgb,var(--astrais-secondary)_14%,transparent)] text-[var(--astrais-secondary)]',
    activeClassName: 'border-[var(--astrais-secondary)] bg-[color-mix(in_srgb,var(--astrais-secondary)_45%,transparent)] shadow-[0_0_34px_color-mix(in_srgb,var(--astrais-secondary)_34%,transparent)]',
  },
  {
    label: 'Este',
    shortLabel: 'E',
    className: 'border-accent-mint-300/50 bg-accent-mint-300/14 text-accent-mint-300',
    activeClassName: 'border-accent-mint-300 bg-accent-mint-300/42 shadow-[0_0_34px_color-mix(in_srgb,var(--astrais-tertiary)_34%,transparent)]',
  },
  {
    label: 'Sur',
    shortLabel: 'S',
    className: 'border-[color-mix(in_srgb,var(--astrais-rarity-legendary)_50%,transparent)] bg-[color-mix(in_srgb,var(--astrais-rarity-legendary)_14%,transparent)] text-[var(--astrais-rarity-legendary)]',
    activeClassName: 'border-[var(--astrais-rarity-legendary)] bg-[color-mix(in_srgb,var(--astrais-rarity-legendary)_42%,transparent)] shadow-[0_0_34px_color-mix(in_srgb,var(--astrais-rarity-legendary)_34%,transparent)]',
  },
  {
    label: 'Oeste',
    shortLabel: 'O',
    className: 'border-[color-mix(in_srgb,var(--astrais-rarity-epic)_50%,transparent)] bg-[color-mix(in_srgb,var(--astrais-rarity-epic)_14%,transparent)] text-[var(--astrais-rarity-epic)]',
    activeClassName: 'border-[var(--astrais-rarity-epic)] bg-[color-mix(in_srgb,var(--astrais-rarity-epic)_42%,transparent)] shadow-[0_0_34px_color-mix(in_srgb,var(--astrais-rarity-epic)_34%,transparent)]',
  },
]

function randomPad() {
  return Math.floor(Math.random() * PAD_COUNT)
}

function wait(ms: number) {
  return new Promise((resolve) => {
    window.setTimeout(resolve, ms)
  })
}

export default function AstraMemoryGame({ gameId }: AstraMemoryGameProps) {
  const [status, setStatus] = React.useState<MemoryStatus>('idle')
  const [sequence, setSequence] = React.useState<number[]>([])
  const [inputIndex, setInputIndex] = React.useState(0)
  const [round, setRound] = React.useState(0)
  const [mistakes, setMistakes] = React.useState(0)
  const [score, setScore] = React.useState(0)
  const [activePad, setActivePad] = React.useState<number | null>(null)
  const [message, setMessage] = React.useState('Memoriza la energia de los pads')
  const runToken = React.useRef(0)
  const reportedRound = React.useRef(false)

  const finishRound = React.useCallback((finalScore: number) => {
    if (reportedRound.current) {
      return
    }

    reportedRound.current = true
    setStatus('finished')
    setMessage('Resultado enviado al catalogo')
    window.parent.postMessage(
      {
        type: GAME_ROUND_COMPLETED_MESSAGE,
        gameId,
        score: Math.max(0, finalScore),
        durationSeconds: Math.max(1, round),
      },
      window.location.origin,
    )
  }, [gameId, round])

  const showSequence = React.useCallback(async (nextSequence: number[]) => {
    const token = runToken.current + 1
    runToken.current = token
    setStatus('showing')
    setInputIndex(0)
    setMessage('Observa la secuencia')

    await wait(300)

    for (const padIndex of nextSequence) {
      if (runToken.current !== token) {
        return
      }

      setActivePad(padIndex)
      await wait(SHOW_STEP_MS)
      setActivePad(null)
      await wait(SHOW_GAP_MS)
    }

    if (runToken.current !== token) {
      return
    }

    setStatus('input')
    setMessage('Repite el patron')
  }, [])

  const startRound = () => {
    const initialSequence = Array.from({ length: INITIAL_SEQUENCE_LENGTH }, () => randomPad())
    reportedRound.current = false
    setSequence(initialSequence)
    setRound(1)
    setMistakes(0)
    setScore(0)
    setActivePad(null)
    void showSequence(initialSequence)
  }

  const handlePad = (padIndex: number) => {
    if (status !== 'input') {
      return
    }

    setActivePad(padIndex)
    window.setTimeout(() => setActivePad(null), 150)

    if (sequence[inputIndex] !== padIndex) {
      const nextMistakes = mistakes + 1
      setMistakes(nextMistakes)
      setMessage(nextMistakes >= MAX_MISTAKES ? 'Limite de fallos alcanzado' : 'Fallo detectado')

      if (nextMistakes >= MAX_MISTAKES) {
        const finalScore = score + Math.max(0, MAX_MISTAKES - nextMistakes) * 6
        finishRound(finalScore)
      }

      return
    }

    const nextScore = score + 2
    const nextInputIndex = inputIndex + 1
    setScore(nextScore)

    if (nextInputIndex < sequence.length) {
      setInputIndex(nextInputIndex)
      setMessage('Pulso correcto')
      return
    }

    const roundScore = nextScore + 10
    const nextRound = round + 1
    const nextSequence = [...sequence, randomPad()]
    setScore(roundScore)
    setRound(nextRound)
    setSequence(nextSequence)
    setMessage(`Ronda ${round} completada`)
    void showSequence(nextSequence)
  }

  return (
    <main className="relative h-screen min-h-112 overflow-hidden bg-transparent p-4 font-['Space_Grotesk'] text-white">
      <div className="memory-grid pointer-events-none absolute inset-0 opacity-30" />
      <section className="relative z-10 grid h-full min-h-0 grid-rows-[auto_minmax(0,1fr)_auto] gap-3">
        <header className="grid grid-cols-[minmax(0,1fr)_18rem] gap-3">
          <div>
            <p className="inline-flex rounded-full border border-[color-mix(in_srgb,var(--astrais-rarity-epic)_35%,transparent)] bg-[color-mix(in_srgb,var(--astrais-rarity-epic)_12%,transparent)] px-3 py-1 text-[0.62rem] font-semibold uppercase tracking-[0.24em] text-[var(--astrais-rarity-epic)]">
              Secuencia astral
            </p>
            <h1 className="mt-3 font-['Press_Start_2P'] text-[clamp(1rem,2vw,1.35rem)] leading-tight">Astra Memory</h1>
            <p className="mt-3 max-w-xl text-[0.82rem] leading-5 text-slate-200">
              Mira el orden de los pads, repitelo y alarga la cadena. Tres fallos cierran la partida.
            </p>
          </div>

          <div className="grid grid-cols-3 gap-2 self-center text-center">
            <div className="rounded-2xl border border-white/10 bg-black/24 p-3">
              <p className="text-[0.55rem] uppercase tracking-[0.18em] text-slate-400">Ronda</p>
              <p className="mt-2 font-['Press_Start_2P'] text-[0.82rem] text-accent-beige-300">{round}</p>
            </div>
            <div className="rounded-2xl border border-white/10 bg-black/24 p-3">
              <p className="text-[0.55rem] uppercase tracking-[0.18em] text-slate-400">Puntos</p>
              <p className="mt-2 text-[1rem] font-semibold text-white">{score}</p>
            </div>
            <div className="rounded-2xl border border-white/10 bg-black/24 p-3">
              <p className="text-[0.55rem] uppercase tracking-[0.18em] text-slate-400">Fallos</p>
              <p className="mt-2 text-[1rem] font-semibold text-[var(--astrais-rarity-epic)]">{mistakes}/{MAX_MISTAKES}</p>
            </div>
          </div>
        </header>

        <div className="relative grid min-h-0 place-items-center overflow-hidden rounded-[26px] border border-white/15 bg-[linear-gradient(160deg,color-mix(in_srgb,var(--astrais-background)_78%,transparent),color-mix(in_srgb,var(--astrais-primary)_38%,var(--astrais-surface)_62%))] p-6">
          <div className="grid aspect-square h-full max-h-[21rem] grid-cols-2 gap-4">
            {pads.map((pad, padIndex) => (
              <button
                key={pad.label}
                type="button"
                onClick={() => handlePad(padIndex)}
                disabled={status !== 'input'}
                className={`rounded-[28px] border p-5 text-center transition duration-150 disabled:cursor-not-allowed ${
                  pad.className
                } ${activePad === padIndex ? pad.activeClassName : 'shadow-[0_16px_34px_color-mix(in_srgb,var(--astrais-background)_32%,transparent)]'}`}
              >
                <span className="block font-['Press_Start_2P'] text-2xl">{pad.shortLabel}</span>
                <span className="mt-3 block text-[0.66rem] uppercase tracking-[0.22em]">{pad.label}</span>
              </button>
            ))}
          </div>

          {status === 'idle' || status === 'finished' ? (
            <div className="absolute inset-0 grid place-items-center bg-black/34 backdrop-blur-[1px]">
              <div className="max-w-md rounded-[26px] border border-white/15 bg-slate-950/84 p-6 text-center shadow-[0_20px_50px_color-mix(in_srgb,var(--astrais-background)_42%,transparent)]">
                <p className="text-[0.64rem] uppercase tracking-[0.24em] text-accent-beige-300">
                  {status === 'finished' ? 'Memoria registrada' : 'Nucleo listo'}
                </p>
                <h2 className="mt-4 font-['Press_Start_2P'] text-lg">{status === 'finished' ? `${score} puntos` : 'Repite el patron'}</h2>
                <button
                  type="button"
                  onClick={startRound}
                  className="mt-5 rounded-2xl [background:var(--astrais-cta-bg)] px-5 py-3 text-[0.78rem] font-semibold text-white transition hover:-translate-y-0.5"
                >
                  {status === 'finished' ? 'Nueva secuencia' : 'Empezar'}
                </button>
              </div>
            </div>
          ) : null}
        </div>

        <footer className="grid grid-cols-[minmax(0,1fr)_16rem] items-center gap-3">
          <div className="rounded-2xl border border-white/10 bg-black/24 px-4 py-3 text-[0.76rem] text-slate-200">
            {message}
          </div>
          <div className="rounded-2xl border border-white/10 bg-white/8 px-4 py-3 text-[0.7rem] uppercase tracking-[0.18em] text-slate-300">
            {status === 'showing' && 'Observando'}
            {status === 'input' && `${inputIndex + 1}/${sequence.length}`}
            {status === 'idle' && 'En espera'}
            {status === 'finished' && 'Finalizado'}
          </div>
        </footer>
      </section>

      <style>{`
        .memory-grid {
          background-image:
            linear-gradient(color-mix(in srgb, var(--astrais-text) 6%, transparent) 1px, transparent 1px),
            linear-gradient(90deg, color-mix(in srgb, var(--astrais-text) 6%, transparent) 1px, transparent 1px);
          background-size: 36px 36px;
          mask-image: radial-gradient(circle, black, transparent 72%);
        }
      `}</style>
    </main>
  )
}
