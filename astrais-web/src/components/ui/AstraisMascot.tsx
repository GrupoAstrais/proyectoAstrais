import React from 'react'
import Lottie from 'lottie-react'
import astra from '../../assets/astra.png'
import astraAlt from '../../assets/astra2.png'
import { isLottieAssetUrl } from '../../data/Api'
import { useVisualPreferences } from '../../context/VisualPreferencesContext'

interface AstraisMascotProps {
  assetUrl?: string | null
  fallback?: 'primary' | 'secondary'
  alt?: string
  className?: string
}

// Resuelve la mascota entre recurso equipado, recurso recibido y fallback local.
export default function AstraisMascot({
  assetUrl,
  fallback = 'primary',
  alt = 'Mascota Astrais',
  className = '',
}: AstraisMascotProps) {
  const { mascotAssetUrl } = useVisualPreferences()
  const fallbackAsset = fallback === 'secondary' ? astraAlt : astra
  const resolvedAssetUrl = assetUrl ?? mascotAssetUrl
  const [animationData, setAnimationData] = React.useState<Record<string, unknown> | null>(null)
  const [hasFailed, setHasFailed] = React.useState(false)
  // Solo intenta Lottie si el recurso termina en JSON y no ha fallado antes.
  const shouldRenderLottie = isLottieAssetUrl(resolvedAssetUrl) && !hasFailed

  React.useEffect(() => {
    setHasFailed(false)
    setAnimationData(null)

    if (!isLottieAssetUrl(resolvedAssetUrl)) {
      return
    }

    const controller = new AbortController()

    // Se cancela la descarga si cambia la mascota o se desmonta el componente.
    const loadAnimation = async () => {
      try {
        const response = await fetch(resolvedAssetUrl!, { signal: controller.signal })

        if (!response.ok) {
          throw new Error(`No se pudo cargar la mascota (${response.status})`)
        }

        const data = await response.json() as Record<string, unknown>
        setAnimationData(data)
      } catch (error) {
        if (!controller.signal.aborted) {
          console.error('No se pudo renderizar la mascota Lottie:', error)
          setHasFailed(true)
        }
      }
    }

    void loadAnimation()

    return () => controller.abort()
  }, [resolvedAssetUrl])

  if (shouldRenderLottie) {
    return (
      <div className={`grid aspect-square place-items-center ${className}`} role="img" aria-label={alt}>
        {animationData ? (
          <Lottie animationData={animationData} loop autoplay className="h-5/6 w-5/6 object-contain" />
        ) : null}
      </div>
    )
  }

  return (
    <span className={`grid aspect-square place-items-center ${className}`}>
      <img
        src={hasFailed || !resolvedAssetUrl ? fallbackAsset : resolvedAssetUrl}
        alt={alt}
        className="h-full w-full object-contain"
        onError={(event) => {
          event.currentTarget.src = fallbackAsset
        }}
      />
    </span>
  )
}
