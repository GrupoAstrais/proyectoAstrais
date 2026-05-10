import React from 'react'
import {
  getStoreItems,
  getUserData,
  resolvePetAssetUrl,
  type StoreItemResponse,
} from '../data/Api'
import { applyThemeColors } from '../styles/theme'
import type { UserData } from '../types/LoginRequest'

interface VisualPreferencesContextValue {
  userData: UserData | null
  storeItems: StoreItemResponse[]
  mascotAssetUrl: string | null
  isLoading: boolean
  refreshVisualPreferences: () => Promise<void>
}

const defaultContextValue: VisualPreferencesContextValue = {
  userData: null,
  storeItems: [],
  mascotAssetUrl: null,
  isLoading: false,
  refreshVisualPreferences: async () => undefined,
}

const VisualPreferencesContext = React.createContext<VisualPreferencesContextValue>(defaultContextValue)

export function VisualPreferencesProvider({ children }: { children: React.ReactNode }) {
  const [userData, setUserData] = React.useState<UserData | null>(null)
  const [storeItems, setStoreItems] = React.useState<StoreItemResponse[]>([])
  const [isLoading, setIsLoading] = React.useState(true)

  const refreshVisualPreferences = React.useCallback(async () => {
    if (typeof window === 'undefined' || !window.localStorage.getItem('jwtToken')) {
      applyThemeColors()
      setUserData(null)
      setStoreItems([])
      setIsLoading(false)
      return
    }

    try {
      setIsLoading(true)
      const [nextUserData, nextStoreItems] = await Promise.all([getUserData(), getStoreItems()])

      applyThemeColors(nextUserData.themeColors, nextStoreItems)
      setUserData(nextUserData)
      setStoreItems(nextStoreItems)
    } catch (error) {
      console.error('No se pudieron cargar las preferencias visuales:', error)
      applyThemeColors()
      setUserData(null)
      setStoreItems([])
    } finally {
      setIsLoading(false)
    }
  }, [])

  React.useEffect(() => {
    void refreshVisualPreferences()
  }, [refreshVisualPreferences])

  const mascotAssetUrl = React.useMemo(
    () => resolvePetAssetUrl(userData?.equippedPetRef),
    [userData?.equippedPetRef],
  )

  const contextValue = React.useMemo(
    () => ({
      userData,
      storeItems,
      mascotAssetUrl,
      isLoading,
      refreshVisualPreferences,
    }),
    [isLoading, mascotAssetUrl, refreshVisualPreferences, storeItems, userData],
  )

  return (
    <VisualPreferencesContext.Provider value={contextValue}>
      {children}
    </VisualPreferencesContext.Provider>
  )
}

export function useVisualPreferences() {
  return React.useContext(VisualPreferencesContext)
}
