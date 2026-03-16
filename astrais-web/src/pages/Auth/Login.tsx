import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router'
import '../../styles/Login.css'
import { performLogin } from '../../data/Api.ts'
/*
import axios from 'axios'
import API from '../../data/Api.ts'
*/
export default function Login() {
  const [email, setEmail] = useState('')
  const [passwd, setPasswd] = useState('')
  const [rememberMe, setRememberMe] = useState(false)
  const [error, setError] = useState('')

  const onSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!email.trim() || !passwd.trim()) {
      setError('Complete user/email and passwd to continue.')
      return
    }

    /*
    Funcion de registro, se llama a la API con los datos del formulario y se maneja la respuesta
    */

    performLogin({ email, passwd }).then(success => {
        if (!success) {
            setError('Invalid email or passwd. Please try again.')
        } else {
            setError('')
        } 
      })

    setError('')
    console.log({ email, passwd, rememberMe })
  }

  return (
    <section className="login-screen">
      <div className="login-screen__overlay" />

      <article className="login-card" aria-labelledby="login-title">
        <header className="login-card__header">
          <p className="login-card__brand">Astrais</p>
          <h1 id="login-title">Log in</h1>
          <p>Welcome back let's continue your adventure.</p>
        </header>

        <form className="login-form" onSubmit={onSubmit} noValidate>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="text"
              placeholder="user@mail.com"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="username"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="passwd">Password</label>
            <input
              id="passwd"
              type="password"
              placeholder="••••••••"
              value={passwd}
              onChange={(event) => setPasswd(event.target.value)}
              autoComplete="current-passwd"
              required
            />
          </div>

          <div className="login-row">
            <label className="remember-me" htmlFor="rememberMe">
              <input
                id="rememberMe"
                type="checkbox"
                checked={rememberMe}
                onChange={(event) => setRememberMe(event.target.checked)}
              />
              Remember me
            </label>
            <Link to="/forgot-passwd" className="link-button">Forgot you passwd?</Link>
          </div>

          {error ? (
            <p className="form-error" role="status" aria-live="polite">
              {error}
            </p>
          ) : null}

          <button className="button button--primary" type="submit">
            <Link to="/home" className="link-button">Log in</Link>
          </button>

          <button className="button button--ghost" type="button">
            Continue with Google
          </button>

          <p className="signup-callout">
            You don't have an account? <Link to="/register">Sign up</Link>
          </p>
        </form>
      </article>
    </section>
  )
}