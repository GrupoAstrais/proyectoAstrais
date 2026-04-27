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
    className: 'border-[#38bdf8]/50 bg-[#38bdf8]/14 text-[#bae6fd]',
    activeClassName: 'border-[#7dd3fc] bg-[#38bdf8]/45 shadow-[0_0_34px_rgba(56,189,248,0.34)]',
  },
  {
    label: 'Este',
    shortLabel: 'E',
    className: 'border-accent-mint-300/50 bg-accent-mint-300/14 text-accent-mint-300',
    activeClassName: 'border-accent-mint-300 bg-accent-mint-300/42 shadow-[0_0_34px_rgba(159,232,197,0.34)]',
  },
  {
    label: 'Sur',
    shortLabel: 'S',
    className: 'border-[#f59e0b]/50 bg-[#f59e0b]/14 text-[#f8d089]',
    activeClassName: 'border-[#fbbf24] bg-[#f59e0b]/42 shadow-[0_0_34px_rgba(245,158,11,0.34)]',
  },
  {
    label: 'Oeste',
    shortLabel: 'O',
    className: 'border-[#ec4899]/50 bg-[#ec4899]/14 text-[#f5b6dc]',
    activeClassName: 'border-[#f472b6] bg-[#ec4899]/42 shadow-[0_0_34px_rgba(236,72,153,0.34)]',
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
    <main className="relative h-screen min-h-112 overflow-hidden bg-[radial-gradient(circle_at_top_right,rgba(236,72,153,0.2),transparent_34%),linear-gradient(145deg,#0f172a,#312e81_52%,#111827)] p-4 font-['Space_Grotesk'] text-white">
      <div className="memory-grid pointer-events-none absolute inset-0 opacity-30" />
      <section className="relative z-10 grid h-full min-h-0 grid-rows-[auto_minmax(0,1fr)_auto] gap-3">
        <header className="grid grid-cols-[minmax(0,1fr)_18rem] gap-3">
          <div>
            <p className="inline-flex rounded-full border border-[#ec4899]/35 bg-[#ec4899]/12 px-3 py-1 text-[0.62rem] font-semibold uppercase tracking-[0.24em] text-[#f5b6dc]">
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
              <p className="mt-2 text-[1rem] font-semibold text-[#f5b6dc]">{mistakes}/{MAX_MISTAKES}</p>
            </div>
          </div>
        </header>

        <div className="relative grid min-h-0 place-items-center overflow-hidden rounded-[26px] border border-white/15 bg-[linear-gradient(160deg,rgba(15,23,42,0.78),rgba(49,46,129,0.58))] p-6">
          <div className="grid aspect-square h-full max-h-[21rem] grid-cols-2 gap-4">
            {pads.map((pad, padIndex) => (
              <button
                key={pad.label}
                type="button"
                onClick={() => handlePad(padIndex)}
                disabled={status !== 'input'}
                className={`rounded-[28px] border p-5 text-center transition duration-150 disabled:cursor-not-allowed ${
                  pad.className
                } ${activePad === padIndex ? pad.activeClassName : 'shadow-[0_16px_34px_rgba(0,0,0,0.22)]'}`}
              >
                <span className="block font-['Press_Start_2P'] text-2xl">{pad.shortLabel}</span>
                <span className="mt-3 block text-[0.66rem] uppercase tracking-[0.22em]">{pad.label}</span>
              </button>
            ))}
          </div>

          {status === 'idle' || status === 'finished' ? (
            <div className="absolute inset-0 grid place-items-center bg-black/34 backdrop-blur-[1px]">
              <div className="max-w-md rounded-[26px] border border-white/15 bg-slate-950/84 p-6 text-center shadow-[0_20px_50px_rgba(0,0,0,0.35)]">
                <p className="text-[0.64rem] uppercase tracking-[0.24em] text-accent-beige-300">
                  {status === 'finished' ? 'Memoria registrada' : 'Nucleo listo'}
                </p>
                <h2 className="mt-4 font-['Press_Start_2P'] text-lg">{status === 'finished' ? `${score} puntos` : 'Repite el patron'}</h2>
                <button
                  type="button"
                  onClick={startRound}
                  className="mt-5 rounded-2xl bg-linear-to-r from-[#38bdf8] via-[#ec4899] to-[#f59e0b] px-5 py-3 text-[0.78rem] font-semibold text-white transition hover:-translate-y-0.5"
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
            linear-gradient(rgba(255, 255, 255, 0.06) 1px, transparent 1px),
            linear-gradient(90deg, rgba(255, 255, 255, 0.06) 1px, transparent 1px);
          background-size: 36px 36px;
          mask-image: radial-gradient(circle, black, transparent 72%);
        }
      `}</style>
    </main>
  )
}
