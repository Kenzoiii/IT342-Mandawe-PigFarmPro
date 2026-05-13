import React, { useEffect, useMemo, useState } from 'react'
import { createSale, getPigs, getSales, updateSale } from '../api'

const MENU_ITEMS = [
  { label: 'Dashboard', href: '#dashboard', icon: '▦', enabled: true },
  { label: 'Pens', href: '#pens', icon: '▣', enabled: true },
  { label: 'Feeding', href: '#feeding', icon: 'ϟ', enabled: true },
  { label: 'Health Records', href: '#health-records', icon: '♡', enabled: true },
  { label: 'Sales', href: '#sales', icon: '$', active: true, enabled: true },
  { label: 'Mortality', href: '#mortality', icon: '△', enabled: true }
]

export default function Sales({ token, onLogout }) {
  const [sales, setSales] = useState([])
  const [pigs, setPigs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [pigError, setPigError] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [modalMode, setModalMode] = useState('create')
  const [activeSaleId, setActiveSaleId] = useState(null)
  const [detailSale, setDetailSale] = useState(null)
  const [form, setForm] = useState({
    pigId: '',
    buyerName: '',
    buyerContact: '',
    salePrice: '',
    saleDate: '',
    expectedPickupDate: '',
    actualPickupDate: '',
    status: 'Pending',
    paymentStatus: 'Unpaid',
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

  const toDateInput = (value) => {
    if (!value) return ''
    const text = String(value)
    return text.includes('T') ? text.split('T')[0] : text
  }

  const formatDate = (value) => {
    const date = toLocalDate(value)
    if (!date) return '-'
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
  }

  const formatCurrency = (value) => new Intl.NumberFormat('en-PH', {
    style: 'currency',
    currency: 'PHP',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(Number(value || 0))

  const loadSales = () => {
    if (!token) return Promise.resolve()
    return getSales(token)
      .then((res) => {
        if (res.success && res.data) {
          setSales(res.data)
          setError('')
        } else {
          setError(res.message || 'Unable to load sales records')
        }
      })
      .catch(() => setError('Unable to load sales records'))
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
    Promise.all([loadSales(), loadPigs()])
      .finally(() => setLoading(false))
  }, [token])

  const resolveStatusLabel = (sale) => {
    const status = String(sale.status || '').trim()
    if (status) return status
    return sale.actualPickupDate ? 'Sold' : 'Pending'
  }

  const resolveStatusClass = (sale) => {
    const label = resolveStatusLabel(sale).toLowerCase()
    if (label.includes('sold') || label.includes('complete')) return 'sold'
    if (label.includes('pending')) return 'pending'
    return 'neutral'
  }

  const metrics = useMemo(() => {
    const completedSales = sales.filter((sale) => resolveStatusClass(sale) === 'sold')
    const pendingSales = sales.filter((sale) => resolveStatusClass(sale) !== 'sold')
    const today = new Date()
    const currentMonthStart = new Date(today.getFullYear(), today.getMonth(), 1)
    const lastMonthStart = new Date(today.getFullYear(), today.getMonth() - 1, 1)
    const lastMonthEnd = new Date(today.getFullYear(), today.getMonth(), 0)

    const currentMonthSales = completedSales.filter((sale) => {
      const date = toLocalDate(sale.saleDate) || toLocalDate(sale.actualPickupDate)
      return date && date >= currentMonthStart
    })

    const lastMonthSales = completedSales.filter((sale) => {
      const date = toLocalDate(sale.saleDate) || toLocalDate(sale.actualPickupDate)
      return date && date >= lastMonthStart && date <= lastMonthEnd
    })

    const sumRevenue = (items) => items.reduce((acc, sale) => acc + Number(sale.salePrice || 0), 0)
    const revenue = sumRevenue(currentMonthSales)
    const lastRevenue = sumRevenue(lastMonthSales)
    const change = lastRevenue > 0
      ? Math.round(((revenue - lastRevenue) / lastRevenue) * 100)
      : 0

    return {
      pendingSales: pendingSales.length,
      completedSales: currentMonthSales.length,
      revenue,
      change
    }
  }, [sales])

  const sortedSales = useMemo(() => (
    [...sales].sort((a, b) => {
      const first = toLocalDate(b.saleDate) || toLocalDate(b.actualPickupDate) || toLocalDate(b.expectedPickupDate)
      const second = toLocalDate(a.saleDate) || toLocalDate(a.actualPickupDate) || toLocalDate(a.expectedPickupDate)
      return (first?.getTime() || 0) - (second?.getTime() || 0)
    })
  ), [sales])

  const openCreateModal = () => {
    const today = new Date()
    const todayValue = today.toISOString().split('T')[0]
    const defaultPigId = pigs[0]?.id ? String(pigs[0].id) : ''
    setForm({
      pigId: defaultPigId,
      buyerName: '',
      buyerContact: '',
      salePrice: '',
      saleDate: todayValue,
      expectedPickupDate: '',
      actualPickupDate: '',
      status: 'Pending',
      paymentStatus: 'Unpaid',
      notes: ''
    })
    setFormError('')
    setActionMessage('')
    setModalMode('create')
    setActiveSaleId(null)
    setModalOpen(true)
  }

  const openEditModal = (sale) => {
    setForm({
      pigId: sale.pigId ? String(sale.pigId) : '',
      buyerName: sale.buyerName || '',
      buyerContact: sale.buyerContact || '',
      salePrice: sale.salePrice ?? '',
      saleDate: toDateInput(sale.saleDate),
      expectedPickupDate: toDateInput(sale.expectedPickupDate),
      actualPickupDate: toDateInput(sale.actualPickupDate),
      status: resolveStatusLabel(sale),
      paymentStatus: sale.paymentStatus || 'Unpaid',
      notes: sale.notes || ''
    })
    setFormError('')
    setActionMessage('')
    setModalMode('edit')
    setActiveSaleId(sale.id)
    setModalOpen(true)
  }

  const closeModal = () => {
    if (saving) return
    setModalOpen(false)
  }

  const handleSubmit = (event) => {
    event.preventDefault()

    const pigIdValue = form.pigId ? Number(form.pigId) : null
    if (modalMode === 'create' && !pigIdValue) {
      setFormError('Pig is required.')
      return
    }

    const buyerName = form.buyerName.trim()
    if (!buyerName) {
      setFormError('Buyer name is required.')
      return
    }

    const priceValue = Number(form.salePrice)
    if (!Number.isFinite(priceValue) || priceValue <= 0) {
      setFormError('Sale price must be greater than zero.')
      return
    }

    setSaving(true)
    setFormError('')

    const payload = {
      buyerName,
      buyerContact: form.buyerContact.trim() || null,
      salePrice: priceValue,
      saleDate: form.saleDate || null,
      expectedPickupDate: form.expectedPickupDate || null,
      actualPickupDate: form.actualPickupDate || null,
      status: form.status || null,
      paymentStatus: form.paymentStatus || null,
      notes: form.notes.trim() || null
    }

    const request = modalMode === 'create'
      ? createSale(token, { ...payload, pigId: pigIdValue })
      : updateSale(token, activeSaleId, payload)

    request
      .then((res) => {
        if (!res.success) {
          setFormError(res.message || 'Unable to save sale')
          return
        }
        setModalOpen(false)
        setActionMessage(modalMode === 'create' ? 'Sale recorded.' : 'Sale updated.')
        return loadSales()
      })
      .catch(() => setFormError('Failed to save sale.'))
      .finally(() => setSaving(false))
  }

  if (!token) {
    return (
      <section className="dashboard-page">
        <main className="dashboard-main">
          <div className="dashboard-topbar">
            <h2>Sales</h2>
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
          <h2>Sales Management</h2>
          <div className="topbar-right">
            <button type="button" className="add-pen-btn" onClick={openCreateModal}>
              <span className="add-pen-icon">+</span>
              New Sale
            </button>
          </div>
        </div>

        <section className="dashboard-content section-page-content">
          {loading && <p className="dashboard-muted">Loading sales records...</p>}
          {error && <p className="form-message error dashboard-error">{error}</p>}

          <section className="sales-kpi-grid" aria-label="Sales summary">
            <article className="sales-stat-card">
              <div className="sales-stat-head">
                <p>Pending Sales</p>
                <span className="sales-stat-icon pending" aria-hidden="true">$</span>
              </div>
              <h3>{metrics.pendingSales}</h3>
              <small>Awaiting pickup</small>
            </article>
            <article className="sales-stat-card">
              <div className="sales-stat-head">
                <p>Completed Sales</p>
                <span className="sales-stat-icon sold" aria-hidden="true">$</span>
              </div>
              <h3>{metrics.completedSales}</h3>
              <small>This month</small>
            </article>
            <article className="sales-stat-card">
              <div className="sales-stat-head">
                <p>Total Revenue</p>
                <span className="sales-stat-icon revenue" aria-hidden="true">^</span>
              </div>
              <h3>{formatCurrency(metrics.revenue)}</h3>
              <small>{metrics.change >= 0 ? '+' : ''}{metrics.change}% from last month</small>
            </article>
          </section>

          <section className="sales-table-card">
            <div className="sales-table-head">
              <h4>Sales Records</h4>
            </div>

            {!loading && sortedSales.length === 0 && (
              <div className="sales-table-empty">
                <div className="empty-illustration gold" aria-hidden="true">
                  <span />
                  <span />
                  <span />
                  <strong>$</strong>
                </div>
                <h5>No sales recorded yet</h5>
                <p>Use the New Sale button to record your first buyer and pickup details.</p>
              </div>
            )}

            {sortedSales.length > 0 && (
              <div className="sales-table-wrap">
                <table className="sales-table">
                  <thead>
                    <tr>
                      <th>Pig ID</th>
                      <th>Buyer</th>
                      <th>Contact</th>
                      <th>Price</th>
                      <th>Sale Date</th>
                      <th>Pickup Date</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {sortedSales.map((sale) => {
                      const statusLabel = resolveStatusLabel(sale)
                      const statusClass = resolveStatusClass(sale)
                      const pickupDate = sale.actualPickupDate || sale.expectedPickupDate
                      const pigLabel = sale.pigIdentifier || (sale.pigId ? `PIG-${sale.pigId}` : 'PIG')

                      return (
                        <tr key={sale.id}>
                          <td className="sales-pig-col">{pigLabel}</td>
                          <td>{sale.buyerName || '-'}</td>
                          <td>{sale.buyerContact || '-'}</td>
                          <td className="sales-price-col">{formatCurrency(sale.salePrice)}</td>
                          <td>{formatDate(sale.saleDate)}</td>
                          <td>{pickupDate ? formatDate(pickupDate) : '-'}</td>
                          <td>
                            <span className={`sales-status-badge ${statusClass}`}>{statusLabel}</span>
                          </td>
                          <td>
                            <div className="sales-action-group">
                              <button type="button" className="sales-action-btn" onClick={() => setDetailSale(sale)}>V</button>
                              <button type="button" className="sales-action-btn" onClick={() => openEditModal(sale)}>E</button>
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
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="sale-modal-title" onClick={(event) => event.stopPropagation()}>
            <div className="modal-header">
              <div>
                <p className="modal-kicker">{modalMode === 'create' ? 'New Sale' : 'Edit Sale'}</p>
                <h3 id="sale-modal-title">{modalMode === 'create' ? 'Record a sale' : 'Update sale details'}</h3>
              </div>
              <button type="button" className="modal-close-btn" onClick={closeModal} aria-label="Close sale dialog">x</button>
            </div>

            <form className="modal-form" onSubmit={handleSubmit}>
              <div className="sales-modal-grid">
                <label>
                  Pig
                  <select
                    value={form.pigId}
                    onChange={(event) => setForm((current) => ({ ...current, pigId: event.target.value }))}
                    disabled={!pigs.length || modalMode === 'edit'}
                    required={modalMode === 'create'}
                  >
                    <option value="" disabled>{pigs.length ? 'Select a pig' : 'No pigs available'}</option>
                    {pigs.map((pig) => (
                      <option key={pig.id} value={pig.id}>{buildPigLabel(pig)}</option>
                    ))}
                  </select>
                  {pigError && <span className="field-error">{pigError}</span>}
                </label>
                <label>
                  Buyer Name
                  <input
                    value={form.buyerName}
                    onChange={(event) => setForm((current) => ({ ...current, buyerName: event.target.value }))}
                    placeholder="John Smith"
                    required
                  />
                </label>
              </div>

              <div className="sales-modal-grid">
                <label>
                  Contact
                  <input
                    value={form.buyerContact}
                    onChange={(event) => setForm((current) => ({ ...current, buyerContact: event.target.value }))}
                    placeholder="(555) 123-4567"
                  />
                </label>
                <label>
                  Sale Price
                  <input
                    type="number"
                    min="0"
                    step="1"
                    value={form.salePrice}
                    onChange={(event) => setForm((current) => ({ ...current, salePrice: event.target.value }))}
                    placeholder="250"
                    required
                  />
                </label>
              </div>

              <div className="sales-modal-grid">
                <label>
                  Sale Date
                  <input
                    type="date"
                    value={form.saleDate}
                    onChange={(event) => setForm((current) => ({ ...current, saleDate: event.target.value }))}
                  />
                </label>
                <label>
                  Expected Pickup Date
                  <input
                    type="date"
                    value={form.expectedPickupDate}
                    onChange={(event) => setForm((current) => ({ ...current, expectedPickupDate: event.target.value }))}
                  />
                </label>
              </div>

              <div className="sales-modal-grid">
                <label>
                  Actual Pickup Date
                  <input
                    type="date"
                    value={form.actualPickupDate}
                    onChange={(event) => setForm((current) => ({ ...current, actualPickupDate: event.target.value }))}
                  />
                </label>
                <label>
                  Status
                  <select
                    value={form.status}
                    onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}
                  >
                    <option value="Pending">Pending</option>
                    <option value="Sold">Sold</option>
                    <option value="Completed">Completed</option>
                  </select>
                </label>
              </div>

              <div className="sales-modal-grid">
                <label>
                  Payment Status
                  <select
                    value={form.paymentStatus}
                    onChange={(event) => setForm((current) => ({ ...current, paymentStatus: event.target.value }))}
                  >
                    <option value="Unpaid">Unpaid</option>
                    <option value="Partial">Partial</option>
                    <option value="Paid">Paid</option>
                  </select>
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
                <button type="submit" className="section-action-btn gold" disabled={saving}>
                  {saving ? 'Saving...' : modalMode === 'create' ? 'Save Sale' : 'Update Sale'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {detailSale && (
        <div className="modal-backdrop" role="presentation" onClick={() => setDetailSale(null)}>
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="sale-detail-title" onClick={(event) => event.stopPropagation()}>
            <div className="modal-header">
              <div>
                <p className="modal-kicker">Sale Details</p>
                <h3 id="sale-detail-title">{detailSale.pigIdentifier || 'Sale record'}</h3>
              </div>
              <button type="button" className="modal-close-btn" onClick={() => setDetailSale(null)} aria-label="Close sale details">x</button>
            </div>

            <div className="sales-detail-grid">
              <div>
                <span>Buyer</span>
                <strong>{detailSale.buyerName || '-'}</strong>
              </div>
              <div>
                <span>Contact</span>
                <strong>{detailSale.buyerContact || '-'}</strong>
              </div>
              <div>
                <span>Price</span>
                <strong>{formatCurrency(detailSale.salePrice)}</strong>
              </div>
              <div>
                <span>Sale Date</span>
                <strong>{formatDate(detailSale.saleDate)}</strong>
              </div>
              <div>
                <span>Expected Pickup</span>
                <strong>{detailSale.expectedPickupDate ? formatDate(detailSale.expectedPickupDate) : '-'}</strong>
              </div>
              <div>
                <span>Actual Pickup</span>
                <strong>{detailSale.actualPickupDate ? formatDate(detailSale.actualPickupDate) : '-'}</strong>
              </div>
              <div>
                <span>Status</span>
                <strong>{resolveStatusLabel(detailSale)}</strong>
              </div>
              <div>
                <span>Payment</span>
                <strong>{detailSale.paymentStatus || '-'}</strong>
              </div>
            </div>

            {detailSale.notes && (
              <div className="sales-detail-notes">
                <span>Notes</span>
                <p>{detailSale.notes}</p>
              </div>
            )}
          </div>
        </div>
      )}
    </section>
  )
}
