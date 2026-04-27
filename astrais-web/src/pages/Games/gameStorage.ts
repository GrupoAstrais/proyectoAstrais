export type GameStatus = 'idle' | 'playing' | 'finished'

export interface ArcadeGameStats {
  bestScore: number
  lastScore: number
  gamesPlayed: number
  totalScore: number
  totalLudionsEarned: number
}

export interface ArcadeStats extends ArcadeGameStats {
  games: Record<string, ArcadeGameStats>
}

export interface ClickerStats extends ArcadeGameStats {
  totalClicks: number
}

export interface CompletedGameRound {
  gameId: string
  score: number
  durationSeconds: number
}

export const GAME_ROUND_SECONDS = 10
export const PRIMARY_CLICKER_GAME_ID = 'atrapa-ludiones'
export const NEBULA_DASH_GAME_ID = 'nebula-dash'
export const ASTRA_MEMORY_GAME_ID = 'astra-memory'
export const ARCADE_STATS_STORAGE_KEY = 'astrais.games.arcade.stats'
export const CLICKER_STATS_STORAGE_KEY = 'astrais.games.clicker.stats'

const DEFAULT_GAME_STATS: ArcadeGameStats = {
  bestScore: 0,
  lastScore: 0,
  gamesPlayed: 0,
  totalScore: 0,
  totalLudionsEarned: 0,
}

const DEFAULT_ARCADE_STATS: ArcadeStats = {
  ...DEFAULT_GAME_STATS,
  games: {},
}

function createEmptyGameStats(): ArcadeGameStats {
  return { ...DEFAULT_GAME_STATS }
}

function sanitizeNumber(value: unknown, fallback: number) {
  return typeof value === 'number' && Number.isFinite(value) ? value : fallback
}

function sanitizeGameStats(value: unknown): ArcadeGameStats {
  const stats = typeof value === 'object' && value !== null ? (value as Partial<ArcadeGameStats>) : {}

  return {
    bestScore: sanitizeNumber(stats.bestScore, 0),
    lastScore: sanitizeNumber(stats.lastScore, 0),
    gamesPlayed: sanitizeNumber(stats.gamesPlayed, 0),
    totalScore: sanitizeNumber(stats.totalScore, 0),
    totalLudionsEarned: sanitizeNumber(stats.totalLudionsEarned, 0),
  }
}

function normalizeArcadeStats(value: unknown): ArcadeStats {
  const rawStats = typeof value === 'object' && value !== null ? (value as Partial<ArcadeStats>) : {}
  const rawGames = typeof rawStats.games === 'object' && rawStats.games !== null ? rawStats.games : {}

  const games = Object.entries(rawGames).reduce<Record<string, ArcadeGameStats>>((normalizedGames, [gameId, stats]) => {
    normalizedGames[gameId] = sanitizeGameStats(stats)
    return normalizedGames
  }, {})

  const gameValues = Object.values(games)
  const derivedGamesPlayed = gameValues.reduce((total, stats) => total + stats.gamesPlayed, 0)
  const derivedBestScore = gameValues.reduce((bestScore, stats) => Math.max(bestScore, stats.bestScore), 0)
  const derivedTotalScore = gameValues.reduce((total, stats) => total + stats.totalScore, 0)
  const derivedTotalLudions = gameValues.reduce((total, stats) => total + stats.totalLudionsEarned, 0)

  return {
    gamesPlayed: sanitizeNumber(rawStats.gamesPlayed, derivedGamesPlayed),
    bestScore: sanitizeNumber(rawStats.bestScore, derivedBestScore),
    lastScore: sanitizeNumber(rawStats.lastScore, 0),
    totalScore: sanitizeNumber(rawStats.totalScore, derivedTotalScore),
    totalLudionsEarned: sanitizeNumber(rawStats.totalLudionsEarned, derivedTotalLudions),
    games,
  }
}

function readStoredJson(storageKey: string) {
  if (typeof window === 'undefined') {
    return null
  }

  const rawStats = window.localStorage.getItem(storageKey)

  if (!rawStats) {
    return null
  }

  try {
    return JSON.parse(rawStats)
  } catch {
    return null
  }
}

