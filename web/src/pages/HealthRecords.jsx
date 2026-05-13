import React, { useEffect, useMemo, useState } from 'react'
import { createHealthRecord, getHealthRecords, getPigs } from '../api'

const MENU_ITEMS = [
  { label: 'Dashboard', href: '#dashboard', icon: '▦', enabled: true },
  { label: 'Pens', href: '#pens', icon: '▣', enabled: true },
  { label: 'Feeding', href: '#feeding', icon: 'ϟ', enabled: true },
  { label: 'Health Records', href: '#health-records', icon: '♡', active: true, enabled: true },
  { label: 'Sales', href: '#sales', icon: '$', enabled: true },
  { label: 'Mortality', href: '#mortality', icon: '△', enabled: true }
]

const DEFAULT_FORM = {
  pigId: '',
  weight: '',
  healthCondition: '',
  temperature: '',
  treatmentGiven: '',
  medicationUsed: '',
  nextTreatmentDate: '',
  nextTreatmentType: '',
  checkupDate: '',
  notes: ''
}

const toDateValue = (value) => {
  if (!value) return null
  const date = value instanceof Date ? value : new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

const toDateTimeInput = (value) => {
  const date = toDateValue(value)
  if (!date) return ''
  const offset = date.getTimezoneOffset() * 60000
  return new Date(date.getTime() - offset).toISOString().slice(0, 16)
}

const toLocalDate = (value) => {
  if (!value) return null
  if (value instanceof Date) {
    const normalized = new Date(value)
    normalized.setHours(0, 0, 0, 0)
    return normalized
  }
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

const isSameDay = (first, second) => {
  if (!first || !second) return false
  return first.getFullYear() === second.getFullYear()
    && first.getMonth() === second.getMonth()
    && first.getDate() === second.getDate()
}

const addDays = (date, amount) => {
  const next = new Date(date)
  next.setDate(next.getDate() + amount)
  return next
}

const formatDate = (value) => {
  const date = toLocalDate(value) || toDateValue(value)
  if (!date) return '-'
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

const formatDateTime = (value) => {
  const date = toDateValue(value)
  if (!date) return '-'
  return date.toLocaleString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: 'numeric', minute: '2-digit' })
}

const formatNumber = (value) => new Intl.NumberFormat('en-US', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 1
}).format(Number(value || 0))

const isVaccinationRecord = (record) => {
  const fields = [record.treatmentGiven, record.nextTreatmentType, record.medicationUsed]
  return fields.some((item) => String(item || '').toLowerCase().includes('vaccin'))
}

