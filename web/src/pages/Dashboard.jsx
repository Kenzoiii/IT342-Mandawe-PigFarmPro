import React, { useEffect, useState } from 'react'
import { getDashboard } from '../api'

export default function Dashboard({ token, onLogout }) {
  const [dashboard, setDashboard] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const metrics = dashboard?.metrics || {}
  const trendPoints = dashboard?.weightTrend || []
  const activities = dashboard?.activities || []
  const pens = dashboard?.pens || []
  const userName = dashboard?.profile?.fullName || dashboard?.profile?.username || 'Farm Manager'
  const userRole = dashboard?.profile?.role || 'Farm Manager'
  const firstName = userName.split(' ')[0] || userName
  const todayLabel = new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric' })

  const formatCurrency = (value) => new Intl.NumberFormat('en-PH', {
    style: 'currency',
    currency: 'PHP',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(Number(value || 0))

  const statCards = [
    {
      label: 'Total Pigs',
      value: metrics.totalPigs ?? 0,
      note: `+${metrics.addedThisMonth ?? 0} this month`,
      tone: 'blue',
      icon: '🐷'
    },
    {
      label: 'Active Pens',
      value: metrics.activePens ?? 0,
      note: `${metrics.pensAtCapacity ?? 0} at capacity`,
      tone: 'green',
      icon: '▦'
    },
    {
      label: 'Pending Sales',
      value: metrics.pendingSales ?? 0,
      note: `${formatCurrency(metrics.pendingSalesValue)} value`,
      tone: 'gold',
      icon: '$'
    },
    {
      label: 'Health Alerts',
      value: metrics.healthAlerts ?? 0,
      note: `${metrics.healthDueToday ?? 0} due today`,
      tone: 'rose',
      icon: '♡'
    }
  ]

  const taskItems = [
    {
      label: 'Health checks due',
      value: metrics.healthDueToday ?? 0,
      meta: 'Today',
      tone: 'rose'
    },
    {
      label: 'Pens at capacity',
      value: metrics.pensAtCapacity ?? 0,
      meta: 'Needs review',
      tone: 'gold'
    },
    {
      label: 'Pending sales',
      value: metrics.pendingSales ?? 0,
      meta: formatCurrency(metrics.pendingSalesValue),
      tone: 'blue'
    }
  ]

  const menuItems = [
    { label: 'Dashboard', href: '#dashboard', icon: '▦', active: true, enabled: true },
    { label: 'Pens', href: '#pens', icon: '▣', enabled: true },
    { label: 'Feeding', icon: 'ϟ', enabled: false },
    { label: 'Health Records', icon: '♡', enabled: false },
    { label: 'Sales', icon: '$', enabled: false },
    { label: 'Mortality', icon: '△', enabled: false }
  ]

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
          setError(res.message || 'Unable to load dashboard data')
        }
      })
      .catch(() => setError('Unauthorized or dashboard unavailable'))
      .finally(() => setLoading(false))
  }, [token])

  if (!token) {
    return (
      <section className="dashboard-page">
        <main className="dashboard-main">
          <div className="dashboard-topbar">
            <h2>Dashboard Overview</h2>
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
          <h2>Dashboard Overview</h2>
          <div className="topbar-right">
            <button type="button" className="notif-btn" aria-label="Notifications">
              🔔
              <em className="notif-dot" />
            </button>
            <div className="user-mini-card">
              <div className="avatar-bubble">👤</div>
              <div>
                <strong>{userName}</strong>
                <p>{userRole}</p>
              </div>
            </div>
          </div>
        </div>

        <section className="dashboard-content">
          <header className="dashboard-hero">
            <div>
              <h3>Welcome back, {firstName}!</h3>
              <p>Here&apos;s the latest on your pig farm today.</p>
            </div>
            <div className="dashboard-hero-chips">
              <span className="dashboard-chip">Updated {todayLabel}</span>
              <span className="dashboard-chip">Active pens: {metrics.activePens ?? 0}</span>
              <span className="dashboard-chip">Pigs tracked: {metrics.totalPigs ?? 0}</span>
            </div>
          </header>

          {loading && <p className="dashboard-muted dashboard-loading-note">Loading dashboard data...</p>}
          {error && <p className="form-message error dashboard-error">{error}</p>}

          <section className="dashboard-kpi-grid" aria-label="Farm metrics">
            {statCards.map((card) => (
              <article className="dashboard-kpi-card" key={card.label}>
                <div className="dashboard-kpi-top">
                  <p>{card.label}</p>
                  <span className={`dashboard-kpi-icon ${card.tone}`}>{card.icon}</span>
                </div>
                <h4>{loading ? '...' : card.value}</h4>
                <small className={card.note.includes('$') || card.note.includes('+') ? 'positive' : ''}>
                  {loading ? 'Updating...' : card.note}
                </small>
              </article>
            ))}
          </section>

          <section className="dashboard-main-grid">
            <article className="dashboard-panel dashboard-chart-panel">
              <div className="dashboard-panel-head">
                <h4>Weight Growth Trend</h4>
                <span className="dashboard-panel-meta">Weekly avg</span>
              </div>
              <div className="dashboard-chart">
                {loading ? (
                  <div className="dashboard-skeleton-block" />
                ) : trendPoints.length === 0 ? (
                  <p className="dashboard-muted">No health weight records yet.</p>
                ) : (
                  <div className="dashboard-chart-bars">
                    {trendPoints.map((point) => {
                      const values = trendPoints.map((item) => Number(item.value || 0))
                      const maxValue = Math.max(...values, 1)
                      const normalized = Math.max((Number(point.value || 0) / maxValue) * 100, 8)

                      return (
                        <div className="dashboard-chart-col" key={point.label}>
                          <div className="dashboard-chart-value">{Number(point.value || 0).toFixed(1)} kg</div>
                          <div className="dashboard-chart-track">
                            <div className="dashboard-chart-fill" style={{ height: `${normalized}%` }} />
                          </div>
                          <span className="dashboard-chart-label">{point.label}</span>
                        </div>
                      )
                    })}
                  </div>
                )}
              </div>
            </article>

            <article className="dashboard-panel dashboard-pen-panel">
              <div className="dashboard-panel-head">
                <h4>Pen Utilization</h4>
                <span className="dashboard-panel-meta">{pens.length} pens</span>
              </div>
              <div className="dashboard-pen-list">
                {loading && (
                  <>
                    <div className="dashboard-skeleton-row" />
                    <div className="dashboard-skeleton-row" />
                  </>
                )}
                {!loading && pens.length === 0 && (
                  <p className="dashboard-muted">Create pens to track occupancy.</p>
                )}
                {!loading && pens.slice(0, 4).map((pen) => {
                  const utilization = Number(pen.utilization || 0)
                  const fillTone = utilization >= 90 ? 'danger' : utilization >= 75 ? 'warning' : 'safe'

                  return (
                    <div className="dashboard-pen-card" key={pen.id || pen.identifier || pen.name}>
                      <div className="dashboard-pen-head">
                        <div>
                          <strong>{pen.name || pen.identifier || 'Pen'}</strong>
                          <span>{pen.status || 'Active'}</span>
                        </div>
                        <div className="dashboard-pen-count">
                          {pen.occupied ?? 0}/{pen.capacity ?? 0}
                        </div>
                      </div>
                      <div className="dashboard-pen-bar">
                        <div className={`dashboard-pen-fill ${fillTone}`} style={{ width: `${utilization}%` }} />
                      </div>
                      <div className="dashboard-pen-meta">
                        <span>{utilization}% utilized</span>
                        <span>{pen.available ?? 0} spots open</span>
                      </div>
                    </div>
                  )
                })}
              </div>
            </article>

            <article className="dashboard-panel dashboard-task-panel">
              <div className="dashboard-panel-head">
                <h4>Health & Tasks</h4>
                <span className="dashboard-panel-meta">Today</span>
              </div>
              <ul className="dashboard-task-list">
                {taskItems.map((task) => (
                  <li className={`dashboard-task ${task.tone}`} key={task.label}>
                    <div>
                      <strong>{task.value}</strong>
                      <span>{task.label}</span>
                    </div>
                    <em>{task.meta}</em>
                  </li>
                ))}
              </ul>
            </article>

            <article className="dashboard-panel dashboard-activity-panel">
              <div className="dashboard-panel-head">
                <h4>Recent Activities</h4>
                <span className="dashboard-panel-meta">Last 7 days</span>
              </div>
              <div className="dashboard-activity-list">
                {loading && (
                  <>
                    <div className="dashboard-skeleton-row" />
                    <div className="dashboard-skeleton-row" />
                  </>
                )}
                {!loading && activities.length === 0 && (
                  <p className="dashboard-muted">No recent activity yet.</p>
                )}
                {!loading && activities.map((activity) => {
                  const iconByTone = {
                    blue: '🐷',
                    green: 'ϟ',
                    gold: '$',
                    rose: '♡'
                  }

                  return (
                    <div className="dashboard-activity-item" key={`${activity.title}-${activity.timeAgo}`}>
                      <span className={`dashboard-activity-icon ${activity.tone || 'blue'}`}>
                        {iconByTone[activity.tone] || '•'}
                      </span>
                      <div>
                        <strong>{activity.title}</strong>
                        <p>{activity.timeAgo}</p>
                      </div>
                    </div>
                  )
                })}
              </div>
            </article>
          </section>
        </section>
      </main>
    </section>
  )
}
