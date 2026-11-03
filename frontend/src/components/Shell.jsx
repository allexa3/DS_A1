import { useAuth } from '../auth.jsx';

export default function Shell({ title, children }) {
  const { session, logout } = useAuth();
  return (
    <div className="shell">
      <header className="topbar">
        <div className="brand">Energy Management</div>
        <div className="who">
          <span>
            {session.username} <span className="role">{session.role === 'ADMIN' ? 'Administrator' : 'Client'}</span>
          </span>
          <button className="btn ghost" onClick={logout}>Sign out</button>
        </div>
      </header>
      <main className="content">
        <h1>{title}</h1>
        {children}
      </main>
    </div>
  );
}
