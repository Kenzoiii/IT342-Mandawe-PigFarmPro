import React, { useEffect, useMemo, useState } from 'react'
import {
  deleteFeeding,
  getDashboard,
  getFeedingTransactions,
  recordFeeding,
  updateFeeding
} from '../api'

const MENU_ITEMS = [
  { label: 'Dashboard', href: '#dashboard', icon: '▦', enabled: true },
  { label: 'Pens', href: '#pens', icon: '▣', enabled: true },
  { label: 'Feeding', href: '#feeding', icon: 'ϟ', active: true, enabled: true },
  { label: 'Health Records', href: '#health-records', icon: '♡', enabled: true },
  { label: 'Sales', href: '#sales', icon: '$', enabled: true },
  { label: 'Mortality', href: '#mortality', icon: '△', enabled: true }
]

const DEFAULT_FORM = {
  penId: '',
  feedType: '',
  quantity: '',
  unit: 'kg',
  cost: '',
  feedingTime: '',
  notes: ''
}

const toDateValue = (value) => {
  if (!value) return null
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

const toDateTimeInput = (value) => {
  const date = toDateValue(value)
  if (!date) return ''
  const offset = date.getTimezoneOffset() * 60000
  return new Date(date.getTime() - offset).toISOString().slice(0, 16)
}

const formatDateTime = (value) => {
  const date = toDateValue(value)
  if (!date) return '-'
  return date.toLocaleString('en-US', { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' })
}

const formatNumber = (value) => new Intl.NumberFormat('en-US', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 1
}).format(Number(value || 0))

const formatMoney = (value) => new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  minimumFractionDigits: 0,
  maximumFractionDigits: 0
}).format(Number(value || 0))

