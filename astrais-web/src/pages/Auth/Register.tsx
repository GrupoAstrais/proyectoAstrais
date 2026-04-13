import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import loginBg from '../../assets/login-bg.jpg'
import { confirmRegister, createUser } from '../../data/Api'

export default function Register() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [name, setName] = useState('')
  const [password, setPassword] = useState('')
  const [passwordVer, setPasswordVer] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [codeSent, setCodeSent] = useState(false)
  const [error, setError] = useState('')

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if(!codeSent) {
      if (!email.trim() || !password.trim() || !passwordVer.trim()) {
        setError('Complete email and password to create you account.')
        return
      } else {
        if (password !== passwordVer) {
          setError('Passwords do not match.')
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
      style={{ backgroundImage: `url(${loginBg})` }}
      className="relative grid min-h-screen place-items-center overflow-hidden bg-cover bg-no-repeat px-6 bg-position-[0_-20cm] max-[480px]:px-4"
    >
      <div className="pointer-events-none absolute inset-0" />

      <article
        aria-labelledby="login-title"
        className="relative z-10 w-full max-w-105 -translate-y-[3vh] rounded-[22px] border border-white/15 bg-[rgba(16,5,33,0.72)] p-8 text-[#f6e8ff] shadow-[0_30px_60px_rgba(9,2,20,0.7)] backdrop-blur-sm max-[480px]:-translate-y-[1.5vh] max-[480px]:p-5.5"
      >
        <header className="mb-5.5">
          <p className="m-0 text-xs tracking-[0.2em] text-[#f5a6ff] uppercase">Astrais</p>
          <h1 id="login-title" className="my-2 text-[clamp(1.9rem,4vw,2.35rem)] leading-[1.1]">Sign Up</h1>
          <p className="m-0 text-[rgba(246,232,255,0.9)]">Welcome! Let's start your adventure.</p>
        </header>

        <form className="grid gap-4" onSubmit={onSubmit} noValidate>
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
              className="rounded-xl border border-white/25 bg-black/25 px-3.5 py-3 text-base text-white placeholder:text-white/65 focus-visible:outline-2 focus-visible:outline-[#ff66dd] focus-visible:outline-offset-1"
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
              className="rounded-xl border border-white/25 bg-black/25 px-3.5 py-3 text-base text-white placeholder:text-white/65 focus-visible:outline-2 focus-visible:outline-[#ff66dd] focus-visible:outline-offset-1"
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
              className="rounded-xl border border-white/25 bg-black/25 px-3.5 py-3 text-base text-white placeholder:text-white/65 focus-visible:outline-2 focus-visible:outline-[#ff66dd] focus-visible:outline-offset-1"
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
              required
              className="rounded-xl border border-white/25 bg-black/25 px-3.5 py-3 text-base text-white placeholder:text-white/65 focus-visible:outline-2 focus-visible:outline-[#ff66dd] focus-visible:outline-offset-1"
            />
          </div>

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
              className="rounded-xl border border-white/25 bg-black/25 px-3.5 py-3 text-base text-white placeholder:text-white/65 focus-visible:outline-2 focus-visible:outline-[#ff66dd] focus-visible:outline-offset-1"
            />
          </div>

          {error ? (
            <p className="m-0 rounded-[10px] border border-[rgba(255,132,163,0.55)] bg-[rgba(255,72,119,0.2)] p-2.5 text-[0.9rem]" role="status" aria-live="polite">
              {error}
            </p>
          ) : null}

          <button
            className="cursor-pointer rounded-xl border border-transparent bg-linear-to-r from-[#ff3dcd] to-[#8b49ff] p-3 text-base font-semibold text-white shadow-[0_10px_30px_rgba(141,73,255,0.45)] transition duration-150 ease-in hover:-translate-y-px"
            type="submit"
          >
            Sign Up
          </button>

          <p className="m-0 text-center text-[0.95rem] text-[rgba(246,232,255,0.92)]">
            You already have an account? <Link to="/login" className="text-white no-underline hover:underline">Log In</Link>
          </p>
        </form>
      </article>
    </section>
  )
}