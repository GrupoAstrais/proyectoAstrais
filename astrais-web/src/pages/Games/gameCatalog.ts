import { ASTRA_MEMORY_GAME_ID, NEBULA_DASH_GAME_ID, PRIMARY_CLICKER_GAME_ID } from './gameStorage'

export type GameAvailability = 'available' | 'upcoming'

export interface ArcadeGameDefinition {
  id: string
  title: string
  description: string
  availability: GameAvailability
  statusLabel: string
  difficulty: string
  rewardNote: string
  embedPath: string
}

export const gameCatalog: ArcadeGameDefinition[] = [
  {
    id: PRIMARY_CLICKER_GAME_ID,
    title: 'Atrapa Ludiones',
    description: 'Diez segundos de precision: cada pulsacion suma puntos y alimenta tu reserva.',
    availability: 'available',
    statusLabel: 'Jugable',
    difficulty: 'Reflejos',
    rewardNote: '12 + 4 por punto',
    embedPath: `/games/embed/${PRIMARY_CLICKER_GAME_ID}`,
  },
  {
    id: NEBULA_DASH_GAME_ID,
    title: 'Nebula Dash',
    description: 'Carrera de carriles entre meteoros: esquiva, recoge fragmentos y protege tus escudos.',
    availability: 'available',
    statusLabel: 'Jugable',
    difficulty: 'Velocidad',
    rewardNote: '30s de carrera',
    embedPath: `/games/embed/${NEBULA_DASH_GAME_ID}`,
  },
  {
    id: ASTRA_MEMORY_GAME_ID,
    title: 'Astra Memory',
    description: 'Memoriza pads de energia, repite la secuencia y aguanta con solo tres fallos.',
    availability: 'available',
    statusLabel: 'Jugable',
    difficulty: 'Memoria',
    rewardNote: 'Combo mental',
    embedPath: `/games/embed/${ASTRA_MEMORY_GAME_ID}`,
  },
]

export function getGameById(gameId: string) {
  return gameCatalog.find((game) => game.id === gameId)
}
