import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router'
import '../../styles/Login.css'
import { createUser } from '../../data/Api.ts'

export default function Login() {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [passwordVer, setPasswordVer] = useState('')
  const [error, setError] = useState('')
  const idiomaSistema = 'ENG';
  //const zonaHoraria = Intl.DateTimeFormat().resolvedOptions().timeZone;

  const onSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!email.trim() || !password.trim()) {
      setError('Complete email and password to create you account.')
      return
    } else {
      if (password !== passwordVer) {
        setError('The passwords don\'t match.')
      return
      } else {
        createUser({ name: name, email: email, passwd: password, lang: idiomaSistema }).then(success => {
        if (!success) {
            setError('Error creating account. Please try again.')
        } else {
            setError('')
        }
      })
      }
      
    }

    /*
    Funcion de registro, se llama a la API con los datos del formulario y se maneja la respuesta
    */

    setError('')
    console.log({ email, password })
  }

  return (
    <section className="login-screen">
      <div className="login-screen__overlay" />

      <article className="login-card" aria-labelledby="login-title">
        <header className="login-card__header">
          <p className="login-card__brand">Astrais</p>
          <h1 id="login-title">Sign Up</h1>
          <p>Welcome! Let's start your adventure.</p>
        </header>

        <form className="login-form" onSubmit={onSubmit} noValidate>
          <div className="form-group">
            <label htmlFor="name">Name</label>
            <input
              id="name"
              type="text"
              placeholder="Name"
              value={name}
              onChange={(event) => setName(event.target.value)}
              autoComplete="username"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="text"
              placeholder="user@mail.com"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="useremail"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="passwordVer">Confirm your password</label>
            <input
              id="passwordVer"
              type="password"
              placeholder="••••••••"
              value={passwordVer}
              onChange={(event) => setPasswordVer(event.target.value)}
              autoComplete="current-passwordVer"
              required
            />
          </div>

          {error ? (
            <p className="form-error" role="status" aria-live="polite">
              {error}
            </p>
          ) : null}

          <button className="button button--primary" type="submit">
            <Link to="/home" className="link-button">Sign Up</Link>
          </button>

          <p className="signup-callout">
            You already have an account? <Link to="/login">Log In</Link>
          </p>
        </form>
      </article>
    </section>
  )
}