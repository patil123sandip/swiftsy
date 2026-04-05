const API_BASE = '/api';

function getToken() {
    return localStorage.getItem('token');
}

function setToken(token) {
    localStorage.setItem('token', token);
}

function clearToken() {
    localStorage.removeItem('token');
    window.location.href = '/login.html';
}

async function fetchWithAuth(url, options = {}) {
    const token = getToken();
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        ...(options.headers || {})
    };

    const response = await fetch(`${API_BASE}${url}`, {
        ...options,
        headers
    });

    if (response.status === 401 || response.status === 403) {
        clearToken();
        throw new Error('Unauthorized');
    }

    return response;
}

function checkAuth() {
    if (!getToken()) {
        window.location.href = '/login.html';
    }
}

function logout() {
    clearToken();
}
