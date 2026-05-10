import type { StoreItemResponse } from './Api'

// Persistencia local de compras y equipamiento de la tienda.
type LocalShopSlot = 'theme' | 'companion' | 'avatar'

interface LocalShopPurchaseResult {
  ok: boolean
  balance: number
  spent: number
}

const LOCAL_SHOP_PREFIX = 'astrais.shop'
const FALLBACK_USER_KEY = 'current'
export const LOCAL_SHOP_UPDATED_EVENT = 'astrais.localShop.updated'

function notifyLocalShopUpdated() {
  if (typeof window === 'undefined') {
    return
  }

  // Avisa a otros componentes para que refresquen tema y avatar.
  window.dispatchEvent(new Event(LOCAL_SHOP_UPDATED_EVENT))
}

function getUserKey(userId?: number | null) {
  return typeof userId === 'number' && Number.isFinite(userId) ? String(userId) : FALLBACK_USER_KEY
}

function getStorageKey(userId: number | null | undefined, suffix: string) {
  // La clave incluye usuario para no mezclar compras entre cuentas.
  return `${LOCAL_SHOP_PREFIX}.${getUserKey(userId)}.${suffix}`
}

function readJson<T>(key: string, fallback: T): T {
  if (typeof window === 'undefined') {
    return fallback
  }

  try {
    const rawValue = window.localStorage.getItem(key)
    return rawValue ? JSON.parse(rawValue) as T : fallback
  } catch {
    // Si localStorage contiene datos corruptos, se usa un valor seguro.
    return fallback
  }
}

function writeJson<T>(key: string, value: T) {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.setItem(key, JSON.stringify(value))
}

function readNumber(key: string) {
  if (typeof window === 'undefined') {
    return null
  }

  const rawValue = window.localStorage.getItem(key)
  if (rawValue === null) {
    return null
  }

  const parsedValue = Number(rawValue)
  return Number.isFinite(parsedValue) ? parsedValue : null
}

function writeNumber(key: string, value: number) {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.setItem(key, String(Math.max(0, Math.trunc(value))))
}

function readOwnedIds(userId?: number | null) {
  const values = readJson<unknown[]>(getStorageKey(userId, 'owned'), [])
  return values.filter((value): value is number => typeof value === 'number' && Number.isFinite(value))
}

function writeOwnedIds(userId: number | null | undefined, ownedIds: number[]) {
  writeJson(getStorageKey(userId, 'owned'), Array.from(new Set(ownedIds)))
}

function readEquippedIds(userId?: number | null): Partial<Record<LocalShopSlot, number>> {
  const values = readJson<Partial<Record<LocalShopSlot, unknown>>>(getStorageKey(userId, 'equipped'), {})

  return Object.entries(values).reduce<Partial<Record<LocalShopSlot, number>>>((equippedIds, [slot, value]) => {
    if ((slot === 'theme' || slot === 'companion' || slot === 'avatar') && typeof value === 'number' && Number.isFinite(value)) {
      equippedIds[slot] = value
    }

    return equippedIds
  }, {})
}

function writeEquippedIds(userId: number | null | undefined, equippedIds: Partial<Record<LocalShopSlot, number>>) {
  writeJson(getStorageKey(userId, 'equipped'), equippedIds)
}

export function getLocalStoreSlot(item: Pick<StoreItemResponse, 'type'>): LocalShopSlot {
  if (item.type === 'APP_THEME') {
    return 'theme'
  }

  if (item.type === 'PET') {
    return 'companion'
  }

  return 'avatar'
}

export function isDefaultThemeItem(item: Pick<StoreItemResponse, 'type' | 'coleccion'>) {
  return item.type === 'APP_THEME' && item.coleccion.trim().toLowerCase() === 'default'
}

export function readLocalBalance(userId?: number | null, initialBalance = 0) {
  const balanceKey = getStorageKey(userId, 'balance')
  const storedBalance = readNumber(balanceKey)

  if (storedBalance !== null) {
    return storedBalance
  }

  const normalizedInitialBalance = Math.max(0, Math.trunc(initialBalance))
  writeNumber(balanceKey, normalizedInitialBalance)
  return normalizedInitialBalance
}

