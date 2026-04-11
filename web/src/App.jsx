import React, { useEffect, useState } from 'react'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Pens from './pages/Pens'
import PenDetails from './pages/PenDetails'
import PenPigs from './pages/PenPigs'

function App() {
  const [route, setRoute] = useState('login')
  const [routeParts, setRouteParts] = useState([])
  const [token, setToken] = useState(localStorage.getItem('token') || '')

  useEffect(() => {
    const resolveRoute = () => {
      const hash = window.location.hash.replace('#', '')
      const parts = hash ? hash.split('/') : []
      setRoute(parts[0] || 'login')
      setRouteParts(parts)
    }

    resolveRoute()
    window.addEventListener('hashchange', resolveRoute)

    return () => {
      window.removeEventListener('hashchange', resolveRoute)
    }
  }, [])

  useEffect(() => {
    const protectedRoutes = ['dashboard', 'pens']
    if (!token && protectedRoutes.includes(route)) {
      window.location.hash = 'login'
    }
  }, [route, token])

  const onLogin = (t) => {
    setToken(t)
    localStorage.setItem('token', t)
    window.location.hash = 'dashboard'
  }

  const onLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setToken('')
    window.location.hash = 'login'
  }

  const isAuthPage = route === 'login' || route === 'register'
  const penPigsId = route === 'pens' && routeParts.length > 2 && routeParts[2] === 'pigs' ? routeParts[1] : ''
  const penDetailsId = route === 'pens' && routeParts.length === 2 ? routeParts[1] : ''

  return (
    <div className={isAuthPage ? 'app-shell auth-shell' : 'app-shell'}>
      {route === 'register' && <Register />}
      {route === 'login' && <Login onLogin={onLogin} />}
      {route === 'dashboard' && <Dashboard token={token} onLogout={onLogout} />}
      {route === 'pens' && penPigsId && <PenPigs token={token} onLogout={onLogout} penId={penPigsId} />}
      {route === 'pens' && penDetailsId && !penPigsId && (
        <PenDetails token={token} onLogout={onLogout} penId={penDetailsId} />
      )}
      {route === 'pens' && !penDetailsId && !penPigsId && <Pens token={token} onLogout={onLogout} />}
    </div>
  )
}

export default App
