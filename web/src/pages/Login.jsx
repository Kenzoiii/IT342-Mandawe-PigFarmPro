import React, { useState } from 'react'
import { login } from '../api'

export default function Login({ onLogin }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')

  const submit = async (e) => {
    e.preventDefault()
    setMessage('')
    try {
      const res = await login({ email, password })
      if (res.success && res.data && res.data.token) {
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(res.data))
        onLogin(res.data.token)
      } else {
        setMessage(res.message || 'Login failed')
      }
    } catch (err) {
      setMessage('Network error')
    }
  }

  return (
    <form onSubmit={submit} style={{ maxWidth: '400px', margin: '50px auto' }}>
      <h2>Login</h2>
      <div style={{ marginBottom: '15px' }}>
        <label>Email</label>
        <input 
          value={email} 
          onChange={e => setEmail(e.target.value)} 
          type="email" 
          required 
          style={{ width: '100%', padding: '8px' }}
        />
      </div>
      <div style={{ marginBottom: '15px' }}>
        <label>Password</label>
        <input 
          value={password} 
          onChange={e => setPassword(e.target.value)} 
          type="password" 
          required 
          style={{ width: '100%', padding: '8px' }}
        />
      </div>
      <button type="submit" style={{ width: '100%', padding: '10px' }}>Login</button>
      {message && <p style={{ color: message.includes('failed') ? 'crimson' : 'teal', marginTop: '15px' }}>{message}</p>}
      <p style={{ textAlign: 'center', marginTop: '15px' }}>
        Don't have an account? <a href="/register">Register here</a>
      </p>
    </form>
  )
}
