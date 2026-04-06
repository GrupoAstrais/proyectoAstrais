import { SHOP_ITEMS, type ShopSlot } from './shopCatalog'

export interface ShopState {
  ownedIds: string[]
  equippedBySlot: Partial<Record<ShopSlot, string>>
}

export const SHOP_STORAGE_KEY = 'astrais.shop.state'
export const SHOP_BASE_BALANCE = 260

const DEFAULT_SHOP_STATE: ShopState = {
  ownedIds: [],
  equippedBySlot: {},
}

export function readShopState(): ShopState {
  if (typeof window === 'undefined') {
    return DEFAULT_SHOP_STATE
  }

  try {
    const rawState = window.localStorage.getItem(SHOP_STORAGE_KEY)

    if (!rawState) {
      return DEFAULT_SHOP_STATE
    }

    const parsedState = JSON.parse(rawState) as Partial<ShopState>

    return {
      ownedIds: Array.isArray(parsedState.ownedIds)
        ? parsedState.ownedIds.filter((value): value is string => typeof value === 'string')
        : [],
      equippedBySlot: parsedState.equippedBySlot ?? {},
    }
  } catch {
    return DEFAULT_SHOP_STATE
  }
}

export function writeShopState(state: ShopState) {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.setItem(SHOP_STORAGE_KEY, JSON.stringify(state))
}

export function calculateSpentLudions(ownedIds: string[]) {
  return ownedIds.reduce((total, ownedId) => {
    const item = SHOP_ITEMS.find((shopItem) => shopItem.id === ownedId)
    return total + (item?.price ?? 0)
  }, 0)
}

export function calculateAvailableBalance(totalLudionsEarned: number, ownedIds: string[]) {
  return SHOP_BASE_BALANCE + totalLudionsEarned - calculateSpentLudions(ownedIds)
}
