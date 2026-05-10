import React from 'react'
import {
  getStoreItems,
  getUserData,
  resolvePetAssetUrl,
  resolveStoreAssetUrl,
  type StoreItemResponse,
} from '../data/Api'
import {
  getLocalEquippedPetAssetRef,
  getLocalEquippedAvatarAssetRef,
  getLocalEquippedThemeColors,
  LOCAL_SHOP_UPDATED_EVENT,
  mergeLocalStoreItems,
} from '../data/localShopStorage'
import { applyThemeColors } from '../styles/theme'
import type { UserData } from '../types/LoginRequest'

interface VisualPreferencesContextValue {
  userData: UserData | null
  storeItems: StoreItemResponse[]
  mascotAssetUrl: string | null
  avatarAssetUrl: string | null
  isLoading: boolean
  refreshVisualPreferences: () => Promise<void>
}

const defaultContextValue: VisualPreferencesContextValue = {
  userData: null,
  storeItems: [],
  mascotAssetUrl: null,
  avatarAssetUrl: null,
  isLoading: false,
  refreshVisualPreferences: async () => undefined,
}

const VisualPreferencesContext = React.createContext<VisualPreferencesContextValue>(defaultContextValue)

// Proveedor global para tema, mascota y avatar equipados.
export function VisualPreferencesProvider({ children }: { children: React.ReactNode }) {
  const [userData, setUserData] = React.useState<UserData | null>(null)
  const [storeItems, setStoreItems] = React.useState<StoreItemResponse[]>([])
  const [isLoading, setIsLoading] = React.useState(true)

  const refreshVisualPreferences = React.useCallback(async () => {
    if (typeof window === 'undefined' || !window.localStorage.getItem('jwtToken')) {
      // Sin sesion se aplica el tema por defecto para no depender de la API.
      applyThemeColors()
      setUserData(null)
      setStoreItems([])
      setIsLoading(false)
      return
    }

    try {
      setIsLoading(true)
      const [nextUserData, nextStoreItems] = await Promise.all([getUserData(), getStoreItems()])
      const mergedStoreItems = mergeLocalStoreItems(nextUserData.id, nextStoreItems)

      // La tienda local puede sobreescribir lo equipado sin esperar al servidor.
      applyThemeColors(getLocalEquippedThemeColors(nextUserData.id, nextStoreItems, nextUserData.themeColors), nextStoreItems)
      setUserData(nextUserData)
      setStoreItems(mergedStoreItems)
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

  React.useEffect(() => {
    if (typeof window === 'undefined') {
      return
    }

    const applyLocalShopPreferences = () => {
      if (!userData) {
        return
      }

      // Escucha compras locales para refrescar la apariencia al instante.
      setStoreItems((currentStoreItems) => {
        const mergedStoreItems = mergeLocalStoreItems(userData.id, currentStoreItems)
        applyThemeColors(getLocalEquippedThemeColors(userData.id, currentStoreItems, userData.themeColors), currentStoreItems)
        return mergedStoreItems
      })
    }

    window.addEventListener(LOCAL_SHOP_UPDATED_EVENT, applyLocalShopPreferences)
    return () => window.removeEventListener(LOCAL_SHOP_UPDATED_EVENT, applyLocalShopPreferences)
  }, [userData])

  // Calcula referencias derivadas para evitar resolver URLs en cada render.
  const mascotAssetRef = React.useMemo(
    () => getLocalEquippedPetAssetRef(userData?.id, storeItems, userData?.equippedPetRef),
    [storeItems, userData?.equippedPetRef, userData?.id],
  )
  const mascotAssetUrl = React.useMemo(() => resolvePetAssetUrl(mascotAssetRef), [mascotAssetRef])
  const avatarAssetRef = React.useMemo(
    () => getLocalEquippedAvatarAssetRef(userData?.id, storeItems, userData?.equippedAvatarRef),
    [storeItems, userData?.equippedAvatarRef, userData?.id],
  )
  const avatarAssetUrl = React.useMemo(() => resolveStoreAssetUrl('AVATAR_PART', avatarAssetRef), [avatarAssetRef])

  const contextValue = React.useMemo(
    () => ({
      userData,
      storeItems,
      mascotAssetUrl,
      avatarAssetUrl,
      isLoading,
      refreshVisualPreferences,
    }),
    [avatarAssetUrl, isLoading, mascotAssetUrl, refreshVisualPreferences, storeItems, userData],
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
