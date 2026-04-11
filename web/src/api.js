const BASE_URL = import.meta.env.VITE_API_BASE_URL || (
  window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
    ? 'http://localhost:8081/api'
    : `${window.location.origin}/api`
)

async function requestJson(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, options)
  let payload = null

  try {
    payload = await res.json()
  } catch {
    payload = null
  }

  if (payload && typeof payload === 'object') {
    if (!res.ok && payload.success === undefined) {
      payload.success = false
    }
    if (!res.ok && !payload.message) {
      payload.message = `Request failed (${res.status})`
    }
    return payload
  }

  return {
    success: res.ok,
    data: null,
    message: res.ok ? 'Request successful' : `Request failed (${res.status})`
  }
}

export async function register({ username, email, password, fullName }) {
  return requestJson('/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, email, password, fullName })
  })
}

export async function login({ email, password }) {
  return requestJson('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  })
}

export async function getMe(token) {
  const res = await requestJson('/user/me', {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  if (!res.success) throw new Error(res.message || 'Unauthorized')
  return res
}

export async function getDashboard(token) {
  const res = await requestJson('/user/dashboard', {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  if (!res.success) throw new Error(res.message || 'Failed to load dashboard')
  return res
}

export async function createPen(token, pen) {
  return requestJson('/user/pens', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(pen)
  })
}

export async function updatePen(token, penId, pen) {
  return requestJson(`/user/pens/${penId}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(pen)
  })
}

export async function getPenDetails(token, penId) {
  const res = await requestJson(`/user/pens/${penId}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  if (!res.success) throw new Error(res.message || 'Failed to load pen details')
  return res
}

export async function createPig(token, penId, pig) {
  return requestJson(`/user/pens/${penId}/pigs`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(pig)
  })
}

export async function updatePig(token, pigId, pig) {
  return requestJson(`/user/pigs/${pigId}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(pig)
  })
}

export async function deletePig(token, pigId) {
  return requestJson(`/user/pigs/${pigId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}
