// Every call goes through the reverse proxy on the same origin, so the paths are relative.
const SESSION_KEY = 'ems.session';

export class ApiError extends Error {
  constructor(status, message, details) {
    super(message);
    this.status = status;
    this.details = details || {};
  }
}

export function loadSession() {
  try {
    return JSON.parse(localStorage.getItem(SESSION_KEY));
  } catch {
    return null;
  }
}

export function saveSession(session) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearSession() {
  localStorage.removeItem(SESSION_KEY);
}

async function request(path, { method = 'GET', body } = {}) {
  const session = loadSession();
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (session?.token) headers.Authorization = `Bearer ${session.token}`;

  let res;
  try {
    res = await fetch(path, { method, headers, body: body !== undefined ? JSON.stringify(body) : undefined });
  } catch {
    throw new ApiError(0, 'The server cannot be reached. Check that the system is running and try again.');
  }

  if (res.status === 401 && session) {
    // token expired or user removed: go back to the login page
    clearSession();
    window.dispatchEvent(new Event('ems:session-expired'));
  }
  if (res.status === 204) return null;

  let data = null;
  try {
    data = await res.json();
  } catch {
    /* empty or non-JSON body */
  }
  if (!res.ok) {
    const fallback = res.status === 403 ? 'You are not allowed to do this.' : `Request failed (${res.status}).`;
    throw new ApiError(res.status, data?.message || fallback, data?.details);
  }
  return data;
}

export const describeError = (e) => {
  const extra = Object.entries(e.details || {}).map(([field, msg]) => `${field} ${msg}`);
  return extra.length ? `${e.message}: ${extra.join('; ')}` : e.message;
};

export const api = {
  login: (username, password) => request('/api/auth/login', { method: 'POST', body: { username, password } }),
  register: (data) => request('/api/auth/register', { method: 'POST', body: data }),

  users: {
    list: () => request('/api/users'),
    get: (id) => request(`/api/users/${id}`),
    create: (u) => request('/api/users', { method: 'POST', body: u }),
    update: (id, u) => request(`/api/users/${id}`, { method: 'PUT', body: u }),
    remove: (id) => request(`/api/users/${id}`, { method: 'DELETE' }),
  },

  devices: {
    list: () => request('/api/devices'),
    ofUser: (userId) => request(`/api/devices/user/${userId}`),
    create: (d) => request('/api/devices', { method: 'POST', body: d }),
    update: (id, d) => request(`/api/devices/${id}`, { method: 'PUT', body: d }),
    remove: (id) => request(`/api/devices/${id}`, { method: 'DELETE' }),
    assign: (id, userId) => request(`/api/devices/${id}/user`, { method: 'PUT', body: { userId } }),
    unassign: (id) => request(`/api/devices/${id}/user`, { method: 'DELETE' }),
  },
};
