import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import loginBg from '../../assets/login-bg.jpg'
import { handleGoogleCallback, loginWithGoogle, performLogin } from '../../data/Api'

export default function Login() {
  const navigate = useNavigate()
  // 👇 AÑADE ESTO - se ejecuta en cada render, no espera al useEffect
  console.log('LOGIN RENDER - URL completa:', window.location.href)
  console.log('LOGIN RENDER - Search params:', window.location.search)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [rememberMe, setRememberMe] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const uid = params.get('uid')
    const hadToRegister = params.get('hadToRegister')
    const jwtAccessToken = params.get('jwtAccessToken') ?? params.get('accessToken')
    const jwtRefreshToken = params.get('jwtRefreshToken') ?? params.get('refreshToken')

    console.log('Params encontrados:', { uid, hadToRegister, jwtAccessToken: !!jwtAccessToken, jwtRefreshToken: !!jwtRefreshToken })

    if (!jwtAccessToken || !jwtRefreshToken || hadToRegister === null) {
      console.log('GUARD ACTIVADO - falta algún param, saliendo')
      return
    }
    void handleGoogleCallback(
      Number(uid ?? 0),
      hadToRegister === 'true',
      jwtAccessToken,
      jwtRefreshToken
    )
      .then(() => navigate('/home', { replace: true }))
      .catch(() => setError('No se pudo completar el login con Google.'))
  }, [navigate])

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!email.trim() || !password.trim()) {
      setError('Complete user/email and password to continue.')
      return
    }

    await performLogin({email: email, passwd: password}).then(() => {
      navigate('/home')
    }).catch(()=> {
      setError('Usuario o contraseña no existen')
    })
  }

  return (
    <section
      style={{ backgroundImage: `url(${loginBg})` }}
      className="relative grid min-h-screen place-items-center overflow-hidden bg-cover bg-no-repeat px-6 bg-position-[0_-20cm] max-[480px]:px-4"
    >
      <div className="pointer-events-none absolute inset-0" />

      <article
        aria-labelledby="login-title"
        className="relative z-10 w-full max-w-105 -translate-y-[3vh] rounded-[22px] border border-white/15 bg-[color-mix(in_srgb,var(--astrais-surface)_72%,transparent)] p-8 text-[var(--astrais-text)] shadow-[0_30px_60px_color-mix(in_srgb,var(--astrais-background)_74%,transparent)] backdrop-blur-sm max-[480px]:-translate-y-[1.5vh] max-[480px]:p-5.5"
      >
        <header className="mb-5.5">
          <p className="m-0 text-xs tracking-[0.2em] text-[var(--astrais-rarity-epic)] uppercase">Astrais</p>
          <h1 id="login-title" className="my-2 text-[clamp(1.9rem,4vw,2.35rem)] leading-[1.1]">Log in</h1>
          <p className="m-0 text-[color-mix(in_srgb,var(--astrais-text)_90%,transparent)]">Welcome back let's continue your adventure.</p>
        </header>

        <form className="grid gap-4" onSubmit={onSubmit} noValidate>
          <div className="grid gap-2">
            <label htmlFor="email" className="text-[0.95rem]">Email</label>
            <input
              id="email"
              type="text"
              placeholder="user@mail.com"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="username"
              required
              className="rounded-xl border border-white/25 bg-black/25 px-3.5 py-3 text-base text-white placeholder:text-white/65 focus-visible:outline-2 focus-visible:outline-[var(--astrais-rarity-epic)] focus-visible:outline-offset-1"
            />
          </div>

          <div className="grid gap-2">
            <label htmlFor="password" className="text-[0.95rem]">Password</label>
            <input
              id="password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              required
              className="rounded-xl border border-white/25 bg-black/25 px-3.5 py-3 text-base text-white placeholder:text-white/65 focus-visible:outline-2 focus-visible:outline-[var(--astrais-rarity-epic)] focus-visible:outline-offset-1"
            />
          </div>

          <div className="flex items-center justify-between gap-2 text-[0.9rem] max-[480px]:flex-col max-[480px]:items-start">
            <label className="inline-flex items-center gap-2" htmlFor="rememberMe">
              <input
                id="rememberMe"
                type="checkbox"
                checked={rememberMe}
                onChange={(event) => setRememberMe(event.target.checked)}
              />
              Remember me
            </label>
            <Link to="/forgot-password" className="text-white no-underline hover:underline">Forgot your password?</Link>
          </div>

          {error ? (
            <p className="m-0 rounded-[10px] border border-[color-mix(in_srgb,var(--astrais-error)_55%,transparent)] bg-[color-mix(in_srgb,var(--astrais-error)_20%,transparent)] p-2.5 text-[0.9rem]" role="status" aria-live="polite">
              {error}
            </p>
          ) : null}

          <button
            className="cursor-pointer rounded-xl border border-transparent [background:var(--astrais-cta-bg)] p-3 text-base font-semibold text-white shadow-[0_10px_30px_color-mix(in_srgb,var(--astrais-primary)_45%,transparent)] transition duration-150 ease-in hover:-translate-y-px"
            type="submit"
          >
            Log in
          </button>

          <button
            className="cursor-pointer rounded-xl border border-white/20 bg-white/10 p-3 text-base font-semibold text-white transition duration-150 ease-in hover:-translate-y-px"
            type="button"
            onClick={loginWithGoogle}
          >
            Continue with Google
          </button>

          <p className="m-0 text-center text-[0.95rem] text-[color-mix(in_srgb,var(--astrais-text)_92%,transparent)]">
            You don't have an account? <Link to="/register" className="text-white no-underline hover:underline">Sign up</Link>
          </p>
        </form>
      </article>
    </section>
  )
}

