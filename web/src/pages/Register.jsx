import React, { useState } from 'react'
import { register } from '../api'

export default function Register() {
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [fullName, setFullName] = useState('')
  const [message, setMessage] = useState('')
  const [errors, setErrors] = useState({})

  const submit = async (e) => {
    e.preventDefault()
    setMessage('')
    setErrors({})
    try {
      const res = await register({ username, email, password, fullName })
      if (res.success) {
        setMessage('Registration successful! Redirecting to login...')
        setTimeout(() => {
          window.location.href = '/login'
        }, 1500)
      } else {
        if (res.error && res.error.details) {
          setErrors(res.error.details)
        }
        setMessage(res.message || 'Registration failed')
      }
    } catch (err) {
      setMessage('Network error')
    }
  }

  return (
    <form onSubmit={submit} style={{ maxWidth: '400px', margin: '50px auto' }}>
      <h2>Register</h2>
      <div style={{ marginBottom: '15px' }}>
        <label>Full Name (Optional)</label>
        <input 
          value={fullName} 
          onChange={e => setFullName(e.target.value)} 
          style={{ width: '100%', padding: '8px' }}
        />
      </div>
      <div style={{ marginBottom: '15px' }}>
        <label>Username *</label>
        <input 
          value={username} 
          onChange={e => setUsername(e.target.value)} 
          required 
          style={{ width: '100%', padding: '8px', borderColor: errors.username ? 'red' : 'default' }}
        />
        {errors.username && <small style={{ color: 'red' }}>{errors.username}</small>}
      </div>
      <div style={{ marginBottom: '15px' }}>
        <label>Email *</label>
        <input 
          value={email} 
          onChange={e => setEmail(e.target.value)} 
          type="email" 
          required 
          style={{ width: '100%', padding: '8px', borderColor: errors.email ? 'red' : 'default' }}
        />
        {errors.email && <small style={{ color: 'red' }}>{errors.email}</small>}
      </div>
      <div style={{ marginBottom: '15px' }}>
        <label>Password (Min 8 chars) *</label>
        <input 
          value={password} 
          onChange={e => setPassword(e.target.value)} 
          type="password" 
          required 
          style={{ width: '100%', padding: '8px', borderColor: errors.password ? 'red' : 'default' }}
        />
        {errors.password && <small style={{ color: 'red' }}>{errors.password}</small>}
      </div>
      <button type="submit" style={{ width: '100%', padding: '10px' }}>Register</button>
      {message && <p style={{ color: message.includes('failed') ? 'crimson' : 'teal', marginTop: '15px' }}>{message}</p>}
    </form>
  )
}