export function readLocalSpent(userId?: number | null) {
  return readNumber(getStorageKey(userId, 'spent')) ?? 0
}

export function addLocalLudiones(userId: number | null | undefined, amount: number, initialBalance = 0) {
  const currentBalance = readLocalBalance(userId, initialBalance)
  const nextBalance = currentBalance + Math.max(0, Math.trunc(amount))

  writeNumber(getStorageKey(userId, 'balance'), nextBalance)
  notifyLocalShopUpdated()
  return nextBalance
}

export function mergeLocalStoreItems(userId: number | null | undefined, storeItems: StoreItemResponse[]) {
  const ownedIds = new Set(readOwnedIds(userId))
  const equippedIds = readEquippedIds(userId)

  // Fusiona estado del backend con compras hechas solo en este navegador.
  return storeItems.map((item) => {
    const slot = getLocalStoreSlot(item)
    const locallyEquippedId = equippedIds[slot]

    return {
      ...item,
      owned: item.owned || ownedIds.has(item.id) || isDefaultThemeItem(item),
      equipped: typeof locallyEquippedId === 'number' ? locallyEquippedId === item.id : item.equipped,
    }
  })
}

export function buyLocalStoreItem(
  userId: number | null | undefined,
  item: StoreItemResponse,
  currentBalance = readLocalBalance(userId),
): LocalShopPurchaseResult {
  const ownedIds = readOwnedIds(userId)
  const alreadyOwned = item.owned || ownedIds.includes(item.id) || isDefaultThemeItem(item)
  const spent = readLocalSpent(userId)

  // Comprar un articulo ya poseido no descuenta ludiones.
  if (alreadyOwned) {
    return { ok: true, balance: currentBalance, spent }
  }

  if (currentBalance < item.price) {
    return { ok: false, balance: currentBalance, spent }
  }

  const nextBalance = currentBalance - item.price
  const nextSpent = spent + item.price

  writeNumber(getStorageKey(userId, 'balance'), nextBalance)
  writeNumber(getStorageKey(userId, 'spent'), nextSpent)
  writeOwnedIds(userId, [...ownedIds, item.id])
  notifyLocalShopUpdated()

  return { ok: true, balance: nextBalance, spent: nextSpent }
}

export function equipLocalStoreItem(userId: number | null | undefined, item: StoreItemResponse) {
  const ownedIds = readOwnedIds(userId)
  const isOwned = item.owned || ownedIds.includes(item.id) || isDefaultThemeItem(item)

  if (!isOwned) {
    return false
  }

  // Solo queda un articulo equipado por cada ranura visual.
  const equippedIds = readEquippedIds(userId)
  equippedIds[getLocalStoreSlot(item)] = item.id
  writeEquippedIds(userId, equippedIds)
  notifyLocalShopUpdated()

  return true
}

export function getLocalEquippedThemeColors(
  userId: number | null | undefined,
  storeItems: StoreItemResponse[],
  fallbackThemeColors?: string | null,
) {
  return mergeLocalStoreItems(userId, storeItems).find((item) => item.type === 'APP_THEME' && item.equipped)?.theme ?? fallbackThemeColors
}

export function getLocalEquippedPetAssetRef(
  userId: number | null | undefined,
  storeItems: StoreItemResponse[],
  fallbackPetRef?: string | null,
) {
  return mergeLocalStoreItems(userId, storeItems).find((item) => item.type === 'PET' && item.equipped)?.assetRef ?? fallbackPetRef
}

export function getLocalEquippedAvatarAssetRef(
  userId: number | null | undefined,
  storeItems: StoreItemResponse[],
  fallbackAvatarRef?: string | null,
) {
  return mergeLocalStoreItems(userId, storeItems).find((item) => item.type === 'AVATAR_PART' && item.equipped)?.assetRef ?? fallbackAvatarRef
}
