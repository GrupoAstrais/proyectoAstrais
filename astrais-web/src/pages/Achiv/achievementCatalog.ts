import {
  ASTRA_MEMORY_GAME_ID,
  getGameStats,
  NEBULA_DASH_GAME_ID,
  PRIMARY_CLICKER_GAME_ID,
  type ArcadeStats,
} from '../Games/gameStorage'

// Catalogo declarativo de logros y sus reglas de progreso.
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
  getProgress: (stats: ArcadeStats) => number
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
    getProgress: (stats) => getGameStats(stats, PRIMARY_CLICKER_GAME_ID).gamesPlayed,
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
    getProgress: (stats) => getGameStats(stats, PRIMARY_CLICKER_GAME_ID).bestScore,
  },
  {
    id: 'reactor-estable',
    title: 'Reactor estable',
    description: 'Acumula energia sostenida manteniendo el ritmo en varias partidas.',
    category: 'Constancia',
    rarity: 'Raro',
    goal: 75,
    reward: 90,
    hint: 'Cada punto cuenta. Varias partidas cortas tambien sirven.',
    getProgress: (stats) => stats.totalScore,
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
  {
    id: 'selector-de-cabinas',
    title: 'Selector de cabinas',
    description: 'Activa el nuevo catalogo y revisa una cabina del arcade.',
    category: 'Exploracion',
    rarity: 'Comun',
    goal: 1,
    reward: 20,
    hint: 'Abre la seccion de minijuegos y carga cualquier cabina en el visor.',
    getProgress: () => 1,
  },
  {
    id: 'maraton-breve',
    title: 'Maraton breve',
    description: 'Completa varias rondas desde el iframe del catalogo.',
    category: 'Constancia',
    rarity: 'Raro',
    goal: 5,
    reward: 80,
    hint: 'Termina cinco partidas en cualquier cabina disponible.',
    getProgress: (stats) => stats.gamesPlayed,
  },
  {
    id: 'pulso-constante',
    title: 'Pulso constante',
    description: 'Suma puntuacion total entre distintas sesiones del arcade.',
    category: 'Minijuegos',
    rarity: 'Epico',
    goal: 220,
    reward: 140,
    hint: 'Acumula puntos jugando rondas completas en el visor.',
    getProgress: (stats) => stats.totalScore,
  },
  {
    id: 'reserva-prometida',
    title: 'Reserva prometida',
    description: 'Guarda una bolsa amplia de ludiones gracias a tus partidas.',
    category: 'Coleccion',
    rarity: 'Legendario',
    goal: 300,
    reward: 220,
    hint: 'Las recompensas se calculan al finalizar cada ronda jugable.',
    getProgress: (stats) => stats.totalLudionsEarned,
  },
  {
    id: 'primer-dash-nebular',
    title: 'Primer dash nebular',
    description: 'Completa tu primera carrera en Nebula Dash.',
    category: 'Minijuegos',
    rarity: 'Comun',
    goal: 1,
    reward: 45,
    hint: 'Entra en Nebula Dash y termina una carrera, aunque pierdas los escudos.',
    getProgress: (stats) => getGameStats(stats, NEBULA_DASH_GAME_ID).gamesPlayed,
  },
  {
    id: 'piloto-de-meteoros',
    title: 'Piloto de meteoros',
    description: 'Firma una carrera de alto rendimiento esquivando y recogiendo fragmentos.',
    category: 'Minijuegos',
    rarity: 'Epico',
    goal: 90,
    reward: 135,
    hint: 'Mueve la nave por carriles y prioriza sobrevivir hasta el final.',
    getProgress: (stats) => getGameStats(stats, NEBULA_DASH_GAME_ID).bestScore,
  },
  {
    id: 'primer-patron-astra',
    title: 'Primer patron astra',
    description: 'Completa tu primera secuencia en Astra Memory.',
    category: 'Minijuegos',
    rarity: 'Comun',
    goal: 1,
    reward: 45,
    hint: 'Juega una partida de Astra Memory y repite el patron de pads.',
    getProgress: (stats) => getGameStats(stats, ASTRA_MEMORY_GAME_ID).gamesPlayed,
  },
  {
    id: 'memoria-de-cristal',
    title: 'Memoria de cristal',
    description: 'Alcanza una puntuacion alta encadenando varias rondas de memoria.',
    category: 'Constancia',
    rarity: 'Epico',
    goal: 80,
    reward: 135,
    hint: 'Mantente atento durante la fase de muestra antes de pulsar.',
    getProgress: (stats) => getGameStats(stats, ASTRA_MEMORY_GAME_ID).bestScore,
  },
]

export function buildAchievements(stats: ArcadeStats, claimedIds: string[]): AchievementViewModel[] {
  // Calcula progreso y estado visible a partir de estadisticas persistidas.
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
  // Centraliza los filtros para que la vista no repita condiciones.
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