export default function Feeding({ token, onLogout }) {
  const [transactions, setTransactions] = useState([])
  const [pens, setPens] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionMessage, setActionMessage] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [modalMode, setModalMode] = useState('create')
  const [activeFeedingId, setActiveFeedingId] = useState(null)
  const [form, setForm] = useState(DEFAULT_FORM)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [openMenuId, setOpenMenuId] = useState(null)

  const refreshFeedings = () => getFeedingTransactions(token)
    .then((res) => {
      if (res.success && res.data) {
        setTransactions(res.data)
        setError('')
      } else {
        setError(res.message || 'Unable to load feeding history')
      }
    })
    .catch(() => setError('Unable to load feeding history'))

  useEffect(() => {
    if (!token) {
      setError('Not logged in')
      setLoading(false)
      return
    }

    setLoading(true)
    setError('')
    setActionMessage('')

    Promise.all([getDashboard(token), getFeedingTransactions(token)])
      .then(([dashboardRes, feedingRes]) => {
        const messages = []

        if (dashboardRes.success && dashboardRes.data) {
          setPens(dashboardRes.data.pens || [])
        } else {
          messages.push(dashboardRes.message || 'Unable to load pens')
        }

        if (feedingRes.success && feedingRes.data) {
          setTransactions(feedingRes.data)
        } else {
          messages.push(feedingRes.message || 'Unable to load feeding history')
        }

        setError(messages[0] || '')
      })
      .catch(() => setError('Unable to load feeding data'))
      .finally(() => setLoading(false))
  }, [token])

  useEffect(() => {
    if (!openMenuId) return
    const handleClick = () => setOpenMenuId(null)
    document.addEventListener('click', handleClick)
    return () => document.removeEventListener('click', handleClick)
  }, [openMenuId])

  const openCreateModal = () => {
    const defaultPenId = pens[0]?.id ? String(pens[0].id) : ''
    setForm({ ...DEFAULT_FORM, penId: defaultPenId })
    setFormError('')
    setModalMode('create')
    setActiveFeedingId(null)
    setModalOpen(true)
  }

  const openEditModal = (feeding) => {
    setForm({
      penId: feeding.penId ? String(feeding.penId) : '',
      feedType: feeding.feedType || '',
      quantity: feeding.quantity ?? '',
      unit: feeding.unit || 'kg',
      cost: feeding.cost ?? '',
      feedingTime: toDateTimeInput(feeding.feedingTime || feeding.createdAt),
      notes: feeding.notes || ''
    })
    setFormError('')
    setModalMode('edit')
    setActiveFeedingId(feeding.id)
    setModalOpen(true)
  }

  const closeModal = () => {
    if (saving) return
    setModalOpen(false)
  }

  const handleSubmit = (event) => {
    event.preventDefault()

    const penId = Number(form.penId)
    const feedType = form.feedType.trim()
    const quantityValue = Number(form.quantity)
    const costValue = form.cost === '' ? null : Number(form.cost)

    if (!Number.isFinite(penId) || penId <= 0) {
      setFormError('Select a pen before saving.')
      return
    }

    if (!feedType) {
      setFormError('Feed type is required.')
      return
    }

    if (!Number.isFinite(quantityValue) || quantityValue <= 0) {
      setFormError('Quantity must be greater than zero.')
      return
    }

    if (costValue !== null && (!Number.isFinite(costValue) || costValue < 0)) {
      setFormError('Cost must be zero or greater.')
      return
    }

    const payload = {
      penId,
      feedType,
      quantity: quantityValue,
      unit: form.unit.trim() || 'kg',
      cost: costValue,
      feedingTime: form.feedingTime || null,
      notes: form.notes.trim() || null
    }

    setSaving(true)
    setFormError('')
    setActionMessage('')

    const request = modalMode === 'edit'
      ? updateFeeding(token, activeFeedingId, payload)
      : recordFeeding(token, payload)

    request
      .then((res) => {
        if (!res.success) {
          setFormError(res.message || 'Unable to save feeding record.')
          return
        }
        setModalOpen(false)
        setActionMessage(modalMode === 'edit' ? 'Feeding updated.' : 'Feeding recorded.')
        return refreshFeedings()
      })
      .catch(() => setFormError('Failed to save feeding record.'))
      .finally(() => setSaving(false))
  }

  const handleDelete = (feeding) => {
    const label = feeding.penName || feeding.penIdentifier || 'this feeding'
    if (!window.confirm(`Delete feeding entry for ${label}?`)) {
      return
    }

    setSaving(true)
    setActionMessage('')
    deleteFeeding(token, feeding.id)
      .then((res) => {
        if (!res.success) {
          setActionMessage(res.message || 'Unable to delete feeding record')
          return
        }
        setActionMessage('Feeding deleted.')
        return refreshFeedings()
      })
      .catch(() => setActionMessage('Failed to delete feeding record.'))
      .finally(() => setSaving(false))
  }

  const { todayFeed, weeklyTotal, feedCost, todayDelta } = useMemo(() => {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)
    const weekStart = new Date(today)
    weekStart.setDate(weekStart.getDate() - 6)

    let todayQty = 0
    let yesterdayQty = 0
    let todayCost = 0
    let weekQty = 0

    transactions.forEach((transaction) => {
      const timeValue = toDateValue(transaction.feedingTime || transaction.createdAt)
      if (!timeValue) return

      const dayValue = new Date(timeValue)
      dayValue.setHours(0, 0, 0, 0)

      const quantityValue = Number(transaction.quantity || 0)
      const costValue = Number(transaction.cost || 0)

      if (dayValue.getTime() === today.getTime()) {
        todayQty += quantityValue
        todayCost += costValue
      }

      if (dayValue.getTime() === yesterday.getTime()) {
        yesterdayQty += quantityValue
      }

      if (timeValue >= weekStart) {
        weekQty += quantityValue
      }
    })

    const delta = yesterdayQty > 0
      ? Math.round(((todayQty - yesterdayQty) / yesterdayQty) * 100)
      : null

    return {
      todayFeed: todayQty,
      weeklyTotal: weekQty,
      feedCost: todayCost,
      todayDelta: delta
    }
  }, [transactions])

  if (!token) {
    return (
      <section className="dashboard-page">
        <main className="dashboard-main">
          <div className="dashboard-topbar">
            <h2>Feeding Transactions</h2>
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

  const todayNote = todayDelta === null
    ? 'No data yesterday'
    : `${todayDelta >= 0 ? '+' : ''}${todayDelta}% from yesterday`

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
          <a href="#settings" className="menu-item"><span>⚙</span>Settings</a>
          <button type="button" className="menu-item" onClick={onLogout}><span>↪</span>Logout</button>
        </div>
      </aside>

      <main className="dashboard-main">
        <div className="dashboard-topbar">
          <h2>Feeding Transactions</h2>
          <div className="topbar-right">
            <button type="button" className="add-pen-btn" onClick={openCreateModal}>
              <span className="add-pen-icon">+</span>
              Record Feeding
            </button>
          </div>
        </div>

        <section className="dashboard-content">
          <section className="dashboard-kpi-grid feeding-kpi-grid">
            <article className="dashboard-kpi-card">
              <div className="dashboard-kpi-top">
                <p>Today's Feed</p>
                <span className="dashboard-kpi-icon green" aria-hidden="true">ϟ</span>
              </div>
              <h4>{loading ? '...' : `${formatNumber(todayFeed)} kg`}</h4>
              <small className={todayDelta !== null && todayDelta >= 0 ? 'positive' : ''}>{todayNote}</small>
            </article>
            <article className="dashboard-kpi-card">
              <div className="dashboard-kpi-top">
                <p>Weekly Total</p>
                <span className="dashboard-kpi-icon blue" aria-hidden="true">↗</span>
              </div>
              <h4>{loading ? '...' : `${formatNumber(weeklyTotal)} kg`}</h4>
              <small>{weeklyTotal > 0 ? 'On track' : 'No feedings yet'}</small>
            </article>
            <article className="dashboard-kpi-card">
              <div className="dashboard-kpi-top">
                <p>Feed Cost</p>
                <span className="dashboard-kpi-icon gold" aria-hidden="true">$</span>
              </div>
              <h4>{loading ? '...' : formatMoney(feedCost)}</h4>
              <small>Today's total</small>
            </article>
          </section>

          <div className="feeding-table-card">
            <div className="feeding-table-head">
              <h4>Feeding History</h4>
              <span className="feeding-table-meta">{transactions.length} records</span>
            </div>

            {loading && <p className="dashboard-muted">Loading feeding history...</p>}
            {error && <p className="form-message error dashboard-error">{error}</p>}

            {!loading && !error && (
              <>
                <table className="pig-table">
                  <thead>
                    <tr>
                      <th>DATE & TIME</th>
                      <th>PEN</th>
                      <th>FEED TYPE</th>
                      <th>QUANTITY</th>
                      <th>COST</th>
                      <th>RECORDED BY</th>
                      <th>ACTIONS</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transactions.map((tx) => (
                      <tr key={tx.id}>
                        <td>{formatDateTime(tx.feedingTime || tx.createdAt)}</td>
                        <td className="pig-id-col">{tx.penName || tx.penIdentifier || '-'}</td>
                        <td>{tx.feedType || '-'}</td>
                        <td>{formatNumber(tx.quantity)} {tx.unit || 'kg'}</td>
                        <td>{formatMoney(tx.cost)}</td>
                        <td>{tx.recordedBy || '-'}</td>
                        <td className="pig-actions-col feeding-actions-cell">
                          <button
                            type="button"
                            className="pig-icon-btn"
                            aria-label="Feeding actions"
                            onClick={(event) => {
                              event.stopPropagation()
                              setOpenMenuId((current) => current === tx.id ? null : tx.id)
                            }}
                          >
                            ...
                          </button>
                          {openMenuId === tx.id && (
                            <div
                              className="feeding-actions-menu"
                              role="menu"
                              onClick={(event) => event.stopPropagation()}
                            >
                              <button type="button" onClick={() => { setOpenMenuId(null); openEditModal(tx) }}>Edit</button>
                              <button type="button" className="danger" onClick={() => { setOpenMenuId(null); handleDelete(tx) }}>Delete</button>
                            </div>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                {transactions.length === 0 && (
                  <div className="pig-table-empty">
                    <h4>No feeding entries yet</h4>
                    <p>Record a feeding to start tracking daily intake and cost.</p>
                  </div>
                )}
              </>
            )}
          </div>

          {actionMessage && <p className="dashboard-muted">{actionMessage}</p>}
        </section>

        {modalOpen && (
          <div className="modal-backdrop" role="presentation" onClick={closeModal}>
            <div
              className="modal-card"
              role="dialog"
              aria-modal="true"
              aria-labelledby="feeding-modal-title"
              onClick={(event) => event.stopPropagation()}
            >
              <div className="modal-header">
                <div>
                  <p className="modal-kicker">{modalMode === 'edit' ? 'Update' : 'New Feeding'}</p>
                  <h3 id="feeding-modal-title">{modalMode === 'edit' ? 'Edit feeding' : 'Record feeding'}</h3>
                </div>
                <button type="button" className="modal-close-btn" onClick={closeModal} aria-label="Close feeding dialog">×</button>
              </div>

              <form className="modal-form" onSubmit={handleSubmit}>
                <div className="pig-modal-grid">
                  <label>
                    Pen
                    <select
                      value={form.penId}
                      onChange={(event) => setForm((current) => ({ ...current, penId: event.target.value }))}
                      required
                    >
                      <option value="" disabled>Select a pen</option>
                      {pens.map((pen) => (
                        <option key={pen.id} value={pen.id}>
                          {pen.name || pen.identifier || `Pen ${pen.id}`}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    Feed Type
                    <input
                      value={form.feedType}
                      onChange={(event) => setForm((current) => ({ ...current, feedType: event.target.value }))}
                      placeholder="Corn mix"
                      required
                    />
                  </label>
                  <label>
                    Quantity
                    <input
                      type="number"
                      min="0"
                      step="0.1"
                      value={form.quantity}
                      onChange={(event) => setForm((current) => ({ ...current, quantity: event.target.value }))}
                      placeholder="50"
                      required
                    />
                  </label>
                  <label>
                    Unit
                    <select
                      value={form.unit}
                      onChange={(event) => setForm((current) => ({ ...current, unit: event.target.value }))}
                    >
                      <option value="kg">kg</option>
                      <option value="lb">lb</option>
                    </select>
                  </label>
                  <label>
                    Cost
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      value={form.cost}
                      onChange={(event) => setForm((current) => ({ ...current, cost: event.target.value }))}
                      placeholder="240"
                    />
                  </label>
                  <label>
                    Feeding Time
                    <input
                      type="datetime-local"
                      value={form.feedingTime}
                      onChange={(event) => setForm((current) => ({ ...current, feedingTime: event.target.value }))}
                    />
                  </label>
                </div>

                <label>
                  Notes
                  <textarea
                    rows="3"
                    value={form.notes}
                    onChange={(event) => setForm((current) => ({ ...current, notes: event.target.value }))}
                    placeholder="Optional notes about the feeding"
                  />
                </label>

                {formError && <p className="form-message error pig-modal-error">{formError}</p>}

                <div className="modal-actions">
                  <button type="button" className="modal-secondary-btn" onClick={closeModal}>Close</button>
                  <button type="submit" className="section-action-btn green" disabled={saving}>
                    {saving ? 'Saving...' : modalMode === 'edit' ? 'Save Changes' : 'Record Feeding'}
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
