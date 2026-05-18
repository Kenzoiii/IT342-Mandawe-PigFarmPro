import React, { useEffect, useMemo, useState } from 'react'
import { createMortalityRecord, getMortalityRecords, getPigs } from '../api'

const MENU_ITEMS = [
  { label: 'Dashboard', href: '#dashboard', icon: '▦', enabled: true },
  { label: 'Pens', href: '#pens', icon: '▣', enabled: true },
  { label: 'Feeding', href: '#feeding', icon: 'ϟ', enabled: true },
  { label: 'Health Records', href: '#health-records', icon: '♡', enabled: true },
  { label: 'Sales', href: '#sales', icon: '$', enabled: true },
  { label: 'Mortality', href: '#mortality', icon: '△', active: true, enabled: true }
]

export default function Mortality({ token, onLogout }) {
  const [records, setRecords] = useState([])
  const [pigs, setPigs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [pigError, setPigError] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [detailRecord, setDetailRecord] = useState(null)
  const [form, setForm] = useState({
    pigId: '',
    dateOfDeath: '',
    ageAtDeath: '',
    causeOfDeath: '',
    weightAtDeath: '',
    symptoms: '',
    actionsTaken: '',
    notes: ''
  })
  const [formError, setFormError] = useState('')
  const [actionMessage, setActionMessage] = useState('')
  const [saving, setSaving] = useState(false)

  const toLocalDate = (value) => {
    if (!value) return null
    const text = String(value).split('T')[0]
    const parts = text.split('-').map((item) => Number(item))
    if (parts.length !== 3 || parts.some((item) => Number.isNaN(item))) {
      return null
    }
    const [year, month, day] = parts
    const local = new Date(year, month - 1, day)
    local.setHours(0, 0, 0, 0)
    return local
  }

  const formatDate = (value) => {
    const date = toLocalDate(value)
    if (!date) return '-'
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
  }

  const formatNumber = (value) => new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 1
  }).format(Number(value || 0))

  const buildPigLabel = (pig) => {
    const identifier = pig.identifier || (pig.id ? `PIG-${pig.id}` : 'Pig')
    const penLabel = pig.penName || pig.penIdentifier
    return penLabel ? `${identifier} - ${penLabel}` : identifier
  }

  const loadRecords = () => {
    if (!token) return Promise.resolve()
    return getMortalityRecords(token)
      .then((res) => {
        if (res.success && res.data) {
          setRecords(res.data)
          setError('')
        } else {
          setError(res.message || 'Unable to load mortality records')
        }
      })
      .catch(() => setError('Unable to load mortality records'))
  }

  const loadPigs = () => {
    if (!token) return Promise.resolve()
    return getPigs(token)
      .then((res) => {
        if (res.success && res.data) {
          setPigs(res.data)
          setPigError('')
        } else {
          setPigError(res.message || 'Unable to load pigs')
        }
      })
      .catch(() => setPigError('Unable to load pigs'))
  }

  useEffect(() => {
    if (!token) {
      setError('Not logged in')
      setLoading(false)
      return
    }

    setLoading(true)
    Promise.all([loadRecords(), loadPigs()])
      .finally(() => setLoading(false))
  }, [token])

  const metrics = useMemo(() => {
    const now = new Date()
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
    const totalDeceased = records.length
    const thisMonth = records.filter((record) => {
      const date = toLocalDate(record.dateOfDeath) || toLocalDate(record.recordedAt)
      return date && date >= monthStart
    }).length

    const population = pigs.length + totalDeceased
    const rate = population > 0 ? (totalDeceased / population) * 100 : 0
    const rateLabel = rate <= 3 ? 'Below average' : 'Above average'
    const monthLabel = now.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })

    return {
      totalDeceased,
      thisMonth,
      rate: Number(rate.toFixed(1)),
      rateLabel,
      monthLabel
    }
  }, [records, pigs])

  const sortedRecords = useMemo(() => (
    [...records].sort((a, b) => {
      const first = toLocalDate(b.dateOfDeath) || toLocalDate(b.recordedAt)
      const second = toLocalDate(a.dateOfDeath) || toLocalDate(a.recordedAt)
      return (first?.getTime() || 0) - (second?.getTime() || 0)
    })
  ), [records])

  const openCreateModal = () => {
    const today = new Date()
    const todayValue = today.toISOString().split('T')[0]
    const defaultPigId = pigs[0]?.id ? String(pigs[0].id) : ''
    setForm({
      pigId: defaultPigId,
      dateOfDeath: todayValue,
      ageAtDeath: '',
      causeOfDeath: '',
      weightAtDeath: '',
      symptoms: '',
      actionsTaken: '',
      notes: ''
    })
    setFormError('')
    setActionMessage('')
    setModalOpen(true)
  }

  const closeModal = () => {
    if (saving) return
    setModalOpen(false)
  }

  const handleSubmit = (event) => {
    event.preventDefault()

    const pigIdValue = form.pigId ? Number(form.pigId) : null
    if (!pigIdValue) {
      setFormError('Pig is required.')
      return
    }

    const ageValue = form.ageAtDeath === '' ? null : Number(form.ageAtDeath)
    if (ageValue !== null && (!Number.isFinite(ageValue) || ageValue < 0)) {
      setFormError('Age at death must be zero or greater.')
      return
    }

    const weightValue = form.weightAtDeath === '' ? null : Number(form.weightAtDeath)
    if (weightValue !== null && (!Number.isFinite(weightValue) || weightValue < 0)) {
      setFormError('Weight at death must be zero or greater.')
      return
    }

    setSaving(true)
    setFormError('')

    const payload = {
      pigId: pigIdValue,
      dateOfDeath: form.dateOfDeath || null,
      ageAtDeath: ageValue,
      causeOfDeath: form.causeOfDeath.trim() || null,
      weightAtDeath: weightValue,
      symptoms: form.symptoms.trim() || null,
      actionsTaken: form.actionsTaken.trim() || null,
      notes: form.notes.trim() || null
    }

    createMortalityRecord(token, payload)
      .then((res) => {
        if (!res.success) {
          setFormError(res.message || 'Unable to record mortality')
          return
        }
        setModalOpen(false)
        setActionMessage('Mortality record saved.')
        return loadRecords()
      })
      .catch(() => setFormError('Failed to save mortality record.'))
      .finally(() => setSaving(false))
  }

  if (!token) {
    return (
      <section className="dashboard-page">
        <main className="dashboard-main">
          <div className="dashboard-topbar">
            <h2>Mortality</h2>
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
          <h2>Mortality Records</h2>
          <div className="topbar-right">
            <button type="button" className="record-death-btn" onClick={openCreateModal}>
              <span className="add-pen-icon">+</span>
              Record Death
            </button>
          </div>
        </div>

        <section className="dashboard-content section-page-content">
          {loading && <p className="dashboard-muted">Loading mortality records...</p>}
          {error && <p className="form-message error dashboard-error">{error}</p>}

          <section className="mortality-kpi-grid" aria-label="Mortality summary">
            <article className="mortality-stat-card">
              <div className="mortality-stat-head">
                <p>Total Deceased</p>
                <span className="mortality-stat-icon alert" aria-hidden="true">!</span>
              </div>
              <h3>{metrics.totalDeceased}</h3>
              <small>All time</small>
            </article>
            <article className="mortality-stat-card">
              <div className="mortality-stat-head">
                <p>This Month</p>
                <span className="mortality-stat-icon neutral" aria-hidden="true">D</span>
              </div>
              <h3>{metrics.thisMonth}</h3>
              <small>{metrics.monthLabel}</small>
            </article>
            <article className="mortality-stat-card">
              <div className="mortality-stat-head">
                <p>Mortality Rate</p>
                <span className="mortality-stat-icon trend" aria-hidden="true">^</span>
              </div>
              <h3>{metrics.rate}%</h3>
              <small>{metrics.rateLabel}</small>
            </article>
          </section>

          <section className="mortality-table-card">
            <div className="mortality-table-head">
              <h4>Mortality Records</h4>
            </div>

            {!loading && sortedRecords.length === 0 && (
              <div className="mortality-table-empty">
                <div className="empty-illustration neutral" aria-hidden="true">
                  <span />
                  <span />
                  <span />
                  <strong>!</strong>
                </div>
                <h5>No mortality records yet</h5>
                <p>Record losses to track causes, weight, and follow-up actions.</p>
              </div>
            )}

            {sortedRecords.length > 0 && (
              <div className="mortality-table-wrap">
                <table className="mortality-table">
                  <thead>
                    <tr>
                      <th>Pig ID</th>
                      <th>Date of Death</th>
                      <th>Age</th>
                      <th>Cause</th>
                      <th>Weight</th>
                      <th>Recorded By</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {sortedRecords.map((record) => {
                      const pigLabel = record.pigIdentifier || (record.pigId ? `PIG-${record.pigId}` : 'PIG')
                      const ageLabel = record.ageAtDeath != null ? `${record.ageAtDeath} days` : '-'
                      const weightLabel = record.weightAtDeath != null ? `${formatNumber(record.weightAtDeath)} kg` : '-'

                      return (
                        <tr key={record.id}>
                          <td className="mortality-pig-col">{pigLabel}</td>
                          <td>{formatDate(record.dateOfDeath || record.recordedAt)}</td>
                          <td>{ageLabel}</td>
                          <td>{record.causeOfDeath || '-'}</td>
                          <td>{weightLabel}</td>
                          <td>{record.recordedBy || '-'}</td>
                          <td>
                            <div className="mortality-action-group">
                              <button type="button" className="mortality-action-btn" onClick={() => setDetailRecord(record)}>V</button>
                            </div>
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          {actionMessage && <p className="dashboard-muted">{actionMessage}</p>}
        </section>
      </main>

      {modalOpen && (
        <div className="modal-backdrop" role="presentation" onClick={closeModal}>
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="mortality-modal-title" onClick={(event) => event.stopPropagation()}>
            <div className="modal-header">
              <div>
                <p className="modal-kicker">Record Death</p>
                <h3 id="mortality-modal-title">Log a mortality record</h3>
              </div>
              <button type="button" className="modal-close-btn" onClick={closeModal} aria-label="Close mortality dialog">x</button>
            </div>

            <form className="modal-form" onSubmit={handleSubmit}>
              <div className="mortality-modal-grid">
                <label>
                  Pig
                  <select
                    value={form.pigId}
                    onChange={(event) => setForm((current) => ({ ...current, pigId: event.target.value }))}
                    disabled={!pigs.length}
                    required
                  >
                    <option value="" disabled>{pigs.length ? 'Select a pig' : 'No pigs available'}</option>
                    {pigs.map((pig) => (
                      <option key={pig.id} value={pig.id}>{buildPigLabel(pig)}</option>
                    ))}
                  </select>
                  {pigError && <span className="field-error">{pigError}</span>}
                </label>
                <label>
                  Date of Death
                  <input
                    type="date"
                    value={form.dateOfDeath}
                    onChange={(event) => setForm((current) => ({ ...current, dateOfDeath: event.target.value }))}
                  />
                </label>
              </div>

              <div className="mortality-modal-grid">
                <label>
                  Age at Death (days)
                  <input
                    type="number"
                    min="0"
                    value={form.ageAtDeath}
                    onChange={(event) => setForm((current) => ({ ...current, ageAtDeath: event.target.value }))}
                    placeholder="45"
                  />
                </label>
                <label>
                  Weight at Death (kg)
                  <input
                    type="number"
                    min="0"
                    step="0.1"
                    value={form.weightAtDeath}
                    onChange={(event) => setForm((current) => ({ ...current, weightAtDeath: event.target.value }))}
                    placeholder="35"
                  />
                </label>
              </div>

              <div className="mortality-modal-grid">
                <label>
                  Cause of Death
                  <input
                    value={form.causeOfDeath}
                    onChange={(event) => setForm((current) => ({ ...current, causeOfDeath: event.target.value }))}
                    placeholder="Natural causes"
                  />
                </label>
                <label>
                  Symptoms
                  <input
                    value={form.symptoms}
                    onChange={(event) => setForm((current) => ({ ...current, symptoms: event.target.value }))}
                    placeholder="Fever, lethargy"
                  />
                </label>
              </div>

              <div className="mortality-modal-grid">
                <label>
                  Actions Taken
                  <input
                    value={form.actionsTaken}
                    onChange={(event) => setForm((current) => ({ ...current, actionsTaken: event.target.value }))}
                    placeholder="Isolation, vet visit"
                  />
                </label>
                <label>
                  Notes
                  <input
                    value={form.notes}
                    onChange={(event) => setForm((current) => ({ ...current, notes: event.target.value }))}
                    placeholder="Optional notes"
                  />
                </label>
              </div>

              {formError && <p className="form-message error">{formError}</p>}

              <div className="modal-actions">
                <button type="button" className="modal-secondary-btn" onClick={closeModal} disabled={saving}>Cancel</button>
                <button type="submit" className="section-action-btn rose" disabled={saving}>
                  {saving ? 'Saving...' : 'Save Record'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {detailRecord && (
        <div className="modal-backdrop" role="presentation" onClick={() => setDetailRecord(null)}>
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="mortality-detail-title" onClick={(event) => event.stopPropagation()}>
            <div className="modal-header">
              <div>
                <p className="modal-kicker">Mortality Record</p>
                <h3 id="mortality-detail-title">{detailRecord.pigIdentifier || 'Pig record'}</h3>
              </div>
              <button type="button" className="modal-close-btn" onClick={() => setDetailRecord(null)} aria-label="Close mortality details">x</button>
            </div>

            <div className="mortality-detail-grid">
              <div>
                <span>Date of Death</span>
                <strong>{formatDate(detailRecord.dateOfDeath || detailRecord.recordedAt)}</strong>
              </div>
              <div>
                <span>Age</span>
                <strong>{detailRecord.ageAtDeath != null ? `${detailRecord.ageAtDeath} days` : '-'}</strong>
              </div>
              <div>
                <span>Cause</span>
                <strong>{detailRecord.causeOfDeath || '-'}</strong>
              </div>
              <div>
                <span>Weight</span>
                <strong>{detailRecord.weightAtDeath != null ? `${formatNumber(detailRecord.weightAtDeath)} kg` : '-'}</strong>
              </div>
              <div>
                <span>Symptoms</span>
                <strong>{detailRecord.symptoms || '-'}</strong>
              </div>
              <div>
                <span>Actions Taken</span>
                <strong>{detailRecord.actionsTaken || '-'}</strong>
              </div>
              <div>
                <span>Recorded By</span>
                <strong>{detailRecord.recordedBy || '-'}</strong>
              </div>
            </div>

            {detailRecord.notes && (
              <div className="mortality-detail-notes">
                <span>Notes</span>
                <p>{detailRecord.notes}</p>
              </div>
            )}
          </div>
        </div>
      )}
    </section>
  )
}
