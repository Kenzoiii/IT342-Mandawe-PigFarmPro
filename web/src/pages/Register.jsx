import React, { useState } from 'react'
import { register } from '../api'

export default function Register() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [fullName, setFullName] = useState('')
  const [message, setMessage] = useState('')
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setMessage('')
    setErrors({})

    if (password !== confirmPassword) {
      setMessage('Passwords do not match')
      setErrors({ password: 'Please use the same password in both fields.' })
      return
    }

    const username = (email.split('@')[0] || fullName || 'farmer')
      .toLowerCase()
      .replace(/[^a-z0-9._-]/g, '')
      .slice(0, 32)

    setLoading(true)

    try {
      const res = await register({ username, email, password, fullName })
      if (res.success) {
        setMessage('Registration successful! Redirecting to sign in...')
        setTimeout(() => {
          window.location.hash = 'login'
        }, 1500)
      } else {
        if (res.error && res.error.details) {
          setErrors(res.error.details)
        }
        setMessage(res.message || 'Registration failed')
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
        <h1>Create Account</h1>
        <p>Start managing your farm today</p>
      </header>

      <form onSubmit={submit} className="auth-card">
        <div className="field-wrap">
          <label htmlFor="register-fullname">Full Name</label>
          <input
            id="register-fullname"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            placeholder="John Farmer"
          />
        </div>

        <div className="field-wrap">
          <label htmlFor="register-email">Email</label>
          <input
            id="register-email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            type="email"
            placeholder="john@pigfarm.com"
            required
            aria-invalid={Boolean(errors.email)}
          />
          {errors.email && <small className="field-error">{errors.email}</small>}
        </div>

        <div className="field-wrap">
          <label htmlFor="register-password">Password</label>
          <input
            id="register-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            type="password"
            placeholder="••••••••"
            required
            minLength={8}
            aria-invalid={Boolean(errors.password)}
          />
          {errors.password && <small className="field-error">{errors.password}</small>}
        </div>

        <div className="field-wrap">
          <label htmlFor="register-confirm-password">Confirm Password</label>
          <input
            id="register-confirm-password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            type="password"
            placeholder="••••••••"
            required
          />
        </div>

        <button type="submit" className="primary-btn" disabled={loading}>
          {loading ? 'Creating Account...' : 'Create Account'}
        </button>

        {message && (
          <p className={`form-message ${message.toLowerCase().includes('failed') || message.toLowerCase().includes('match') ? 'error' : 'ok'}`}>
            {message}
          </p>
        )}

        <p className="switch-auth-text">
          Already have an account?{' '}
          <a href="#login" className="switch-auth-link">Sign in here</a>
        </p>
      </form>
    </section>
  )
}
