import React, { useEffect, useMemo, useState } from 'react'
import { createPen, getDashboard } from '../api'

export default function Pens({ token, onLogout }) {
  const [dashboard, setDashboard] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [createOpen, setCreateOpen] = useState(false)
  const [createMessage, setCreateMessage] = useState('')
  const [form, setForm] = useState({
    identifier: '',
    name: '',
    description: '',
    capacity: ''
  })

  useEffect(() => {
    if (!token) {
      setError('Not logged in')
      setLoading(false)
      return
    }

    setLoading(true)
    getDashboard(token)
      .then((res) => {
        if (res.success && res.data) {
          setDashboard(res.data)
          setError('')
        } else {
          setError(res.message || 'Unable to load pens')
        }
      })
      .catch(() => setError('Unauthorized or pens unavailable'))
      .finally(() => setLoading(false))
  }, [token])

  const pens = useMemo(() => dashboard?.pens || [], [dashboard?.pens])

  const menuItems = [
    { label: 'Dashboard', href: '#dashboard', icon: '▦', enabled: true },
    { label: 'Pens', href: '#pens', icon: '▣', active: true, enabled: true },
    { label: 'Feeding', icon: 'ϟ', enabled: false },
    { label: 'Health Records', icon: '♡', enabled: false },
    { label: 'Sales', icon: '$', enabled: false },
    { label: 'Mortality', icon: '△', enabled: false }
  ]

  const openCreate = () => {
    setCreateMessage('')
    setCreateOpen(true)
  }

  const closeCreate = () => {
    setCreateOpen(false)
  }

  const handleCreateSubmit = (event) => {
    event.preventDefault()

    const trimmedName = form.name.trim()
    const capacityValue = Number(form.capacity)

    if (!trimmedName) {
      setCreateMessage('Pen name is required.')
      return
    }

    if (!Number.isFinite(capacityValue) || capacityValue <= 0) {
      setCreateMessage('Capacity must be greater than zero.')
      return
    }

    setLoading(true)
    setError('')
    createPen(token, {
      penIdentifier: form.identifier.trim() || null,
      penName: trimmedName,
      description: form.description.trim() || null,
      capacity: capacityValue
    })
      .then((res) => {
        if (!res.success) {
          setCreateMessage(res.message || 'Unable to create pen')
          return null
        }

        setCreateMessage('Pen saved to the database.')
        setForm({ identifier: '', name: '', description: '', capacity: '' })
        setCreateOpen(false)
        return getDashboard(token)
      })
      .then((freshDashboard) => {
        if (freshDashboard && freshDashboard.success && freshDashboard.data) {
          setDashboard(freshDashboard.data)
        }
      })
      .catch((err) => {
        setCreateMessage(err?.message === 'Failed to load dashboard'
          ? 'Saved, but failed to refresh the list.'
          : (err?.message || 'Failed to save pen to the server. Check the API URL and backend availability.'))
      })
      .finally(() => setLoading(false))
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
          {menuItems.map((item) => item.enabled ? (
            <a key={item.label} href={item.href} className={`menu-item ${item.active ? 'active' : ''}`}>
              <span>{item.icon}</span>{item.label}
            </a>
          ) : (
            <button key={item.label} type="button" className="menu-item disabled" disabled>
              <span>{item.icon}</span>{item.label}
            </button>
          ))}
        </nav>

        <div className="sidebar-footer">
          <button type="button" className="menu-item"><span>⚙</span>Settings</button>
          <button type="button" className="menu-item" onClick={onLogout}><span>↪</span>Logout</button>
        </div>
      </aside>

      <main className="dashboard-main">
        <div className="dashboard-topbar">
          <h2>Pens Management</h2>
          <div className="topbar-right">
            <button type="button" className="add-pen-btn" onClick={openCreate}>
              <span className="add-pen-icon">＋</span>
              Add New Pen
            </button>
          </div>
        </div>

        <section className="dashboard-content">
          {loading && <p className="dashboard-muted">Loading pens...</p>}
          {error && <p className="form-message error dashboard-error">{error}</p>}

          {!loading && pens.length === 0 && !error ? (
            <div className="pens-empty-shell">
              <div className="pens-empty-card">
                <div className="empty-illustration green" aria-hidden="true">
                  <span />
                  <span />
                  <span />
                  <strong>▣</strong>
                </div>
                <h3>No Pens yet, wanna create one?</h3>
                <p>Add your first pen to start tracking capacity, occupancy, and growth in a way that actually updates the dashboard.</p>
                <button type="button" className="section-action-btn green" onClick={openCreate}>Create a Pen</button>
              </div>
            </div>
          ) : (
            <section className="pens-grid">
              {pens.map((pen) => {
                const capacity = Number(pen.capacity || 0)
                const occupied = Number(pen.occupied || 0)
                const utilization = capacity > 0 ? Math.min((occupied / capacity) * 100, 100) : 0
                const isFull = utilization >= 100
                const codeLabel = pen.identifier || (pen.id ? `PEN-${pen.id}` : 'PEN')
                const nameLabel = pen.name || pen.description || 'Section'

                return (
                  <article className="pen-card" key={pen.id}>
                    <div className="pen-card-head">
                      <div>
                        <h3 className="pen-code">{codeLabel}</h3>
                        <p className="pen-subtitle">{nameLabel}</p>
                      </div>
                    </div>

                    <div className="pen-capacity-row">
                      <span>Capacity</span>
                      <strong>{occupied} / {capacity}</strong>
                    </div>

                    <div className="pen-progress-track" aria-label={`Pen utilization ${utilization.toFixed(0)} percent`}>
                      <div className={`pen-progress-fill ${isFull ? 'danger' : 'safe'}`} style={{ width: `${utilization}%` }} />
                    </div>

                    <button type="button" className="pen-details-btn" onClick={() => { window.location.hash = `pens/${pen.id}` }}>
                      <span className="pen-details-icon">👁</span>
                      View Details
                    </button>
                  </article>
                )
              })}
            </section>
          )}

          {createMessage && <p className="dashboard-muted">{createMessage}</p>}
        </section>

        {createOpen && (
          <div className="modal-backdrop" role="presentation" onClick={closeCreate}>
            <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="create-pen-title" onClick={(event) => event.stopPropagation()}>
              <div className="modal-header">
                <div>
                  <p className="modal-kicker">New Pen</p>
                  <h3 id="create-pen-title">Create a pen</h3>
                </div>
                <button type="button" className="modal-close-btn" onClick={closeCreate} aria-label="Close create pen dialog">×</button>
              </div>

              <form className="modal-form" onSubmit={handleCreateSubmit}>
                <label>
                  Pen Identifier
                  <input
                    value={form.identifier}
                    onChange={(event) => setForm((current) => ({ ...current, identifier: event.target.value }))}
                    placeholder="PEN-G"
                  />
                </label>

                <label>
                  Pen Name
                  <input
                    value={form.name}
                    onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
                    placeholder="Grow-out Pen"
                    required
                  />
                </label>

                <label>
                  Capacity
                  <input
                    type="number"
                    min="1"
                    value={form.capacity}
                    onChange={(event) => setForm((current) => ({ ...current, capacity: event.target.value }))}
                    placeholder="40"
                    required
                  />
                </label>

                <label>
                  Description
                  <textarea
                    rows="3"
                    value={form.description}
                    onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
                    placeholder="Short description of this pen..."
                  />
                </label>

                <div className="modal-actions">
                  <button type="button" className="modal-secondary-btn" onClick={closeCreate}>Cancel</button>
                  <button type="submit" className="section-action-btn green">Create Pen</button>
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </section>
  )
}
