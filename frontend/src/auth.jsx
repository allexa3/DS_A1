import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api, clearSession, loadSession, saveSession } from './api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => loadSession());

  useEffect(() => {
    const onExpired = () => setSession(null);
    window.addEventListener('ems:session-expired', onExpired);
    return () => window.removeEventListener('ems:session-expired', onExpired);
  }, []);

  const login = useCallback(async (username, password) => {
    const res = await api.login(username, password);
    const next = { token: res.token, userId: res.userId, username: res.username, role: res.role };
    saveSession(next);
    setSession(next);
    return next;
  }, []);

  const logout = useCallback(() => {
    clearSession();
    setSession(null);
  }, []);

  const value = useMemo(() => ({ session, login, logout }), [session, login, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
