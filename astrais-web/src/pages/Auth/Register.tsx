import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router'
import '../../styles/Login.css'

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const onSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!email.trim() || !password.trim()) {
      setError('Complete email and password to create you account.')
      return
    }

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