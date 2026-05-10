import { useEffect } from 'react'
import { useNavigate } from 'react-router'
import { handleGoogleCallback } from '../../data/Api'

// Pantalla puente para guardar tokens recibidos desde Google OAuth.
export default function GoogleCallback() {
  const navigate = useNavigate()

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const accessToken = params.get('jwtAccessToken') ?? params.get('accessToken')
    const refreshToken = params.get('jwtRefreshToken') ?? params.get('refreshToken')
    const uidParam = params.get('uid')
    const hadToRegister = params.get('hadToRegister')

    // El callback acepta nombres nuevos y antiguos de parametros.
    console.log('GoogleCallback params:', { uidParam, accessToken: !!accessToken, refreshToken: !!refreshToken, hadToRegister })

    if (!accessToken || !refreshToken || hadToRegister === null) {
      navigate('/login', { replace: true })
      return
    }

    handleGoogleCallback(Number(uidParam ?? 0), hadToRegister === 'true', accessToken, refreshToken)
      .then(() => navigate('/home', { replace: true }))
      .catch(() => navigate('/login', { replace: true }))
  }, [navigate])

  return null
}
