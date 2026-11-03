import { useEffect, useState } from 'react';
import { api, describeError } from '../api.js';
import { useAuth } from '../auth.jsx';
import Shell from '../components/Shell.jsx';
import { Alert, LoadBar } from '../components/ui.jsx';

export default function ClientPage() {
  const { session } = useAuth();
  const [profile, setProfile] = useState(null);
  const [devices, setDevices] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.users.get(session.userId).then(setProfile).catch(() => {});
    api.devices.ofUser(session.userId).then(setDevices).catch((e) => setError(describeError(e)));
  }, [session.userId]);

  const max = Math.max(0, ...(devices || []).map((d) => d.maxConsumption));

  return (
    <Shell title="My devices">
      {profile && <p className="muted">{profile.name} · {profile.email}</p>}
      <Alert>{error}</Alert>
      {devices === null && !error && <p className="muted">Loading devices…</p>}
      {devices && devices.length === 0 && (
        <p className="empty">No devices are assigned to your account yet. Ask an administrator to assign one.</p>
      )}
      {devices && devices.length > 0 && (
        <ul className="device-list">
          {devices.map((d) => (
            <li key={d.id}>
              <div>
                <strong>{d.name}</strong>
                {d.description && <p className="muted">{d.description}</p>}
              </div>
              <div>
                <span className="muted small">Maximum consumption</span>
                <LoadBar value={d.maxConsumption} max={max} />
              </div>
            </li>
          ))}
        </ul>
      )}
    </Shell>
  );
}
