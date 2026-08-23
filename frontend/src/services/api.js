const API_BASE = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '');

const defaultOptions = {
  credentials: 'include',
  headers: {
    'Content-Type': 'application/json',
  },
};

async function handleResponse(response) {
  if (response.status === 401) {
    // Redirect to login if unauthorized
    window.location.href = '/';
    throw new Error('Unauthorized');
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const message = data?.message || data?.error || `Request failed with status ${response.status}`;
    const error = new Error(message);
    error.status = response.status;
    error.data = data;
    throw error;
  }

  return data;
}

// Auth APIs
export const authApi = {
  getLoginUrl: () => `${API_BASE}/api/auth/login`,

  getStatus: async () => {
    const res = await fetch(`${API_BASE}/api/auth/status`, {
      credentials: 'include',
    });
    return handleResponse(res);
  },

  logout: async () => {
    const res = await fetch(`${API_BASE}/api/auth/logout`, {
      method: 'POST',
      ...defaultOptions,
    });
    return handleResponse(res);
  },
};

// Object APIs
export const objectApi = {
  getObjects: async () => {
    const res = await fetch(`${API_BASE}/api/objects`, {
      ...defaultOptions,
    });
    return handleResponse(res);
  },

  getMetadata: async (objectName) => {
    const res = await fetch(`${API_BASE}/api/objects/${objectName}/metadata`, {
      ...defaultOptions,
    });
    return handleResponse(res);
  },
};

// Record APIs
export const recordApi = {
  getRecords: async (objectName, cursor = null, signal = null) => {
    const url = cursor
      ? `${API_BASE}/api/records/${objectName}?cursor=${encodeURIComponent(cursor)}`
      : `${API_BASE}/api/records/${objectName}`;

    const res = await fetch(url, {
      ...defaultOptions,
      signal,
    });
    return handleResponse(res);
  },

  getRecord: async (objectName, id) => {
    const res = await fetch(`${API_BASE}/api/records/${objectName}/${id}`, {
      ...defaultOptions,
    });
    return handleResponse(res);
  },

  createRecord: async (objectName, fields) => {
    const res = await fetch(`${API_BASE}/api/records/${objectName}`, {
      method: 'POST',
      ...defaultOptions,
      body: JSON.stringify(fields),
    });
    return handleResponse(res);
  },

  updateRecord: async (objectName, id, fields) => {
    const res = await fetch(`${API_BASE}/api/records/${objectName}/${id}`, {
      method: 'PATCH',
      ...defaultOptions,
      body: JSON.stringify(fields),
    });
    return handleResponse(res);
  },

  deleteRecord: async (objectName, id) => {
    const res = await fetch(`${API_BASE}/api/records/${objectName}/${id}`, {
      method: 'DELETE',
      ...defaultOptions,
    });
    return handleResponse(res);
  },
};
