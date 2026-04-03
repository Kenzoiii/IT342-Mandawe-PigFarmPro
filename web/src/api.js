const BASE_URL = import.meta.env.VITE_API_BASE_URL || (
  window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
    ? 'http://localhost:8081/api'
    : `${window.location.origin}/api`
)

export async function register({ username, email, password, fullName }) {
  const res = await fetch(`${BASE_URL}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, email, password, fullName })
  })
  return res.json()
}

export async function login({ email, password }) {
  const res = await fetch(`${BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  })
  return res.json()
}

export async function getMe(token) {
  const res = await fetch(`${BASE_URL}/user/me`, {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  if (!res.ok) throw new Error('Unauthorized')
  return res.json()
}

export async function getDashboard(token) {
  const res = await fetch(`${BASE_URL}/user/dashboard`, {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  if (!res.ok) throw new Error('Failed to load dashboard')
  return res.json()
}

export async function createPen(token, pen) {
  const res = await fetch(`${BASE_URL}/user/pens`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(pen)
  })
  return res.json()
}

export async function updatePen(token, penId, pen) {
  const res = await fetch(`${BASE_URL}/user/pens/${penId}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(pen)
  })
  return res.json()
}

export async function getPenDetails(token, penId) {
  const res = await fetch(`${BASE_URL}/user/pens/${penId}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  if (!res.ok) throw new Error('Failed to load pen details')
  return res.json()
}

export async function createPig(token, penId, pig) {
  const res = await fetch(`${BASE_URL}/user/pens/${penId}/pigs`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(pig)
  })
  return res.json()
}

export async function updatePig(token, pigId, pig) {
  const res = await fetch(`${BASE_URL}/user/pigs/${pigId}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(pig)
  })
  return res.json()
}

export async function deletePig(token, pigId) {
  const res = await fetch(`${BASE_URL}/user/pigs/${pigId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
  return res.json()
}
