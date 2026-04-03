import React, { useEffect, useMemo, useState } from 'react'
import { createPig, deletePig, getPenDetails, updatePig } from '../api'

const MENU_ITEMS = [
  { label: 'Dashboard', href: '#dashboard', icon: '▦', enabled: true },
  { label: 'Pens', href: '#pens', icon: '▣', active: true, enabled: true },
  { label: 'Feeding', icon: 'ϟ', enabled: false },
  { label: 'Health Records', icon: '♡', enabled: false },
  { label: 'Sales', icon: '$', enabled: false },
  { label: 'Mortality', icon: '△', enabled: false }
]

function formatBirthdate(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

const DEFAULT_PIG_FORM = {
  identifier: '',
  breed: '',
  gender: '',
  birthdate: '',
  weight: '',
  weightUnit: 'kg',
  status: 'Active',
  notes: ''
}

function toDateInput(value) {
  if (!value) return ''
  const text = String(value)
  return text.includes('T') ? text.split('T')[0] : text
}

function normalizeStatus(value) {
  return String(value || '').trim().toLowerCase().replace(/\s+/g, '-')
}

export default function PenPigs({ token, onLogout, penId }) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [filterOpen, setFilterOpen] = useState(false)
  const [filters, setFilters] = useState({
    status: '',
    breed: '',
    gender: '',
    minWeight: '',
    maxWeight: '',
    startDate: '',
    endDate: ''
  })
  const [modalOpen, setModalOpen] = useState(false)
  const [modalMode, setModalMode] = useState('create')
  const [activePigId, setActivePigId] = useState(null)
  const [modalError, setModalError] = useState('')
  const [actionMessage, setActionMessage] = useState('')
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState(DEFAULT_PIG_FORM)

  const loadPenDetails = () => {
    if (!token || !penId) {
      return Promise.resolve()
    }

    setLoading(true)
    return getPenDetails(token, penId)
      .then((res) => {
        if (res.success && res.data) {
          setData(res.data)
          setError('')
        } else {
          setError(res.message || 'Unable to load pigs')
        }
      })
      .catch(() => setError('Pigs table unavailable'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    if (!token || !penId) {
      setError('Pen not found')
      setLoading(false)
      return
    }

    loadPenDetails()
  }, [token, penId])

  const pen = data?.pen || {}
  const pigs = data?.pigs || []

  const openCreateModal = () => {
    setModalMode('create')
    setActivePigId(null)
    setForm(DEFAULT_PIG_FORM)
    setModalError('')
    setModalOpen(true)
  }

  const openViewModal = (pig) => {
    setModalMode('view')
    setActivePigId(pig.id)
    setForm({
      identifier: pig.identifier || '',
      breed: pig.breed || '',
      gender: pig.gender || '',
      birthdate: toDateInput(pig.birthdate),
      weight: pig.weight ?? '',
      weightUnit: pig.weightUnit || 'kg',
      status: pig.status || 'Active',
      notes: pig.notes || ''
    })
    setModalError('')
    setModalOpen(true)
  }

  const openEditModal = (pig) => {
    setModalMode('edit')
    setActivePigId(pig.id)
    setForm({
      identifier: pig.identifier || '',
      breed: pig.breed || '',
      gender: pig.gender || '',
      birthdate: toDateInput(pig.birthdate),
      weight: pig.weight ?? '',
      weightUnit: pig.weightUnit || 'kg',
      status: pig.status || 'Active',
      notes: pig.notes || ''
    })
    setModalError('')
    setModalOpen(true)
  }

  const closeModal = () => {
    if (saving) return
    setModalOpen(false)
  }

  const handleDelete = (pig) => {
    if (!window.confirm(`Delete ${pig.identifier || 'this pig'}?`)) {
      return
    }

    setSaving(true)
    setActionMessage('')
    deletePig(token, pig.id)
      .then((res) => {
        if (!res.success) {
          setActionMessage(res.message || 'Unable to delete pig')
          return
        }
        setActionMessage('Pig deleted.')
        return loadPenDetails()
      })
      .catch(() => setActionMessage('Failed to delete pig.'))
      .finally(() => setSaving(false))
  }

  const handleModalSubmit = (event) => {
    event.preventDefault()
    if (modalMode === 'view') {
      return
    }

    setSaving(true)
    setModalError('')
    setActionMessage('')

    const weightValue = form.weight === '' ? null : Number(form.weight)
    const payload = {
      pigIdentifier: form.identifier.trim() || null,
      breed: form.breed.trim() || null,
      gender: form.gender || null,
      birthdate: form.birthdate || null,
      currentWeight: Number.isFinite(weightValue) ? weightValue : null,
      weightUnit: form.weightUnit || null,
      status: form.status || null,
      notes: form.notes.trim() || null
    }

    const request = modalMode === 'edit'
      ? updatePig(token, activePigId, payload)
      : createPig(token, penId, payload)

    request
      .then((res) => {
        if (!res.success) {
          setModalError(res.message || 'Unable to save pig')
          return
        }
        setModalOpen(false)
        setActionMessage(modalMode === 'edit' ? 'Pig updated.' : 'Pig created.')
        return loadPenDetails()
      })
      .catch(() => setModalError('Failed to save pig.'))
      .finally(() => setSaving(false))
  }

  const filteredPigs = useMemo(() => {
    const query = search.trim().toLowerCase()
    const statusFilter = normalizeStatus(filters.status)
    const breedFilter = filters.breed.trim().toLowerCase()
    const genderFilter = filters.gender.trim().toLowerCase()
    const minWeight = filters.minWeight === '' ? null : Number(filters.minWeight)
    const maxWeight = filters.maxWeight === '' ? null : Number(filters.maxWeight)
    const startDate = filters.startDate ? new Date(filters.startDate) : null
    const endDate = filters.endDate ? new Date(filters.endDate) : null

    return pigs.filter((pig) => {
      const identifier = String(pig.identifier || '').toLowerCase()
      const breed = String(pig.breed || '').toLowerCase()
      const gender = String(pig.gender || '').toLowerCase()
      const status = normalizeStatus(pig.status)
      const weightValue = Number(pig.weight)
      const birthdateValue = pig.birthdate ? new Date(pig.birthdate) : null

      if (query && ![identifier, breed, gender, status].some((value) => value.includes(query))) {
        return false
      }

      if (statusFilter && status !== statusFilter) {
        return false
      }

      if (breedFilter && !breed.includes(breedFilter)) {
        return false
      }

      if (genderFilter && !gender.includes(genderFilter)) {
        return false
      }

      if (minWeight !== null && Number.isFinite(minWeight)) {
        if (!Number.isFinite(weightValue) || weightValue < minWeight) {
          return false
        }
      }

      if (maxWeight !== null && Number.isFinite(maxWeight)) {
        if (!Number.isFinite(weightValue) || weightValue > maxWeight) {
          return false
        }
      }

      if (startDate && birthdateValue instanceof Date && !Number.isNaN(startDate.getTime())) {
        if (!birthdateValue || Number.isNaN(birthdateValue.getTime()) || birthdateValue < startDate) {
          return false
        }
      }

      if (endDate && birthdateValue instanceof Date && !Number.isNaN(endDate.getTime())) {
        if (!birthdateValue || Number.isNaN(birthdateValue.getTime()) || birthdateValue > endDate) {
          return false
        }
      }

      return true
    })
  }, [pigs, search, filters])

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
        <div className="dashboard-topbar pen-pigs-topbar">
          <div className="pen-detail-heading">
            <button type="button" className="back-link" onClick={() => { window.location.hash = `pens/${penId}` }} aria-label="Back to pen details">←</button>
            <div>
              <h2>Pigs in {pen.name || 'Pen'}</h2>
              <p className="topbar-subtitle">{filteredPigs.length} pigs</p>
            </div>
          </div>

          <div className="pen-pigs-actions">
            <div className="pen-pigs-search-wrap">
              <span className="pen-pigs-search-icon" aria-hidden="true">🔍</span>
              <input
                className="pen-pigs-search"
                placeholder="Search pigs..."
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />
            </div>
            <button type="button" className="pen-pigs-filter-btn" onClick={() => setFilterOpen((open) => !open)}>
              <span className="pen-pigs-filter-icon" aria-hidden="true">⏳</span>
              Filter
            </button>
            <button type="button" className="pen-pigs-add-btn" onClick={openCreateModal}>
              <span className="pen-pigs-add-icon" aria-hidden="true">＋</span>
              Add Pig
            </button>
          </div>
        </div>

        <section className="dashboard-content">
          {loading && <p className="dashboard-muted">Loading pigs...</p>}
          {error && <p className="form-message error dashboard-error">{error}</p>}
          {filterOpen && !loading && (
            <div className="pen-pigs-filter-panel">
              <div className="pen-pigs-filter-grid">
                <label>
                  Status
                  <select
                    value={filters.status}
                    onChange={(event) => setFilters((current) => ({ ...current, status: event.target.value }))}
                  >
                    <option value="">All</option>
                    <option value="Active">Active</option>
                    <option value="Quarantined">Quarantined</option>
                    <option value="Under Treatment">Under Treatment</option>
                  </select>
                </label>
                <label>
                  Breed
                  <input
                    value={filters.breed}
                    onChange={(event) => setFilters((current) => ({ ...current, breed: event.target.value }))}
                    placeholder="e.g. Yorkshire"
                  />
                </label>
                <label>
                  Gender
                  <select
                    value={filters.gender}
                    onChange={(event) => setFilters((current) => ({ ...current, gender: event.target.value }))}
                  >
                    <option value="">All</option>
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                  </select>
                </label>
                <label>
                  Min Weight (kg)
                  <input
                    type="number"
                    min="0"
                    value={filters.minWeight}
                    onChange={(event) => setFilters((current) => ({ ...current, minWeight: event.target.value }))}
                    placeholder="0"
                  />
                </label>
                <label>
                  Max Weight (kg)
                  <input
                    type="number"
                    min="0"
                    value={filters.maxWeight}
                    onChange={(event) => setFilters((current) => ({ ...current, maxWeight: event.target.value }))}
                    placeholder="120"
                  />
                </label>
                <label>
                  Birthdate Start
                  <input
                    type="date"
                    value={filters.startDate}
                    onChange={(event) => setFilters((current) => ({ ...current, startDate: event.target.value }))}
                  />
                </label>
                <label>
                  Birthdate End
                  <input
                    type="date"
                    value={filters.endDate}
                    onChange={(event) => setFilters((current) => ({ ...current, endDate: event.target.value }))}
                  />
                </label>
              </div>
              <div className="pen-pigs-filter-actions">
                <button
                  type="button"
                  className="modal-secondary-btn"
                  onClick={() => setFilters({ status: '', breed: '', gender: '', minWeight: '', maxWeight: '', startDate: '', endDate: '' })}
                >
                  Clear
                </button>
                <button type="button" className="section-action-btn green" onClick={() => setFilterOpen(false)}>
                  Apply
                </button>
              </div>
            </div>
          )}

          {!loading && !error && (
            <div className="pig-table-card">
              <table className="pig-table">
                <thead>
                  <tr>
                    <th>Pig ID</th>
                    <th>Breed</th>
                    <th>Gender</th>
                    <th>Birthdate</th>
                    <th>Weight</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredPigs.map((pig) => (
                    <tr key={pig.id}>
                      <td className="pig-id-col">{pig.identifier || '—'}</td>
                      <td>{pig.breed || '—'}</td>
                      <td>{pig.gender || '—'}</td>
                      <td>{formatBirthdate(pig.birthdate)}</td>
                      <td>{pig.weight ? `${pig.weight} ${pig.weightUnit || 'kg'}` : '—'}</td>
                      <td>
                        <span className={`pen-status ${normalizeStatus(pig.status) || 'active'}`}>
                          {pig.status || 'Active'}
                        </span>
                      </td>
                      <td className="pig-actions-col">
                        <button type="button" className="pig-icon-btn" aria-label="View pig" onClick={() => openViewModal(pig)}>👁</button>
                        <button type="button" className="pig-icon-btn" aria-label="Edit pig" onClick={() => openEditModal(pig)}>✎</button>
                        <button type="button" className="pig-icon-btn danger" aria-label="Delete pig" onClick={() => handleDelete(pig)}>🗑</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {filteredPigs.length === 0 && (
                <div className="pig-table-empty">
                  <h4>No matching pigs</h4>
                  <p>Try a different search term or add a pig to this pen.</p>
                </div>
              )}
            </div>
          )}

          {actionMessage && <p className="dashboard-muted">{actionMessage}</p>}
        </section>

        {modalOpen && (
          <div className="modal-backdrop" role="presentation" onClick={closeModal}>
            <div
              className="modal-card"
              role="dialog"
              aria-modal="true"
              aria-labelledby="pig-modal-title"
              onClick={(event) => event.stopPropagation()}
            >
              <div className="modal-header">
                <div>
                  <p className="modal-kicker">Pig</p>
                  <h3 id="pig-modal-title">
                    {modalMode === 'create' ? 'Add Pig' : modalMode === 'edit' ? 'Edit Pig' : 'Pig Details'}
                  </h3>
                </div>
                <button type="button" className="modal-close-btn" onClick={closeModal} aria-label="Close pig dialog">×</button>
              </div>

              <form className="modal-form" onSubmit={handleModalSubmit}>
                <div className="pig-modal-grid">
                  <label>
                    Pig ID
                    <input
                      value={form.identifier}
                      onChange={(event) => setForm((current) => ({ ...current, identifier: event.target.value }))}
                      placeholder="PIG-2026-001"
                      disabled={modalMode === 'view'}
                    />
                  </label>
                  <label>
                    Breed
                    <input
                      value={form.breed}
                      onChange={(event) => setForm((current) => ({ ...current, breed: event.target.value }))}
                      placeholder="Yorkshire"
                      disabled={modalMode === 'view'}
                    />
                  </label>
                  <label>
                    Gender
                    <select
                      value={form.gender}
                      onChange={(event) => setForm((current) => ({ ...current, gender: event.target.value }))}
                      disabled={modalMode === 'view'}
                    >
                      <option value="">Select</option>
                      <option value="Male">Male</option>
                      <option value="Female">Female</option>
                    </select>
                  </label>
                  <label>
                    Birthdate
                    <input
                      type="date"
                      value={form.birthdate}
                      onChange={(event) => setForm((current) => ({ ...current, birthdate: event.target.value }))}
                      disabled={modalMode === 'view'}
                    />
                  </label>
                  <label>
                    Weight
                    <input
                      type="number"
                      min="0"
                      value={form.weight}
                      onChange={(event) => setForm((current) => ({ ...current, weight: event.target.value }))}
                      placeholder="45"
                      disabled={modalMode === 'view'}
                    />
                  </label>
                  <label>
                    Unit
                    <select
                      value={form.weightUnit}
                      onChange={(event) => setForm((current) => ({ ...current, weightUnit: event.target.value }))}
                      disabled={modalMode === 'view'}
                    >
                      <option value="kg">kg</option>
                      <option value="lb">lb</option>
                    </select>
                  </label>
                  <label>
                    Status
                    <select
                      value={form.status}
                      onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}
                      disabled={modalMode === 'view'}
                    >
                      <option value="Active">Active</option>
                      <option value="Quarantined">Quarantined</option>
                      <option value="Under Treatment">Under Treatment</option>
                    </select>
                  </label>
                </div>

                <label>
                  Notes
                  <textarea
                    rows="3"
                    value={form.notes}
                    onChange={(event) => setForm((current) => ({ ...current, notes: event.target.value }))}
                    placeholder="Optional notes about this pig..."
                    disabled={modalMode === 'view'}
                  />
                </label>

                {modalError && <p className="form-message error pig-modal-error">{modalError}</p>}

                <div className="modal-actions">
                  <button type="button" className="modal-secondary-btn" onClick={closeModal}>Close</button>
                  {modalMode !== 'view' && (
                    <button type="submit" className="section-action-btn green" disabled={saving}>
                      {saving ? 'Saving...' : 'Save Pig'}
                    </button>
                  )}
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </section>
  )
}
