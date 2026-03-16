import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router'
import '../../styles/Login.css'
/*
import axios from 'axios'
import API from '../../data/Api.ts'


*/
export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [rememberMe, setRememberMe] = useState(false)
  const [error, setError] = useState('')

  const onSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!email.trim() || !password.trim()) {
      setError('Complete user/email and password to continue.')
      return
    }

    setError('')
    console.log({ email, password, rememberMe })
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
            <Link to="/forgot-password" className="link-button">Forgot you password?</Link>
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