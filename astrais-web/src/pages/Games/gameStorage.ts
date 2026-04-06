export type GameStatus = 'idle' | 'playing' | 'finished'

export interface ClickerStats {
  bestScore: number
  lastScore: number
  gamesPlayed: number
  totalClicks: number
  totalLudionsEarned: number
}

export const GAME_ROUND_SECONDS = 10
export const CLICKER_STATS_STORAGE_KEY = 'astrais.games.clicker.stats'

const DEFAULT_CLICKER_STATS: ClickerStats = {
  bestScore: 0,
  lastScore: 0,
  gamesPlayed: 0,
  totalClicks: 0,
  totalLudionsEarned: 0,
}

function sanitizeNumber(value: unknown, fallback: number) {
  return typeof value === 'number' && Number.isFinite(value) ? value : fallback
}

export function readClickerStats(): ClickerStats {
  if (typeof window === 'undefined') {
    return DEFAULT_CLICKER_STATS
  }

  try {
    const rawStats = window.localStorage.getItem(CLICKER_STATS_STORAGE_KEY)

    if (!rawStats) {
      return DEFAULT_CLICKER_STATS
    }

    const parsedStats = JSON.parse(rawStats) as Partial<ClickerStats>

    return {
      bestScore: sanitizeNumber(parsedStats.bestScore, 0),
      lastScore: sanitizeNumber(parsedStats.lastScore, 0),
      gamesPlayed: sanitizeNumber(parsedStats.gamesPlayed, 0),
      totalClicks: sanitizeNumber(parsedStats.totalClicks, 0),
      totalLudionsEarned: sanitizeNumber(parsedStats.totalLudionsEarned, 0),
    }
  } catch {
    return DEFAULT_CLICKER_STATS
  }
}

export function writeClickerStats(stats: ClickerStats) {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.setItem(CLICKER_STATS_STORAGE_KEY, JSON.stringify(stats))
}

export function getLudionReward(score: number) {
  if (score <= 0) {
    return 0
  }

  return 12 + score * 4
}

export function getArenaRank(score: number) {
  if (score >= 30) {
    return 'Leyenda del vacio'
  }

  if (score >= 20) {
    return 'Piloto de elite'
  }

  if (score >= 12) {
    return 'Cadete orbital'
  }

  if (score >= 1) {
    return 'Explorador en practicas'
  }

  return 'Cabina en reposo'
}
