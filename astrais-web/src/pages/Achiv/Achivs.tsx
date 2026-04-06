import React from 'react'
import { NavLink } from 'react-router'
import Navbar from '../../components/layout/Navbar'
import astra from '../../assets/astra2.png'
import bgImage from '../../assets/homeScreenBack.jpg'
import iconLogro from '../../assets/iconLogro.png'
import {
  buildAchievements,
  matchesAchievementFilter,
  type AchievementCategory,
  type AchievementFilter,
  type AchievementRarity,
} from './achievementCatalog'
import { readClickerStats } from '../Games/gameStorage'

const CLAIMED_ACHIEVEMENTS_STORAGE_KEY = 'astrais.achievements.claimed'
const SELECTED_ACHIEVEMENT_STORAGE_KEY = 'astrais.achievements.selected'

const filters: Array<{ key: AchievementFilter; label: string }> = [
  { key: 'all', label: 'Todos' },
  { key: 'unlocked', label: 'Desbloqueados' },
  { key: 'progress', label: 'En progreso' },
  { key: 'locked', label: 'Bloqueados' },
]

function readClaimedAchievements() {
  if (typeof window === 'undefined') {
    return [] as string[]
  }

  try {
    const rawClaimed = window.localStorage.getItem(CLAIMED_ACHIEVEMENTS_STORAGE_KEY)

    if (!rawClaimed) {
      return []
    }

    const parsedClaimed = JSON.parse(rawClaimed)
    return Array.isArray(parsedClaimed) ? parsedClaimed.filter((item): item is string => typeof item === 'string') : []
  } catch {
    return []
  }
}

function readSelectedAchievement() {
  if (typeof window === 'undefined') {
    return ''
  }

  return window.localStorage.getItem(SELECTED_ACHIEVEMENT_STORAGE_KEY) ?? ''
}

function glyphForCategory(category: AchievementCategory) {
  if (category === 'Minijuegos') {
    return (
      <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" stroke="currentColor" strokeWidth="1.8">
        <path d="M8 8h8m-8 4h4m6-6 2 2v8a2 2 0 0 1-2 2h-3l-3-3H8l-3 3H4a2 2 0 0 1-2-2V8l2-2h14Z" />
      </svg>
    )
  }

  if (category === 'Constancia') {
    return (
      <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" stroke="currentColor" strokeWidth="1.8">
        <path d="M12 3v3m6.36 1.64-2.12 2.12M21 12h-3m.48 6.36-2.12-2.12M12 21v-3m-6.36-.64 2.12-2.12M3 12h3m-.48-6.36 2.12 2.12" />
        <circle cx="12" cy="12" r="4.2" />
      </svg>
    )
  }

  if (category === 'Coleccion') {
    return (
      <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" stroke="currentColor" strokeWidth="1.8">
        <path d="M5 5h6v6H5zM13 5h6v6h-6zM5 13h6v6H5zM13 13h6v6h-6z" />
      </svg>
    )
  }

  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" stroke="currentColor" strokeWidth="1.8">
      <path d="m12 3 2.6 5.26 5.8.84-4.2 4.1.98 5.8L12 16.2 6.82 19l.98-5.8-4.2-4.1 5.8-.84L12 3Z" />
    </svg>
  )
}

function rarityClasses(rarity: AchievementRarity) {
  if (rarity === 'Legendario') {
    return 'border-[#f59e0b]/40 bg-[#f59e0b]/10 text-[#f8d089]'
  }

  if (rarity === 'Epico') {
    return 'border-[#ec4899]/35 bg-[#ec4899]/10 text-[#f5b6dc]'
  }

  if (rarity === 'Raro') {
    return 'border-accent-mint-300/35 bg-accent-mint-300/10 text-accent-mint-300'
  }

  return 'border-white/15 bg-white/8 text-slate-200'
}

