import type { ClickerStats } from '../Games/gameStorage'

export type AchievementCategory = 'Minijuegos' | 'Exploracion' | 'Constancia' | 'Coleccion'
export type AchievementRarity = 'Comun' | 'Raro' | 'Epico' | 'Legendario'
export type AchievementFilter = 'all' | 'unlocked' | 'progress' | 'locked'

interface AchievementDefinition {
  id: string
  title: string
  description: string
  category: AchievementCategory
  rarity: AchievementRarity
  goal: number
  reward: number
  hint: string
  getProgress: (stats: ClickerStats) => number
}

export interface AchievementViewModel {
  id: string
  title: string
  description: string
  category: AchievementCategory
  rarity: AchievementRarity
  goal: number
  reward: number
  hint: string
  progress: number
  percent: number
  unlocked: boolean
  claimed: boolean
}

const achievementCatalog: AchievementDefinition[] = [
  {
    id: 'bienvenida-orbital',
    title: 'Bienvenida orbital',
    description: 'Descubre el panel de logros y activa tu hoja de ruta galactica.',
    category: 'Exploracion',
    rarity: 'Comun',
    goal: 1,
    reward: 25,
    hint: 'Ya esta listo para reclamar como premio de bienvenida.',
    getProgress: () => 1,
  },
  {
    id: 'primer-impulso',
    title: 'Primer impulso',
    description: 'Completa tu primera sesion en Atrapa Ludiones.',
    category: 'Minijuegos',
    rarity: 'Comun',
    goal: 1,
    reward: 40,
    hint: 'Juega una partida completa en la pestana de minijuegos.',
    getProgress: (stats) => stats.gamesPlayed,
  },
  {
    id: 'cazador-de-ludiones',
    title: 'Cazador de ludiones',
    description: 'Alcanza una puntuacion alta y manten el ritmo durante una ronda.',
    category: 'Minijuegos',
    rarity: 'Raro',
    goal: 18,
    reward: 70,
    hint: 'Apunta a una racha de clics rapida durante los diez segundos.',
    getProgress: (stats) => stats.bestScore,
  },
  {
    id: 'reactor-estable',
    title: 'Reactor estable',
    description: 'Acumula energia sostenida manteniendo el ritmo en varias partidas.',
    category: 'Constancia',
    rarity: 'Raro',
    goal: 75,
    reward: 90,
    hint: 'Cada clic cuenta. Varias partidas cortas tambien sirven.',
    getProgress: (stats) => stats.totalClicks,
  },
  {
    id: 'coleccionista-curioso',
    title: 'Coleccionista curioso',
    description: 'Explora funciones del ecosistema Astrais y amplia tu vitrina.',
    category: 'Coleccion',
    rarity: 'Epico',
    goal: 5,
    reward: 120,
    hint: 'Seguira creciendo cuando la web conecte tareas, tienda y perfil.',
    getProgress: () => 2,
  },
  {
    id: 'leyenda-del-arcade',
    title: 'Leyenda del arcade',
    description: 'Reune una reserva enorme de ludiones gracias a tus mejores rondas.',
    category: 'Minijuegos',
    rarity: 'Legendario',
    goal: 160,
    reward: 200,
    hint: 'Las recompensas del minijuego se acumulan automaticamente entre sesiones.',
    getProgress: (stats) => stats.totalLudionsEarned,
  },
]

export function buildAchievements(stats: ClickerStats, claimedIds: string[]): AchievementViewModel[] {
  return achievementCatalog.map((achievement) => {
    const rawProgress = achievement.getProgress(stats)
    const progress = Math.max(0, Math.min(rawProgress, achievement.goal))
    const percent = Math.round((progress / achievement.goal) * 100)
    const unlocked = progress >= achievement.goal

    return {
      ...achievement,
      progress,
      percent,
      unlocked,
      claimed: claimedIds.includes(achievement.id),
    }
  })
}

export function matchesAchievementFilter(achievement: AchievementViewModel, filter: AchievementFilter) {
  if (filter === 'unlocked') {
    return achievement.unlocked
  }

  if (filter === 'progress') {
    return !achievement.unlocked && achievement.progress > 0
  }

  if (filter === 'locked') {
    return !achievement.unlocked && achievement.progress === 0
  }

  return true
}
