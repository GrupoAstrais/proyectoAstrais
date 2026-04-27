export const GAME_ROUND_COMPLETED_MESSAGE = 'astrais:game-round-completed'

export interface GameRoundCompletedMessage {
  type: typeof GAME_ROUND_COMPLETED_MESSAGE
  gameId: string
  score: number
  durationSeconds: number
}

export function isGameRoundCompletedMessage(value: unknown): value is GameRoundCompletedMessage {
  if (typeof value !== 'object' || value === null) {
    return false
  }

  const message = value as Partial<GameRoundCompletedMessage>

  return (
    message.type === GAME_ROUND_COMPLETED_MESSAGE &&
    typeof message.gameId === 'string' &&
    typeof message.score === 'number' &&
    Number.isFinite(message.score) &&
    typeof message.durationSeconds === 'number' &&
    Number.isFinite(message.durationSeconds)
  )
}