function readLegacyClickerStats(): ArcadeStats | null {
  const legacyStats = readStoredJson(CLICKER_STATS_STORAGE_KEY) as Partial<ClickerStats> | null

  if (!legacyStats) {
    return null
  }

  const clickerStats: ArcadeGameStats = {
    bestScore: sanitizeNumber(legacyStats.bestScore, 0),
    lastScore: sanitizeNumber(legacyStats.lastScore, 0),
    gamesPlayed: sanitizeNumber(legacyStats.gamesPlayed, 0),
    totalScore: sanitizeNumber(legacyStats.totalClicks, 0),
    totalLudionsEarned: sanitizeNumber(legacyStats.totalLudionsEarned, 0),
  }

  return {
    ...clickerStats,
    games: {
      [PRIMARY_CLICKER_GAME_ID]: clickerStats,
    },
  }
}

export function getEmptyGameStats(): ArcadeGameStats {
  return createEmptyGameStats()
}

export function getGameStats(stats: ArcadeStats, gameId: string): ArcadeGameStats {
  return stats.games[gameId] ?? createEmptyGameStats()
}

export function readArcadeStats(): ArcadeStats {
  if (typeof window === 'undefined') {
    return DEFAULT_ARCADE_STATS
  }

  const arcadeStats = readStoredJson(ARCADE_STATS_STORAGE_KEY)

  if (arcadeStats) {
    return normalizeArcadeStats(arcadeStats)
  }

  return readLegacyClickerStats() ?? DEFAULT_ARCADE_STATS
}

export function writeArcadeStats(stats: ArcadeStats) {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.setItem(ARCADE_STATS_STORAGE_KEY, JSON.stringify(stats))
}

export function buildArcadeStatsAfterRound(currentStats: ArcadeStats, round: CompletedGameRound) {
  const score = Math.max(0, sanitizeNumber(round.score, 0))
  const reward = getLudionReward(score)
  const previousGameStats = getGameStats(currentStats, round.gameId)

  const nextGameStats: ArcadeGameStats = {
    bestScore: Math.max(previousGameStats.bestScore, score),
    lastScore: score,
    gamesPlayed: previousGameStats.gamesPlayed + 1,
    totalScore: previousGameStats.totalScore + score,
    totalLudionsEarned: previousGameStats.totalLudionsEarned + reward,
  }

  return {
    reward,
    stats: {
      bestScore: Math.max(currentStats.bestScore, score),
      lastScore: score,
      gamesPlayed: currentStats.gamesPlayed + 1,
      totalScore: currentStats.totalScore + score,
      totalLudionsEarned: currentStats.totalLudionsEarned + reward,
      games: {
        ...currentStats.games,
        [round.gameId]: nextGameStats,
      },
    },
  }
}

export function readClickerStats(): ClickerStats {
  const clickerStats = getGameStats(readArcadeStats(), PRIMARY_CLICKER_GAME_ID)

  return {
    ...clickerStats,
    totalClicks: clickerStats.totalScore,
  }
}

export function writeClickerStats(stats: ClickerStats) {
  const clickerStats: ArcadeGameStats = {
    bestScore: sanitizeNumber(stats.bestScore, 0),
    lastScore: sanitizeNumber(stats.lastScore, 0),
    gamesPlayed: sanitizeNumber(stats.gamesPlayed, 0),
    totalScore: sanitizeNumber(stats.totalClicks, stats.totalScore),
    totalLudionsEarned: sanitizeNumber(stats.totalLudionsEarned, 0),
  }
  const currentStats = readArcadeStats()
  const games = {
    ...currentStats.games,
    [PRIMARY_CLICKER_GAME_ID]: clickerStats,
  }
  const gameValues = Object.values(games)

  writeArcadeStats({
    bestScore: gameValues.reduce((bestScore, gameStats) => Math.max(bestScore, gameStats.bestScore), 0),
    lastScore: clickerStats.lastScore,
    gamesPlayed: gameValues.reduce((total, gameStats) => total + gameStats.gamesPlayed, 0),
    totalScore: gameValues.reduce((total, gameStats) => total + gameStats.totalScore, 0),
    totalLudionsEarned: gameValues.reduce((total, gameStats) => total + gameStats.totalLudionsEarned, 0),
    games,
  })
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
