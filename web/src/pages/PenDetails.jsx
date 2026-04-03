import React, { useEffect, useState } from 'react'
import { getPenDetails, updatePen } from '../api'

const MENU_ITEMS = [
  { label: 'Dashboard', href: '#dashboard', icon: '▦', enabled: true },
  { label: 'Pens', href: '#pens', icon: '▣', active: true, enabled: true },
  { label: 'Feeding', icon: 'ϟ', enabled: false },
  { label: 'Health Records', icon: '♡', enabled: false },
  { label: 'Sales', icon: '$', enabled: false },
  { label: 'Mortality', icon: '△', enabled: false }
]

export default function PenDetails({ token, onLogout, penId }) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editOpen, setEditOpen] = useState(false)
  const [editForm, setEditForm] = useState({
    identifier: '',
    name: '',
    capacity: '',
    description: ''
  })
  const [editError, setEditError] = useState('')
  const [editSaving, setEditSaving] = useState(false)

  useEffect(() => {
    if (!token) {
      setError('Not logged in')
      setLoading(false)
      return
    }

    if (!penId) {
      setError('Pen not found')
      setLoading(false)
      return
    }

    setLoading(true)
    getPenDetails(token, penId)
      .then((res) => {
        if (res.success && res.data) {
          setData(res.data)
          setError('')
        } else {
          setError(res.message || 'Unable to load pen details')
        }
      })
      .catch(() => setError('Pen details unavailable'))
      .finally(() => setLoading(false))
  }, [token, penId])

  const pen = data?.pen || {}
  const pigs = data?.pigs || []
  const utilization = Number(pen.utilization || 0)
  const progressClass = utilization >= 100 ? 'danger' : utilization >= 80 ? 'warning' : 'safe'
  const statusClass = (value) => String(value || '').trim().toLowerCase().replace(/\s+/g, '-') || 'active'
  const toTime = (value) => {
    const time = new Date(value || 0).getTime()
    return Number.isNaN(time) ? 0 : time
  }
  const recentPigs = [...pigs]
    .sort((a, b) => toTime(b.addedAt) - toTime(a.addedAt))
    .slice(0, 3)

  const openEditModal = () => {
    setEditForm({
      identifier: pen.identifier || '',
      name: pen.name || '',
      capacity: pen.capacity ?? '',
      description: pen.description || ''
    })
    setEditError('')
    setEditOpen(true)
  }

  const closeEditModal = () => {
    if (editSaving) return
    setEditOpen(false)
  }

  const handleEditSubmit = (event) => {
    event.preventDefault()

    const trimmedName = editForm.name.trim()
    const capacityValue = Number(editForm.capacity)

    if (!trimmedName) {
      setEditError('Pen name is required.')
      return
    }

    if (!Number.isFinite(capacityValue) || capacityValue <= 0) {
      setEditError('Capacity must be greater than zero.')
      return
    }

    setEditSaving(true)
    setEditError('')

    updatePen(token, penId, {
      penIdentifier: editForm.identifier.trim() || null,
      penName: trimmedName,
      capacity: capacityValue,
      description: editForm.description.trim() || null
    })
      .then((res) => {
        if (!res.success) {
          setEditError(res.message || 'Unable to update pen')
          return
        }

        return getPenDetails(token, penId)
          .then((fresh) => {
            if (fresh.success && fresh.data) {
              setData(fresh.data)
              setError('')
            } else {
              setError(fresh.message || 'Unable to load pen details')
            }
          })
          .finally(() => setEditOpen(false))
      })
      .catch(() => setEditError('Failed to update pen.'))
      .finally(() => setEditSaving(false))
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

      <main className="dashboard-main pen-detail-main">
        <div className="dashboard-topbar pen-detail-topbar">
          <div className="pen-detail-heading">
            <button type="button" className="back-link" onClick={() => { window.location.hash = 'pens' }} aria-label="Back to pens">←</button>
            <div>
              <h2>{pen.name || 'Pen Details'}</h2>
              <p className="topbar-subtitle">{pen.identifier || 'Pen overview'}</p>
            </div>
          </div>
          <div className="topbar-right">
            <button type="button" className="pen-detail-edit-btn" onClick={openEditModal} disabled={loading || !!error}>
              <span className="pen-detail-edit-icon" aria-hidden="true">✎</span>
              Edit Pen
            </button>
          </div>
        </div>

        <section className="dashboard-content">
          {loading && <p className="dashboard-muted">Loading pen details...</p>}
          {error && <p className="form-message error dashboard-error">{error}</p>}

          {!loading && !error && (
            <>
              <section className="pen-stats-grid">
                <article className="pen-stat-card">
                  <div className="pen-stat-head">
                    <p>Capacity</p>
                    <span className="pen-stat-icon" aria-hidden="true">🐷</span>
                  </div>
                  <strong className="pen-stat-value">{Number(pen.occupied || 0)} / {Number(pen.capacity || 0)}</strong>
                  <div className="pen-progress-track pen-progress-track-large" aria-label={`Pen utilization ${utilization} percent`}>
                    <div className={`pen-progress-fill ${progressClass}`} style={{ width: `${Math.min(utilization, 100)}%` }} />
                  </div>
                </article>

                <article className="pen-stat-card">
                  <p>Available Space</p>
                  <strong className="pen-stat-value">{Number(pen.available || 0)}</strong>
                  <span className="pen-stat-caption">spots remaining</span>
                </article>
              </section>

              <section className="pen-info-card">
                <h3>Pen Information</h3>
                <div className="pen-info-grid">
                  <div>
                    <span>Pen ID</span>
                    <strong>{pen.identifier || '—'}</strong>
                  </div>
                  <div>
                    <span>Pen Name</span>
                    <strong>{pen.name || '—'}</strong>
                  </div>
                  <div>
                    <span>Total Pigs</span>
                    <strong>{Number(pen.occupied || 0)} pigs</strong>
                  </div>
                  <div>
                    <span>Maximum Capacity</span>
                    <strong>{Number(pen.capacity || 0)} pigs</strong>
                  </div>
                </div>
              </section>

              <section className="pen-info-card">
                <div className="pen-section-heading">
                  <h3>Recently Added Pigs</h3>
                  <a href={`#pens/${pen.id}/pigs`} className="section-action-btn green pen-section-link pen-view-btn">
                    <span className="pen-view-icon" aria-hidden="true">👁</span>
                    View All Pigs
                  </a>
                </div>

                {recentPigs.length === 0 ? (
                  <div className="pen-empty-inline">
                    <h4>No pigs in this pen yet</h4>
                    <p>This pen is empty right now. Add pigs to see them listed here with live weight and status.</p>
                  </div>
                ) : (
                  <div className="pig-grid">
                    {recentPigs.map((pig) => (
                      <article className="pig-card" key={pig.id}>
                        <div className="pig-card-head">
                          <div>
                            <h4>{pig.identifier || 'Pig'}</h4>
                            <p className="pig-breed">{pig.breed || 'Unknown breed'}</p>
                          </div>
                        </div>
                        <div className="pig-card-foot">
                          <p className="pig-weight">{pig.weight ? `${pig.weight} ${pig.weightUnit || 'kg'}` : 'No weight recorded'}</p>
                          <span className={`pen-status ${statusClass(pig.status)}`}>
                            {pig.status || 'Active'}
                          </span>
                        </div>
                      </article>
                    ))}
                  </div>
                )}
              </section>
            </>
          )}
        </section>

        {editOpen && (
          <div className="modal-backdrop" role="presentation" onClick={closeEditModal}>
            <div
              className="modal-card"
              role="dialog"
              aria-modal="true"
              aria-labelledby="edit-pen-title"
              onClick={(event) => event.stopPropagation()}
            >
              <div className="modal-header">
                <div>
                  <p className="modal-kicker">Pen</p>
                  <h3 id="edit-pen-title">Edit Pen</h3>
                </div>
                <button type="button" className="modal-close-btn" onClick={closeEditModal} aria-label="Close edit pen dialog">×</button>
              </div>

              <form className="modal-form" onSubmit={handleEditSubmit}>
                <label>
                  Pen Identifier
                  <input
                    value={editForm.identifier}
                    onChange={(event) => setEditForm((current) => ({ ...current, identifier: event.target.value }))}
                    placeholder="PEN-G"
                  />
                </label>

                <label>
                  Pen Name
                  <input
                    value={editForm.name}
                    onChange={(event) => setEditForm((current) => ({ ...current, name: event.target.value }))}
                    placeholder="Grow-out Pen"
                    required
                  />
                </label>

                <label>
                  Capacity
                  <input
                    type="number"
                    min="1"
                    value={editForm.capacity}
                    onChange={(event) => setEditForm((current) => ({ ...current, capacity: event.target.value }))}
                    placeholder="40"
                    required
                  />
                </label>

                <label>
                  Description
                  <textarea
                    rows="3"
                    value={editForm.description}
                    onChange={(event) => setEditForm((current) => ({ ...current, description: event.target.value }))}
                    placeholder="Short description of this pen..."
                  />
                </label>

                {editError && <p className="form-message error">{editError}</p>}

                <div className="modal-actions">
                  <button type="button" className="modal-secondary-btn" onClick={closeEditModal}>Cancel</button>
                  <button type="submit" className="section-action-btn green" disabled={editSaving}>
                    {editSaving ? 'Saving...' : 'Save Changes'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </section>
  )
}
