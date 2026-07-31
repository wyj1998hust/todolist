export class ApiError extends Error {
  constructor(message, status, payload = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = payload.code
    this.fieldErrors = payload.fieldErrors || {}
  }
}

export async function api(path, options = {}) {
  const headers = new Headers(options.headers || {})
  const hasBody = options.body !== undefined && options.body !== null
  if (hasBody && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  const response = await fetch(`/api${path}`, {
    ...options,
    headers,
    credentials: 'include',
    body: hasBody && typeof options.body !== 'string' ? JSON.stringify(options.body) : options.body,
  })
  const isJson = response.headers.get('content-type')?.includes('application/json')
  const payload = isJson ? await response.json() : null
  if (!response.ok) {
    throw new ApiError(payload?.message || '请求失败，请稍后重试', response.status, payload || {})
  }
  return payload
}

export const todoApi = {
  login: (body) => api('/auth/login', { method: 'POST', body }),
  me: () => api('/auth/me'),
  logout: () => api('/auth/logout', { method: 'POST' }),
  tasks: (filters = {}) => {
    const query = new URLSearchParams(Object.entries(filters).filter(([, value]) => value !== '' && value !== null && value !== undefined))
    return api(`/tasks${query.size ? `?${query}` : ''}`)
  },
  createTask: (body) => api('/tasks', { method: 'POST', body }),
  updateTask: (id, body) => api(`/tasks/${id}`, { method: 'PATCH', body }),
  deleteTask: (id) => api(`/tasks/${id}`, { method: 'DELETE' }),
  users: (includeInactive = false) => api(`/users${includeInactive ? '?includeInactive=true' : ''}`),
  createUser: (body) => api('/users', { method: 'POST', body }),
  updateUser: (id, body) => api(`/users/${id}`, { method: 'PATCH', body }),
  categories: (includeInactive = false) => api(`/categories${includeInactive ? '?includeInactive=true' : ''}`),
  createCategory: (body) => api('/categories', { method: 'POST', body }),
  updateCategory: (id, body) => api(`/categories/${id}`, { method: 'PATCH', body }),
  disableCategory: (id) => api(`/categories/${id}`, { method: 'DELETE' }),
}
