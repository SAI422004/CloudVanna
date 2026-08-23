import { useState, useEffect, useCallback } from 'react';
import { authApi } from '../services/api';

export function useAuth() {
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  const checkAuth = useCallback(async () => {
    try {
      setLoading(true);
      const data = await authApi.getStatus();
      setAuthenticated(data.authenticated);
    } catch {
      setAuthenticated(false);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // Check for auth callback params
    const params = new URLSearchParams(window.location.search);
    if (params.get('auth_success') === 'true' || params.get('auth_error')) {
      // Clean URL
      window.history.replaceState({}, document.title, '/');
    }

    checkAuth();
  }, [checkAuth]);

  const login = useCallback(() => {
    window.location.href = authApi.getLoginUrl();
  }, []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
      setAuthenticated(false);
    } catch {
      // Even if logout fails, clear state
      setAuthenticated(false);
    }
  }, []);

  return { authenticated, loading, login, logout, checkAuth };
}
