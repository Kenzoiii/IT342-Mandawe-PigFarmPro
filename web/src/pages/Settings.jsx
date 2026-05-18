import React, { useEffect, useState } from 'react'
import { getMe, updatePassword, updateProfile } from '../api'

const MENU_ITEMS = [
  { label: 'Dashboard', href: '#dashboard', icon: '▦', enabled: true },
  { label: 'Pens', href: '#pens', icon: '▣', enabled: true },
  { label: 'Feeding', href: '#feeding', icon: 'ϟ', enabled: true },
  { label: 'Health Records', href: '#health-records', icon: '♡', enabled: true },
  { label: 'Sales', href: '#sales', icon: '$', enabled: true },
  { label: 'Mortality', href: '#mortality', icon: '△', enabled: true }
]

export default function Settings({ token, onLogout }) {
  const [profile, setProfile] = useState(null)
  const [profileForm, setProfileForm] = useState({
    fullName: '',
    username: '',
    email: '',
    role: ''
  })
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [profileMessage, setProfileMessage] = useState('')
  const [passwordMessage, setPasswordMessage] = useState('')
  const [profileSaving, setProfileSaving] = useState(false)
  const [passwordSaving, setPasswordSaving] = useState(false)

  const loadProfile = () => {
    if (!token) return Promise.resolve()
    return getMe(token)
      .then((res) => {
        if (res.success && res.data) {
          setProfile(res.data)
          setProfileForm({
            fullName: res.data.fullName || '',
            username: res.data.username || '',
            email: res.data.email || '',
            role: res.data.role || ''
          })
          setError('')
        } else {
          setError(res.message || 'Unable to load profile')
        }
      })
      .catch(() => setError('Unable to load profile'))
  }

  useEffect(() => {
    if (!token) {
      setError('Not logged in')
      setLoading(false)
      return
    }

    setLoading(true)
    loadProfile()
      .finally(() => setLoading(false))
  }, [token])

  const handleProfileSubmit = (event) => {
    event.preventDefault()

    const username = profileForm.username.trim()
    const email = profileForm.email.trim()

    if (!username) {
      setProfileMessage('Username is required.')
      return
    }

    if (!email) {
      setProfileMessage('Email is required.')
      return
    }

    if (!email.includes('@')) {
      setProfileMessage('Email must be valid.')
      return
    }

    setProfileSaving(true)
    setProfileMessage('')

    updateProfile(token, {
      fullName: profileForm.fullName.trim() || null,
      username,
      email
    })
      .then((res) => {
        if (!res.success) {
          setProfileMessage(res.message || 'Unable to save profile')
          return
        }
        if (res.data) {
          setProfile(res.data)
          setProfileForm({
            fullName: res.data.fullName || '',
            username: res.data.username || '',
            email: res.data.email || '',
            role: res.data.role || ''
          })
        }
        const stored = localStorage.getItem('user')
        if (stored) {
          try {
            const parsed = JSON.parse(stored)
            const updated = {
              ...parsed,
              username,
              email,
              fullName: profileForm.fullName.trim() || null
            }
            localStorage.setItem('user', JSON.stringify(updated))
          } catch {
            // ignore parsing errors
          }
        }
        setProfileMessage('Profile updated.')
      })
      .catch(() => setProfileMessage('Failed to update profile.'))
      .finally(() => setProfileSaving(false))
  }

  const handlePasswordSubmit = (event) => {
    event.preventDefault()

    if (!passwordForm.currentPassword) {
      setPasswordMessage('Current password is required.')
      return
    }

    if (!passwordForm.newPassword) {
      setPasswordMessage('New password is required.')
      return
    }

    if (passwordForm.newPassword.length < 8) {
      setPasswordMessage('New password must be at least 8 characters.')
      return
    }

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordMessage('Passwords do not match.')
      return
    }

    setPasswordSaving(true)
    setPasswordMessage('')

    updatePassword(token, {
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
      .then((res) => {
        if (!res.success) {
          setPasswordMessage(res.message || 'Unable to update password')
          return
        }
        setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
        setPasswordMessage('Password updated.')
      })
      .catch(() => setPasswordMessage('Failed to update password.'))
      .finally(() => setPasswordSaving(false))
  }

  if (!token) {
    return (
      <section className="dashboard-page">
        <main className="dashboard-main">
          <div className="dashboard-topbar">
            <h2>Settings</h2>
          </div>
          <section className="dashboard-content">
            <div className="dashboard-panel">
              <p className="form-message error">Not logged in.</p>
              <button type="button" className="primary-btn" onClick={onLogout}>Go to Sign In</button>
            </div>
          </section>
        </main>
      </section>
    )
  }

  return (
    <section className="dashboard-page">
      <aside className="dashboard-sidebar">
        <div className="brand-block">
          <div className="brand-icon">🐷</div>
          <div>
            <h1>PigFarmPro</h1>
            <p>Farm Management</p>
          </div>
        </div>

        <nav className="menu-list" aria-label="Dashboard sections">
          {MENU_ITEMS.map((item) => item.enabled ? (
            <a key={item.label} href={item.href} className="menu-item">
              <span>{item.icon}</span>{item.label}
            </a>
          ) : (
            <button key={item.label} type="button" className="menu-item disabled" disabled>
              <span>{item.icon}</span>{item.label}
            </button>
          ))}
        </nav>

        <div className="sidebar-footer">
          <a href="#settings" className="menu-item active"><span>⚙</span>Settings</a>
          <button type="button" className="menu-item" onClick={onLogout}><span>↪</span>Logout</button>
        </div>
      </aside>

      <main className="dashboard-main">
        <div className="dashboard-topbar">
          <h2>Settings</h2>
        </div>

        <section className="dashboard-content section-page-content">
          {loading && <p className="dashboard-muted">Loading settings...</p>}
          {error && <p className="form-message error dashboard-error">{error}</p>}

          <section className="settings-grid">
            <article className="dashboard-panel settings-card">
              <div className="settings-card-head">
                <div>
                  <h3>Profile</h3>
                  <p>Update your account details.</p>
                </div>
                {profile?.createdAt && (
                  <span className="settings-tag">Joined {new Date(profile.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span>
                )}
              </div>

              <form className="settings-form" onSubmit={handleProfileSubmit}>
                <div className="settings-row">
                  <label>
                    Full Name
                    <input
                      value={profileForm.fullName}
                      onChange={(event) => setProfileForm((current) => ({ ...current, fullName: event.target.value }))}
                      placeholder="John Farmer"
                    />
                  </label>
                  <label>
                    Username
                    <input
                      value={profileForm.username}
                      onChange={(event) => setProfileForm((current) => ({ ...current, username: event.target.value }))}
                      placeholder="johnfarmer"
                      required
                    />
                  </label>
                </div>

                <div className="settings-row">
                  <label>
                    Email
                    <input
                      type="email"
                      value={profileForm.email}
                      onChange={(event) => setProfileForm((current) => ({ ...current, email: event.target.value }))}
                      placeholder="john@pigfarm.com"
                      required
                    />
                  </label>
                  <label>
                    Role
                    <input value={profileForm.role || 'USER'} disabled />
                  </label>
                </div>

                {profileMessage && (
                  <p className={`form-message ${profileMessage.toLowerCase().includes('updated') ? 'ok' : 'error'}`}>
                    {profileMessage}
                  </p>
                )}

                <div className="settings-actions">
                  <button type="submit" className="section-action-btn green" disabled={profileSaving}>
                    {profileSaving ? 'Saving...' : 'Save Changes'}
                  </button>
                </div>
              </form>
            </article>

            <article className="dashboard-panel settings-card">
              <div className="settings-card-head">
                <div>
                  <h3>Security</h3>
                  <p>Update your password to keep your account secure.</p>
                </div>
                <span className="settings-tag">At least 8 characters</span>
              </div>

              <form className="settings-form" onSubmit={handlePasswordSubmit}>
                <div className="settings-row">
                  <label>
                    Current Password
                    <input
                      type="password"
                      value={passwordForm.currentPassword}
                      onChange={(event) => setPasswordForm((current) => ({ ...current, currentPassword: event.target.value }))}
                      placeholder="********"
                      required
                    />
                  </label>
                </div>

                <div className="settings-row">
                  <label>
                    New Password
                    <input
                      type="password"
                      value={passwordForm.newPassword}
                      onChange={(event) => setPasswordForm((current) => ({ ...current, newPassword: event.target.value }))}
                      placeholder="********"
                      required
                      minLength={8}
                    />
                  </label>
                  <label>
                    Confirm Password
                    <input
                      type="password"
                      value={passwordForm.confirmPassword}
                      onChange={(event) => setPasswordForm((current) => ({ ...current, confirmPassword: event.target.value }))}
                      placeholder="********"
                      required
                      minLength={8}
                    />
                  </label>
                </div>

                {passwordMessage && (
                  <p className={`form-message ${passwordMessage.toLowerCase().includes('updated') ? 'ok' : 'error'}`}>
                    {passwordMessage}
                  </p>
                )}

                <div className="settings-actions">
                  <button type="submit" className="section-action-btn neutral" disabled={passwordSaving}>
                    {passwordSaving ? 'Saving...' : 'Update Password'}
                  </button>
                </div>
              </form>
            </article>
          </section>
        </section>
      </main>
    </section>
  )
}
