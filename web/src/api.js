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

export async function logout(token) {
  return requestJson('/auth/logout', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  })
}

export async function getMe(token) {
  const res = await requestJson('/user/me', {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  if (res && typeof res === 'object' && res.success === undefined && res.id !== undefined) {
    return { success: true, data: res }
  }
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

export async function getPigs(token) {
  const res = await requestJson('/user/pigs', {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  if (!res.success) throw new Error(res.message || 'Failed to load pigs')
  return res
}

export async function getFeedingTransactions(token) {
  const res = await fetch(`${BASE_URL}/user/feeding`, {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  if (!res.ok) throw new Error('Failed to load feeding transactions')
  return res.json()
}

export async function recordFeeding(token, transaction) {
  const res = await fetch(`${BASE_URL}/user/feeding`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(transaction)
  })
  return res.json()
}

export async function updateFeeding(token, feedingId, transaction) {
  const res = await fetch(`${BASE_URL}/user/feeding/${feedingId}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(transaction)
  })
  return res.json()
}

export async function deleteFeeding(token, feedingId) {
  const res = await fetch(`${BASE_URL}/user/feeding/${feedingId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
  return res.json()
}

export async function getHealthRecords(token) {
  const res = await requestJson('/user/health-records', {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  if (!res.success) throw new Error(res.message || 'Failed to load health records')
  return res
}

export async function createHealthRecord(token, record) {
  return requestJson('/user/health-records', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(record)
  })
}

export async function getSales(token) {
  const res = await requestJson('/user/sales', {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  if (!res.success) throw new Error(res.message || 'Failed to load sales')
  return res
}

export async function createSale(token, sale) {
  return requestJson('/user/sales', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(sale)
  })
}

export async function updateSale(token, saleId, sale) {
  return requestJson(`/user/sales/${saleId}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(sale)
  })
}

export async function updateProfile(token, profile) {
  return requestJson('/user/me', {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(profile)
  })
}

export async function updatePassword(token, payload) {
  return requestJson('/user/password', {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  })
}

export async function getMortalityRecords(token) {
  const res = await requestJson('/user/mortality', {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  if (!res.success) throw new Error(res.message || 'Failed to load mortality records')
  return res
}

export async function createMortalityRecord(token, record) {
  return requestJson('/user/mortality', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(record)
  })
}