export default function HealthRecords({ token, onLogout }) {
  const [records, setRecords] = useState([])
  const [pigs, setPigs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [pigError, setPigError] = useState('')
  const [filter, setFilter] = useState('all')
  const [modalOpen, setModalOpen] = useState(false)
  const [detailRecord, setDetailRecord] = useState(null)
  const [form, setForm] = useState(DEFAULT_FORM)
  const [formError, setFormError] = useState('')
  const [formMessage, setFormMessage] = useState('')
  const [saving, setSaving] = useState(false)

  const loadRecords = () => {
    if (!token) return Promise.resolve()
    return getHealthRecords(token)
      .then((res) => {
        if (res.success && res.data) {
          setRecords(res.data)
          setError('')
        } else {
          setError(res.message || 'Unable to load health records')
        }
      })
      .catch(() => setError('Unable to load health records'))
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

  const buildPigLabel = (pig) => {
    const identifier = pig.identifier || (pig.id ? `PIG-${pig.id}` : 'Pig')
    const penLabel = pig.penName || pig.penIdentifier
    return penLabel ? `${identifier} - ${penLabel}` : identifier
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

  const today = useMemo(() => {
    const now = new Date()
    now.setHours(0, 0, 0, 0)
    return now
  }, [])

  const summary = useMemo(() => {
    const weekEnd = addDays(today, 6)
    const weekStart = addDays(today, -6)

    const upcoming = records.filter((record) => {
      const nextDate = toLocalDate(record.nextTreatmentDate)
      return nextDate && nextDate >= today && nextDate <= weekEnd
    })

    const recent = records.filter((record) => {
      const checkDate = toDateValue(record.checkupDate) || toDateValue(record.createdAt)
      return checkDate && checkDate >= weekStart
    })

    const todayCount = upcoming.filter((record) => isSameDay(toLocalDate(record.nextTreatmentDate), today)).length
    const tomorrow = addDays(today, 1)
    const tomorrowCount = upcoming.filter((record) => isSameDay(toLocalDate(record.nextTreatmentDate), tomorrow)).length

    const weekendCount = upcoming.filter((record) => {
      const nextDate = toLocalDate(record.nextTreatmentDate)
      if (!nextDate) return false
      const day = nextDate.getDay()
      return nextDate >= today && nextDate <= weekEnd && (day === 0 || day === 6)
    }).length

    const vaccinations = recent.filter(isVaccinationRecord).length
    const treatmentsGiven = recent.filter((record) => String(record.treatmentGiven || '').trim().length > 0).length

    return {
      upcomingCount: upcoming.length,
      todayCount,
      tomorrowCount,
      weekendCount,
      recentCount: recent.length,
      vaccinations,
      treatmentsGiven
    }
  }, [records, today])

  const filteredRecords = useMemo(() => {
    if (filter === 'all') return records
    return records.filter((record) => {
      const nextDate = toLocalDate(record.nextTreatmentDate)
      const pending = nextDate && nextDate >= today
      return filter === 'pending' ? pending : !pending
    })
  }, [records, filter, today])

  const openCreateModal = () => {
    const defaultPigId = pigs[0]?.id ? String(pigs[0].id) : ''
    setForm({
      ...DEFAULT_FORM,
      pigId: defaultPigId,
      checkupDate: toDateTimeInput(new Date())
    })
    setFormError('')
    setFormMessage('')
    setModalOpen(true)
  }

  const closeCreateModal = () => {
    if (saving) return
    setModalOpen(false)
  }

  const handleCreateSubmit = (event) => {
    event.preventDefault()

    const pigIdValue = form.pigId ? Number(form.pigId) : null
    if (!pigIdValue) {
      setFormError('Pig is required.')
      return
    }

    const weightValue = form.weight === '' ? null : Number(form.weight)
    if (weightValue !== null && (!Number.isFinite(weightValue) || weightValue < 0)) {
      setFormError('Weight must be zero or greater.')
      return
    }

    const tempValue = form.temperature === '' ? null : Number(form.temperature)
    if (tempValue !== null && (!Number.isFinite(tempValue) || tempValue < 0)) {
      setFormError('Temperature must be zero or greater.')
      return
    }

    setSaving(true)
    setFormError('')

    const payload = {
      pigId: pigIdValue,
      weight: weightValue,
      healthCondition: form.healthCondition.trim() || null,
      temperature: tempValue,
      treatmentGiven: form.treatmentGiven.trim() || null,
      medicationUsed: form.medicationUsed.trim() || null,
      nextTreatmentDate: form.nextTreatmentDate || null,
      nextTreatmentType: form.nextTreatmentType.trim() || null,
      checkupDate: form.checkupDate || null,
      notes: form.notes.trim() || null
    }

    createHealthRecord(token, payload)
      .then((res) => {
        if (!res.success) {
          setFormError(res.message || 'Unable to save health record')
          return
        }
        setFormMessage('Health record saved.')
        setModalOpen(false)
        return loadRecords()
      })
      .catch(() => setFormError('Failed to save health record.'))
      .finally(() => setSaving(false))
  }

  const resolveStatusTone = (record) => {
    const nextDate = toLocalDate(record.nextTreatmentDate)
    if (!nextDate) return 'neutral'
    if (nextDate < today) return 'danger'
    if (isSameDay(nextDate, today)) return 'danger'
    return 'ok'
  }

  const resolveNextLabel = (value) => {
    const nextDate = toLocalDate(value)
    if (!nextDate) return '-'
    if (isSameDay(nextDate, today)) return 'Today'
    if (isSameDay(nextDate, addDays(today, 1))) return 'Tomorrow'
    return formatDate(nextDate)
  }

  if (!token) {
    return (
      <section className="dashboard-page">
        <main className="dashboard-main">
          <div className="dashboard-topbar">
            <h2>Health Records</h2>
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
          <button type="button" className="menu-item"><span>⚙</span>Settings</button>
          <button type="button" className="menu-item" onClick={onLogout}><span>↪</span>Logout</button>
        </div>
      </aside>

      <main className="dashboard-main">
        <div className="dashboard-topbar">
          <h2>Health Records</h2>
          <div className="topbar-right">
            <button type="button" className="add-pen-btn" onClick={openCreateModal}>
              <span className="add-pen-icon">+</span>
              New Health Check
            </button>
          </div>
        </div>

        <section className="dashboard-content section-page-content">
          <section className="health-summary-grid" aria-label="Health highlights">
            <article className="health-summary-card alert">
              <div className="health-summary-head">
                <div>
                  <h3>Upcoming Treatments</h3>
                  <p>{summary.upcomingCount} scheduled this week</p>
                </div>
                <span className="health-summary-icon alert" aria-hidden="true">📅</span>
              </div>
              <ul className="health-summary-list">
                <li><span className="health-summary-dot alert" /> {summary.todayCount} today</li>
                <li><span className="health-summary-dot alert" /> {summary.tomorrowCount} tomorrow</li>
                <li><span className="health-summary-dot alert" /> {summary.weekendCount} this weekend</li>
              </ul>
            </article>

            <article className="health-summary-card ok">
              <div className="health-summary-head">
                <div>
                  <h3>Recent Checkups</h3>
                  <p>Last 7 days</p>
                </div>
                <span className="health-summary-icon ok" aria-hidden="true">❤</span>
              </div>
              <ul className="health-summary-list">
                <li><span className="health-summary-dot ok" /> {summary.recentCount} health checks</li>
                <li><span className="health-summary-dot ok" /> {summary.vaccinations} vaccinations</li>
                <li><span className="health-summary-dot ok" /> {summary.treatmentsGiven} treatments given</li>
              </ul>
            </article>
          </section>

          {loading && <p className="dashboard-muted">Loading health records...</p>}
          {error && <p className="form-message error dashboard-error">{error}</p>}

          <section className="dashboard-panel health-records-panel">
            <div className="health-records-head">
              <h3>Health Records</h3>
              <div className="health-filter-group" role="tablist" aria-label="Health record filters">
                {['all', 'pending', 'completed'].map((key) => (
                  <button
                    key={key}
                    type="button"
                    className={`health-filter-btn ${filter === key ? 'active' : ''}`}
                    onClick={() => setFilter(key)}
                  >
                    {key.charAt(0).toUpperCase() + key.slice(1)}
                  </button>
                ))}
              </div>
            </div>

            {!loading && filteredRecords.length === 0 && (
              <div className="health-records-empty">
                <div className="empty-illustration rose" aria-hidden="true">
                  <span />
                  <span />
                  <span />
                  <strong>♡</strong>
                </div>
                <h4>No health records yet</h4>
                <p>Log a new health check to start tracking weights, treatments, and follow ups.</p>
                <button type="button" className="section-action-btn rose" onClick={openCreateModal}>Add Health Record</button>
              </div>
            )}

            <div className="health-records-list">
              {filteredRecords.map((record) => {
                const statusTone = resolveStatusTone(record)
                const recordDate = record.checkupDate || record.createdAt || record.nextTreatmentDate
                const weightLabel = record.weight != null ? `${formatNumber(record.weight)} kg` : '-'
                const conditionLabel = record.healthCondition || '-'
                const treatmentLabel = record.treatmentGiven || record.nextTreatmentType || record.medicationUsed || '-'
                const pigLabel = record.pigIdentifier || (record.pigId ? `Pig ${record.pigId}` : 'Pig')

                return (
                  <article className="health-record-row" key={record.id}>
                    <span className={`health-record-bar ${statusTone}`} aria-hidden="true" />
                    <div className="health-record-main">
                      <div className="health-record-title">
                        <div>
                          <h4>{pigLabel}</h4>
                          <p>{formatDate(recordDate)}</p>
                        </div>
                      </div>
                      <div className="health-record-meta">
                        <span><strong>Weight:</strong> {weightLabel}</span>
                        <span><strong>Condition:</strong> {conditionLabel}</span>
                        <span><strong>Treatment:</strong> {treatmentLabel}</span>
                        <span><strong>Next:</strong> {resolveNextLabel(record.nextTreatmentDate)}</span>
                      </div>
                    </div>
                    <div className="health-record-actions">
                      <button type="button" className="health-record-btn" onClick={() => setDetailRecord(record)}>View Details</button>
                    </div>
                  </article>
                )
              })}
            </div>
          </section>

          {formMessage && <p className="dashboard-muted">{formMessage}</p>}
        </section>
      </main>

      {modalOpen && (
        <div className="modal-backdrop" role="presentation" onClick={closeCreateModal}>
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="create-health-title" onClick={(event) => event.stopPropagation()}>
            <div className="modal-header">
              <div>
                <p className="modal-kicker">New Health Check</p>
                <h3 id="create-health-title">Record a health check</h3>
              </div>
              <button type="button" className="modal-close-btn" onClick={closeCreateModal} aria-label="Close health record dialog">x</button>
            </div>

            <form className="modal-form" onSubmit={handleCreateSubmit}>
              <div className="health-modal-grid">
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
                  Checkup Date
                  <input
                    type="datetime-local"
                    value={form.checkupDate}
                    onChange={(event) => setForm((current) => ({ ...current, checkupDate: event.target.value }))}
                  />
                </label>
              </div>

              <div className="health-modal-grid">
                <label>
                  Weight (kg)
                  <input
                    type="number"
                    min="0"
                    step="0.1"
                    value={form.weight}
                    onChange={(event) => setForm((current) => ({ ...current, weight: event.target.value }))}
                    placeholder="48"
                  />
                </label>
                <label>
                  Temperature (C)
                  <input
                    type="number"
                    min="0"
                    step="0.1"
                    value={form.temperature}
                    onChange={(event) => setForm((current) => ({ ...current, temperature: event.target.value }))}
                    placeholder="39.2"
                  />
                </label>
              </div>

              <div className="health-modal-grid">
                <label>
                  Condition
                  <input
                    value={form.healthCondition}
                    onChange={(event) => setForm((current) => ({ ...current, healthCondition: event.target.value }))}
                    placeholder="Excellent"
                  />
                </label>
                <label>
                  Treatment Given
                  <input
                    value={form.treatmentGiven}
                    onChange={(event) => setForm((current) => ({ ...current, treatmentGiven: event.target.value }))}
                    placeholder="Vaccination"
                  />
                </label>
              </div>

              <div className="health-modal-grid">
                <label>
                  Medication Used
                  <input
                    value={form.medicationUsed}
                    onChange={(event) => setForm((current) => ({ ...current, medicationUsed: event.target.value }))}
                    placeholder="Dewormer"
                  />
                </label>
                <label>
                  Next Treatment Type
                  <input
                    value={form.nextTreatmentType}
                    onChange={(event) => setForm((current) => ({ ...current, nextTreatmentType: event.target.value }))}
                    placeholder="Weight check"
                  />
                </label>
              </div>

              <div className="health-modal-grid">
                <label>
                  Next Treatment Date
                  <input
                    type="date"
                    value={form.nextTreatmentDate}
                    onChange={(event) => setForm((current) => ({ ...current, nextTreatmentDate: event.target.value }))}
                  />
                </label>
                <label>
                  Notes
                  <input
                    value={form.notes}
                    onChange={(event) => setForm((current) => ({ ...current, notes: event.target.value }))}
                    placeholder="Any observations..."
                  />
                </label>
              </div>

              {formError && <p className="form-message error">{formError}</p>}

              <div className="modal-actions">
                <button type="button" className="modal-secondary-btn" onClick={closeCreateModal} disabled={saving}>Cancel</button>
                <button type="submit" className="section-action-btn rose" disabled={saving}>{saving ? 'Saving...' : 'Save Record'}</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {detailRecord && (
        <div className="modal-backdrop" role="presentation" onClick={() => setDetailRecord(null)}>
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="detail-health-title" onClick={(event) => event.stopPropagation()}>
            <div className="modal-header">
              <div>
                <p className="modal-kicker">Health Record</p>
                <h3 id="detail-health-title">{detailRecord.pigIdentifier || 'Pig record'}</h3>
              </div>
              <button type="button" className="modal-close-btn" onClick={() => setDetailRecord(null)} aria-label="Close health record details">x</button>
            </div>

            <div className="health-detail-grid">
              <div>
                <span>Checkup Date</span>
                <strong>{formatDateTime(detailRecord.checkupDate || detailRecord.createdAt)}</strong>
              </div>
              <div>
                <span>Weight</span>
                <strong>{detailRecord.weight != null ? `${formatNumber(detailRecord.weight)} kg` : '-'}</strong>
              </div>
              <div>
                <span>Condition</span>
                <strong>{detailRecord.healthCondition || '-'}</strong>
              </div>
              <div>
                <span>Temperature</span>
                <strong>{detailRecord.temperature != null ? `${formatNumber(detailRecord.temperature)} C` : '-'}</strong>
              </div>
              <div>
                <span>Treatment Given</span>
                <strong>{detailRecord.treatmentGiven || '-'}</strong>
              </div>
              <div>
                <span>Medication Used</span>
                <strong>{detailRecord.medicationUsed || '-'}</strong>
              </div>
              <div>
                <span>Next Treatment</span>
                <strong>{detailRecord.nextTreatmentType || '-'}</strong>
              </div>
              <div>
                <span>Next Treatment Date</span>
                <strong>{detailRecord.nextTreatmentDate ? formatDate(detailRecord.nextTreatmentDate) : '-'}</strong>
              </div>
              <div>
                <span>Recorded By</span>
                <strong>{detailRecord.recordedBy || '-'}</strong>
              </div>
            </div>

            {detailRecord.notes && (
              <div className="health-detail-notes">
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
