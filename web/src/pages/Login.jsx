import React, { useState } from 'react'
import { login } from '../api'

export default function Login({ onLogin }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setMessage('')
    setLoading(true)

    try {
      const res = await login({ email, password })
      if (res.success && res.data && res.data.token) {
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(res.data))
        onLogin(res.data.token)
      } else {
        setMessage(res.message || 'Login failed')
      }
    } catch (_err) {
      setMessage('Network error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="auth-page">
      <header className="auth-header">
        <div className="logo-chip" aria-hidden="true">
          <span className="logo-glyph">🐷</span>
        </div>
        <h1>Welcome to PigFarmPro</h1>
        <p>Sign in to manage your farm</p>
      </header>

      <form onSubmit={submit} className="auth-card">
        <div className="field-wrap">
          <label htmlFor="login-email">Email</label>
          <input
            id="login-email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            type="email"
            placeholder="john@pigfarm.com"
            required
          />
        </div>

        <div className="field-wrap">
          <label htmlFor="login-password">Password</label>
          <input
            id="login-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            type="password"
            placeholder="••••••••"
            required
          />
        </div>

        <button type="submit" className="primary-btn" disabled={loading}>
          {loading ? 'Signing In...' : 'Sign In'}
        </button>

        {message && (
          <p className={`form-message ${message.toLowerCase().includes('failed') ? 'error' : 'ok'}`}>
            {message}
          </p>
        )}

        <p className="switch-auth-text">
          Don't have an account?{' '}
          <a href="#register" className="switch-auth-link">Register here</a>
        </p>
      </form>
    </section>
  )
}