export default function Achivs() {
  const [activeFilter, setActiveFilter] = React.useState<AchievementFilter>('all')
  const [claimedIds, setClaimedIds] = React.useState<string[]>(() => readClaimedAchievements())
  const [selectedId, setSelectedId] = React.useState(() => readSelectedAchievement())
  const [gameStats] = React.useState(() => readClickerStats())

  const achievements = buildAchievements(gameStats, claimedIds)
  const filteredAchievements = achievements.filter((achievement) => matchesAchievementFilter(achievement, activeFilter))
  const selectedAchievement =
    achievements.find((achievement) => achievement.id === selectedId) ??
    filteredAchievements[0] ??
    achievements[0]

  const unlockedCount = achievements.filter((achievement) => achievement.unlocked).length
  const claimedCount = achievements.filter((achievement) => achievement.claimed).length
  const pendingRewards = achievements
    .filter((achievement) => achievement.unlocked && !achievement.claimed)
    .reduce((rewardCount, achievement) => rewardCount + achievement.reward, 0)
  const totalCompletion = Math.round(
    achievements.reduce((progressSum, achievement) => progressSum + achievement.percent, 0) / achievements.length,
  )

  React.useEffect(() => {
    if (!filteredAchievements.length) {
      return
    }

    const selectedIsVisible = filteredAchievements.some((achievement) => achievement.id === selectedId)

    if (!selectedIsVisible) {
      setSelectedId(filteredAchievements[0].id)
    }
  }, [filteredAchievements, selectedId])

  React.useEffect(() => {
    if (typeof window === 'undefined') {
      return
    }

    window.localStorage.setItem(CLAIMED_ACHIEVEMENTS_STORAGE_KEY, JSON.stringify(claimedIds))
  }, [claimedIds])

  React.useEffect(() => {
    if (typeof window === 'undefined' || !selectedId) {
      return
    }

    window.localStorage.setItem(SELECTED_ACHIEVEMENT_STORAGE_KEY, selectedId)
  }, [selectedId])

  const handleClaimReward = () => {
    if (!selectedAchievement || !selectedAchievement.unlocked || selectedAchievement.claimed) {
      return
    }

    setClaimedIds((currentClaimedIds) => [...currentClaimedIds, selectedAchievement.id])
  }

  return (
    <div
      style={{ backgroundImage: `url(${bgImage})` }}
      className="relative h-screen overflow-hidden bg-cover bg-center font-['Space_Grotesk'] text-white"
    >
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(236,72,153,0.20),transparent_34%),radial-gradient(circle_at_bottom_left,rgba(34,197,94,0.16),transparent_34%),radial-gradient(circle_at_center,rgba(99,102,241,0.22),transparent_46%)]" />
      <div className="pointer-events-none absolute inset-0 bg-black/62" />
      <div className="scanlines pointer-events-none absolute inset-0 opacity-25" />

      <div className="relative z-10 flex h-full min-h-0 flex-col">
        <Navbar />

        <main className="flex min-h-0 flex-1 px-3 pb-3 pt-1 md:px-4 md:pb-4 xl:px-6 xl:pb-5">
          <section className="mx-auto hidden h-full w-full max-w-[1460px] gap-3 lg:grid lg:grid-cols-[minmax(0,1.08fr)_minmax(19rem,0.88fr)] min-[1400px]:gap-4 min-[1400px]:grid-cols-[minmax(0,1.24fr)_minmax(24rem,0.92fr)]">
            <div className="grid min-h-0 grid-rows-[auto_auto_minmax(0,1fr)] gap-3 min-[1400px]:gap-4">
              <article className="panel-glow relative overflow-hidden rounded-[26px] border border-white/15 bg-[linear-gradient(160deg,rgba(15,23,42,0.88),rgba(91,33,182,0.54),rgba(30,74,99,0.74))] p-3.5 shadow-[0_20px_58px_rgba(7,12,24,0.5)] min-[1400px]:p-4">
                <div className="pointer-events-none absolute -right-10 top-0 h-40 w-40 rounded-full bg-secondary-500/18 blur-3xl" />
                <div className="grid grid-cols-[minmax(0,1fr)_15rem] items-start gap-3 min-[1400px]:grid-cols-[minmax(0,1fr)_18rem] min-[1400px]:gap-4">
                  <div>
                    <p className="text-[0.64rem] uppercase tracking-[0.28em] text-accent-beige-300">Vitrina Astrais</p>
                    <h1 className="mt-3 font-['Press_Start_2P'] text-[clamp(0.95rem,1.5vw,1.28rem)] leading-tight text-white">
                      Logros y recompensas
                    </h1>
                    <p className="mt-3 max-w-xl text-[0.82rem] leading-5 text-slate-200 xl:text-[0.9rem] xl:leading-6">
                      Una vista compacta para escritorio con filtros, detalle activo y recompensas listas para reclamar
                      sin necesidad de scroll.
                    </p>
                  </div>

                  <div className="grid grid-cols-3 gap-2.5 min-[1400px]:gap-3">
                    <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                      <p className="text-[0.58rem] uppercase tracking-[0.22em] text-slate-400">Desbloq.</p>
                      <p className="mt-2 text-[1rem] font-semibold text-white xl:text-[1.14rem]">{unlockedCount}</p>
                    </div>
                    <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                      <p className="text-[0.58rem] uppercase tracking-[0.22em] text-slate-400">Reclam.</p>
                      <p className="mt-2 text-[1rem] font-semibold text-accent-mint-300 xl:text-[1.14rem]">{claimedCount}</p>
                    </div>
                    <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                      <p className="text-[0.58rem] uppercase tracking-[0.22em] text-slate-400">Pendiente</p>
                      <p className="mt-2 text-[1rem] font-semibold text-[#f8d089] xl:text-[1.14rem]">{pendingRewards}</p>
                    </div>
                  </div>
                </div>
              </article>

              <article className="panel-glow rounded-[22px] border border-white/15 bg-[rgba(15,23,42,0.82)] p-3 shadow-[0_18px_40px_rgba(7,12,24,0.35)]">
                <div className="flex items-center justify-between gap-3">
                  <div className="flex flex-wrap gap-2">
                    {filters.map((filter) => (
                      <button
                        key={filter.key}
                        type="button"
                        onClick={() => setActiveFilter(filter.key)}
                        className={`rounded-full border px-4 py-2 text-[0.76rem] font-semibold transition xl:text-[0.82rem] ${
                          activeFilter === filter.key
                            ? 'border-transparent bg-linear-to-r from-[#f97316] via-[#ec4899] to-[#8b5cf6] text-white shadow-[0_10px_24px_rgba(236,72,153,0.22)]'
                            : 'border-white/15 bg-white/6 text-slate-200 hover:bg-white/10'
                        }`}
                      >
                        {filter.label}
                      </button>
                    ))}
                  </div>

                  <div className="rounded-full border border-accent-mint-300/25 bg-accent-mint-300/8 px-4 py-2 text-[0.76rem] text-accent-mint-300 xl:text-[0.82rem]">
                    Progreso global {totalCompletion}%
                  </div>
                </div>
              </article>

              <section className="grid min-h-0 grid-cols-2 gap-3 min-[1400px]:grid-cols-3 min-[1400px]:gap-4">
                {filteredAchievements.map((achievement) => (
                  <button
                    key={achievement.id}
                    type="button"
                    onClick={() => setSelectedId(achievement.id)}
                    className={`achievement-card flex h-full min-h-0 flex-col rounded-[22px] border p-3 text-left transition min-[1400px]:p-4 ${
                      selectedAchievement?.id === achievement.id
                        ? 'border-accent-beige-300/40 bg-[linear-gradient(160deg,rgba(255,255,255,0.12),rgba(129,140,248,0.12))] shadow-[0_16px_38px_rgba(15,23,42,0.36)]'
                        : achievement.unlocked
                          ? 'border-accent-mint-300/22 bg-[rgba(15,23,42,0.8)] hover:border-accent-mint-300/36 hover:bg-white/8'
                          : 'border-white/10 bg-[rgba(15,23,42,0.74)] hover:border-white/18 hover:bg-white/8'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <div className="flex items-center gap-2.5">
                        <div className={`flex h-10 w-10 items-center justify-center rounded-2xl border ${rarityClasses(achievement.rarity)}`}>
                          {glyphForCategory(achievement.category)}
                        </div>
                        <div className="min-w-0">
                          <p className="text-[0.58rem] uppercase tracking-[0.18em] text-slate-400">{achievement.category}</p>
                          <h2 className="mt-1 truncate text-[0.8rem] font-semibold text-white min-[1400px]:text-[0.9rem]">{achievement.title}</h2>
                        </div>
                      </div>

                      <span className={`rounded-full border px-2 py-1 text-[0.52rem] uppercase tracking-[0.18em] ${rarityClasses(achievement.rarity)}`}>
                        {achievement.rarity}
                      </span>
                    </div>

                    <p className="mt-3 text-[0.72rem] leading-5 text-slate-300 line-clamp-achievement min-[1400px]:text-[0.8rem]">
                      {achievement.description}
                    </p>

                    <div className="mt-3">
                      <div className="mb-2 flex items-center justify-between text-[0.56rem] uppercase tracking-[0.18em] text-slate-400">
                        <span>{achievement.progress}/{achievement.goal}</span>
                        <span>
                          {achievement.claimed
                            ? 'Reclamado'
                            : achievement.unlocked
                              ? 'Listo'
                              : achievement.progress > 0
                                ? 'Avance'
                                : 'Bloqueado'}
                        </span>
                      </div>
                      <div className="h-2 overflow-hidden rounded-full bg-white/10">
                        <div
                          className={`h-full rounded-full transition-all duration-300 ${
                            achievement.claimed
                              ? 'bg-linear-to-r from-accent-mint-300 to-[#10b981]'
                              : achievement.unlocked
                                ? 'bg-linear-to-r from-[#f59e0b] to-[#ec4899]'
                                : 'bg-linear-to-r from-secondary-500 to-primary-500'
                          }`}
                          style={{ width: `${achievement.percent}%` }}
                        />
                      </div>
                    </div>

                    <div className="mt-auto flex items-center justify-between pt-3">
                      <span className="text-[0.72rem] font-semibold text-[#f8d089] min-[1400px]:text-[0.82rem]">+{achievement.reward} ludiones</span>
                      <span className="text-[0.58rem] uppercase tracking-[0.16em] text-slate-400">{achievement.percent}%</span>
                    </div>
                  </button>
                ))}
              </section>
            </div>

            <aside className="grid min-h-0 grid-rows-[minmax(0,1fr)_auto] gap-3 min-[1400px]:gap-4">
              {selectedAchievement ? (
                <>
                  <article className="panel-glow relative min-h-0 overflow-hidden rounded-[26px] border border-white/15 bg-[linear-gradient(180deg,rgba(15,23,42,0.9),rgba(30,74,99,0.76))] p-3.5 shadow-[0_22px_56px_rgba(7,12,24,0.46)] min-[1400px]:p-5">
                    <div className="pointer-events-none absolute -right-8 top-6 h-36 w-36 rounded-full bg-secondary-500/18 blur-3xl" />
                    <img
                      src={astra}
                      alt="Mascota Astrais"
                      className="pointer-events-none absolute bottom-0 right-0 hidden h-[clamp(6rem,13vh,8rem)] opacity-75 drop-shadow-[0_14px_28px_rgba(15,23,42,0.55)] min-[1400px]:block"
                    />

                    <div className="relative z-10 grid h-full min-h-0 grid-rows-[auto_auto_auto_auto_auto]">
                      <div className="flex items-start justify-between gap-3">
                        <div className="flex items-center gap-3">
                          <div className={`flex h-12 w-12 items-center justify-center rounded-[18px] border ${rarityClasses(selectedAchievement.rarity)} min-[1400px]:h-14 min-[1400px]:w-14`}>
                            <img src={iconLogro} alt="Icono de logro" className="h-7 w-7 object-contain" />
                          </div>
                          <div>
                            <p className="text-[0.58rem] uppercase tracking-[0.2em] text-slate-400">{selectedAchievement.category}</p>
                            <h2 className="mt-2 font-['Press_Start_2P'] text-[0.84rem] leading-snug text-white min-[1400px]:text-[1rem]">
                              {selectedAchievement.title}
                            </h2>
                          </div>
                        </div>

                        <span className={`rounded-full border px-3 py-1 text-[0.56rem] uppercase tracking-[0.18em] ${rarityClasses(selectedAchievement.rarity)}`}>
                          {selectedAchievement.rarity}
                        </span>
                      </div>

                      <p className="mt-4 text-[0.76rem] leading-5 text-slate-200 min-[1400px]:text-[0.88rem] min-[1400px]:leading-6">
                        {selectedAchievement.description}
                      </p>

                      <div className="mt-4 grid grid-cols-3 gap-2.5 min-[1400px]:gap-3">
                        <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                          <p className="text-[0.56rem] uppercase tracking-[0.18em] text-slate-400">Progreso</p>
                          <p className="mt-2 text-[0.84rem] font-semibold text-white min-[1400px]:text-[1rem]">
                            {selectedAchievement.progress}/{selectedAchievement.goal}
                          </p>
                        </div>
                        <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                          <p className="text-[0.56rem] uppercase tracking-[0.18em] text-slate-400">Premio</p>
                          <p className="mt-2 text-[0.84rem] font-semibold text-[#f8d089] min-[1400px]:text-[1rem]">{selectedAchievement.reward}</p>
                        </div>
                        <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                          <p className="text-[0.56rem] uppercase tracking-[0.18em] text-slate-400">Estado</p>
                          <p className="mt-2 text-[0.66rem] font-semibold text-accent-mint-300 min-[1400px]:text-[0.78rem]">
                            {selectedAchievement.claimed
                              ? 'Reclamado'
                              : selectedAchievement.unlocked
                                ? 'Listo para reclamar'
                                : 'Sigue avanzando'}
                          </p>
                        </div>
                      </div>

                      <div className="mt-4">
                        <div className="mb-2 flex items-center justify-between text-[0.68rem] text-slate-300 min-[1400px]:text-[0.78rem]">
                          <span>Porcentaje completado</span>
                          <span>{selectedAchievement.percent}%</span>
                        </div>
                        <div className="h-3 overflow-hidden rounded-full bg-white/10">
                          <div
                            className={`h-full rounded-full transition-all duration-300 ${
                              selectedAchievement.claimed
                                ? 'bg-linear-to-r from-accent-mint-300 to-[#10b981]'
                                : selectedAchievement.unlocked
                                  ? 'bg-linear-to-r from-[#f59e0b] to-[#ec4899]'
                                  : 'bg-linear-to-r from-secondary-500 to-primary-500'
                            }`}
                            style={{ width: `${selectedAchievement.percent}%` }}
                          />
                        </div>
                      </div>

                      <div className="mt-4 rounded-[22px] border border-white/10 bg-black/20 px-4 py-3">
                        <p className="text-[0.56rem] uppercase tracking-[0.18em] text-slate-400">Pista recomendada</p>
                        <p className="mt-2 text-[0.72rem] leading-5 text-slate-200 min-[1400px]:text-[0.82rem] min-[1400px]:leading-6">{selectedAchievement.hint}</p>
                      </div>

                      <div className="mt-4 flex items-center justify-between gap-3">
                        <button
                          type="button"
                          onClick={handleClaimReward}
                          disabled={!selectedAchievement.unlocked || selectedAchievement.claimed}
                          className={`rounded-2xl px-4 py-3 text-[0.72rem] font-semibold transition min-[1400px]:text-[0.82rem] ${
                            selectedAchievement.claimed
                              ? 'cursor-default border border-accent-mint-300/25 bg-accent-mint-300/12 text-accent-mint-300'
                              : selectedAchievement.unlocked
                                ? 'border border-transparent bg-linear-to-r from-[#f59e0b] via-[#ec4899] to-[#8b5cf6] text-white shadow-[0_14px_28px_rgba(236,72,153,0.24)] hover:-translate-y-0.5'
                                : 'cursor-not-allowed border border-white/10 bg-white/8 text-slate-400'
                          }`}
                        >
                          {selectedAchievement.claimed ? 'Recompensa guardada' : 'Reclamar recompensa'}
                        </button>

                        <NavLink
                          to="/games"
                          className="rounded-2xl border border-white/15 bg-white/8 px-4 py-3 text-[0.72rem] font-semibold text-white transition hover:bg-white/12 min-[1400px]:text-[0.82rem]"
                        >
                          Ir a minijuegos
                        </NavLink>
                      </div>
                    </div>
                  </article>

                  <div className="grid grid-cols-2 gap-3 min-[1400px]:gap-4">
                    <article className="panel-glow rounded-[22px] border border-white/15 bg-[rgba(15,23,42,0.82)] p-3.5 shadow-[0_16px_36px_rgba(7,12,24,0.34)] min-[1400px]:p-4">
                      <p className="text-[0.58rem] uppercase tracking-[0.2em] text-accent-beige-300">Sincronizado</p>
                      <h3 className="mt-2 font-['Press_Start_2P'] text-[0.74rem] leading-snug text-white min-[1400px]:text-[0.88rem]">Datos del arcade</h3>
                      <div className="mt-3 space-y-2.5 text-[0.72rem] text-slate-300 min-[1400px]:text-[0.82rem]">
                        <div className="flex items-center justify-between">
                          <span>Partidas jugadas</span>
                          <span className="font-semibold text-white">{gameStats.gamesPlayed}</span>
                        </div>
                        <div className="flex items-center justify-between">
                          <span>Mejor puntuacion</span>
                          <span className="font-semibold text-white">{gameStats.bestScore}</span>
                        </div>
                        <div className="flex items-center justify-between">
                          <span>Ludiones obtenidos</span>
                          <span className="font-semibold text-white">{gameStats.totalLudionsEarned}</span>
                        </div>
                      </div>
                    </article>

                    <article className="panel-glow rounded-[22px] border border-white/15 bg-[rgba(15,23,42,0.82)] p-3.5 shadow-[0_16px_36px_rgba(7,12,24,0.34)] min-[1400px]:p-4">
                      <p className="text-[0.58rem] uppercase tracking-[0.2em] text-accent-beige-300">Objetivo activo</p>
                      <h3 className="mt-2 font-['Press_Start_2P'] text-[0.74rem] leading-snug text-white min-[1400px]:text-[0.88rem]">Siguiente paso</h3>
                      <p className="mt-3 text-[0.72rem] leading-5 text-slate-300 min-[1400px]:text-[0.82rem] min-[1400px]:leading-6">
                        {selectedAchievement.unlocked && !selectedAchievement.claimed
                          ? 'La recompensa ya esta lista. Reclama y sigue ampliando tu vitrina.'
                          : selectedAchievement.claimed
                            ? 'Recompensa archivada. Cambia de filtro o vuelve al arcade para otra pieza.'
                            : selectedAchievement.hint}
                      </p>
                    </article>
                  </div>
                </>
              ) : null}
            </aside>
          </section>

          <section className="mx-auto flex h-full max-w-md items-center justify-center lg:hidden">
            <article className="rounded-[28px] border border-white/15 bg-[rgba(15,23,42,0.84)] p-6 text-center shadow-[0_24px_60px_rgba(7,12,24,0.45)] backdrop-blur-sm">
              <p className="text-[0.72rem] uppercase tracking-[0.28em] text-accent-beige-300">Vitrina Astrais</p>
              <h1 className="mt-4 font-['Press_Start_2P'] text-lg text-white">Vista de escritorio</h1>
              <p className="mt-4 text-sm leading-6 text-slate-300">
                Esta pantalla esta optimizada para escritorios medianos y grandes. Logros desbloqueados: {unlockedCount}.
              </p>
            </article>
          </section>
        </main>
      </div>

      <style>{`
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
          box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.1);
          pointer-events: none;
        }

        .achievement-card {
          box-shadow: 0 14px 30px rgba(7, 12, 24, 0.22);
        }

        .line-clamp-achievement {
          display: -webkit-box;
          -webkit-box-orient: vertical;
          -webkit-line-clamp: 2;
          overflow: hidden;
        }
      `}</style>
    </div>
  )
}
