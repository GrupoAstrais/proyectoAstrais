import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { confirmRegister, createUser, handleGoogleCallback, loginWithGoogle } from '../../data/Api'

// Politica minima de seguridad para altas por email.
const PASSWORD_SECURITY_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{12,}$/

// Pantalla de registro con verificacion por codigo.
export default function Register() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [name, setName] = useState('')
  const [password, setPassword] = useState('')
  const [passwordVer, setPasswordVer] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [codeSent, setCodeSent] = useState(false)
  const [error, setError] = useState('')

  const isPasswordSecure = (value: string) => PASSWORD_SECURITY_PATTERN.test(value)

  useEffect(() => {
    // Permite completar registros iniciados por OAuth.
    const params = new URLSearchParams(window.location.search)
    const uid = params.get('uid')
    const hadToRegister = params.get('hadToRegister')
    const jwtAccessToken = params.get('jwtAccessToken') ?? params.get('accessToken')
    const jwtRefreshToken = params.get('jwtRefreshToken') ?? params.get('refreshToken')

    if (!jwtAccessToken || !jwtRefreshToken || hadToRegister === null) {
      return
    }

    void handleGoogleCallback(
      Number(uid ?? 0),
      hadToRegister === 'true',
      jwtAccessToken,
      jwtRefreshToken
    )
      .then(() => navigate('/home', { replace: true }))
      .catch(() => setError('No se pudo completar el registro con Google.'))
  }, [navigate])

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    // Primera fase: crea la cuenta y solicita el codigo de confirmacion.
    if(!codeSent) {
      if (!email.trim() || !password.trim() || !passwordVer.trim()) {
        setError('Complete email and password to create you account.')
        return
      } else {
        if (password !== passwordVer) {
          setError('Passwords do not match.')
          return
        } else if (!isPasswordSecure(password)) {
          setError('Password must have at least 12 characters, including uppercase, lowercase and a number.')
          return
        } else {
          await createUser({name: name, email: email, passwd: password, lang: 'ENG'}).then( () => {
              setCodeSent(true);
            }
          ).catch(()=> {
            setError('Error creating your account, try again later.')
          })
        }
      }
    } else {
      // Segunda fase: confirma el codigo recibido por email.
      if (confirmation.trim() === '') {
        setError('Tienes que introducir el codigo de confirmacion para continuar.')
        return
      } 
      else
      {
        await confirmRegister({email: email, code: parseInt(confirmation)}).then( () => {
            navigate('/login');
          }).catch(() => {
            setError('Error on cofriming your email, try again later.')
          })
      }
    }
  }

  return (
    <section
      className="relative grid min-h-screen place-items-center overflow-hidden px-6 max-[480px]:px-4"
    >
      <div className="pointer-events-none absolute inset-0" />

      {/* Tarjeta principal de registro */}
      <article
        aria-labelledby="login-title"
        className="relative z-10 w-full max-w-105 -translate-y-[3vh] rounded-[22px] border border-white/15 bg-purple-950/20 px-8 py-10 text-(--astrais-text) shadow-[0_30px_60px_color-mix(in_srgb,var(--astrais-background)_74%,transparent)] backdrop-blur-sm max-[480px]:-translate-y-[1.5vh] max-[480px]:p-5.5"
      >
        <header className="mb-5.5">
          <p className="m-0 text-xs tracking-[0.2em] text-(--astrais-rarity-epic) uppercase">Astrais</p>
          <h1 id="login-title" className="my-2 text-[clamp(1.9rem,4vw,2.35rem)] leading-[1.1]">Sign Up</h1>
          <p className="m-0 text-[color-mix(in_srgb,var(--astrais-text)_90%,transparent)]">Welcome! Let's start your adventure.</p>
        </header>

        <form className="grid gap-4" onSubmit={onSubmit} noValidate>
          {/* Datos de nueva cuenta */}
          <div className="grid gap-2">
            <label htmlFor="name" className="text-[0.95rem]">Name</label>
            <input
              id="name"
              type="text"
              placeholder="Name"
              value={name}
              onChange={(event) => setName(event.target.value)}
              autoComplete="username"
              required
              className="rounded-xl border border-white/25 bg-black/25 px-3.5 py-3 text-base text-white placeholder:text-white/65 focus-visible:outline-2 focus-visible:outline-(--astrais-rarity-epic) focus-visible:outline-offset-1"
            />
          </div>

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
              className="rounded-xl border border-white/25 bg-black/25 px-3.5 py-3 text-base text-white placeholder:text-white/65 focus-visible:outline-2 focus-visible:outline-(--astrais-rarity-epic) focus-visible:outline-offset-1"
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
              minLength={12}
              pattern={`${PASSWORD_SECURITY_PATTERN}`}
              title="Minimum 12 characters, with uppercase, lowercase and a number."
              required
              className="rounded-xl border border-white/25 bg-black/25 px-3.5 py-3 text-base text-white placeholder:text-white/65 focus-visible:outline-2 focus-visible:outline-(--astrais-rarity-epic) focus-visible:outline-offset-1"
            />
          </div>

          <div className="grid gap-2">
            <label htmlFor="passwordVer" className="text-[0.95rem]">Confirm your password</label>
            <input
              id="passwordVer"
              type="password"
              placeholder="••••••••"
              value={passwordVer}
              onChange={(event) => setPasswordVer(event.target.value)}
              autoComplete="current-password"
              minLength={12}
              pattern={`${PASSWORD_SECURITY_PATTERN}`}
              title="Minimum 1 characters, with uppercase, lowercase and a number."
              required
              className="rounded-xl border border-white/25 bg-black/25 px-3.5 py-3 text-base text-white placeholder:text-white/65 focus-visible:outline-2 focus-visible:outline-(--astrais-rarity-epic) focus-visible:outline-offset-1"
            />
          </div>

          {/* Codigo de confirmacion mostrado tras crear la cuenta */}
          <div className={`grid gap-2 ${!codeSent && 'hidden'}`}>
            <label htmlFor="passwordVer" className="text-[0.95rem]">Introduce your confirmation code</label>
            <input
              id="text"
              type="text"
              placeholder="123456"
              value={confirmation}
              onChange={(event) => setConfirmation(event.target.value)}
              autoComplete="current-password"
              required
              className="rounded-xl border border-white/25 bg-black/25 px-3.5 py-3 text-base text-white placeholder:text-white/65 focus-visible:outline-2 focus-visible:outline-(--astrais-rarity-epic) focus-visible:outline-offset-1"
            />
          </div>

          {error ? (
            <p className="m-0 rounded-[10px] border border-[color-mix(in_srgb,var(--astrais-error)_55%,transparent)] bg-[color-mix(in_srgb,var(--astrais-error)_20%,transparent)] p-2.5 text-[0.9rem]" role="status" aria-live="polite">
              {error}
            </p>
          ) : null}

          {/* Acciones de registro y OAuth */}
          <button
            className="cursor-pointer rounded-xl border-0 bg-linear-to-r from-purple-800/80 via-pink-500/80 to-purple-600/80 p-3 text-base font-semibold text-white shadow-[0_10px_30px_color-mix(in_srgb,var(--astrais-primary)_45%,transparent)] transition duration-150 ease-in hover:-translate-y-px"
            type="submit"
          >
            Sign Up
          </button>

          <button
            className="cursor-pointer rounded-xl border border-white/20 bg-white/10 p-3 text-base font-semibold text-white transition duration-150 ease-in hover:-translate-y-px"
            type="button"
            onClick={loginWithGoogle}
          >
            Continue with Google
          </button>

          <p className="m-0 text-center text-[0.95rem] text-[color-mix(in_srgb,var(--astrais-text)_92%,transparent)]">
            You already have an account? <Link to="/login" className="text-white no-underline hover:underline">Log In</Link>
          </p>
        </form>
      </article>
    </section>
  )
}

