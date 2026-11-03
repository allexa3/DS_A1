import { useCallback, useEffect, useState } from 'react';
import { api, describeError } from '../api.js';
import { Alert, Field, LoadBar } from '../components/ui.jsx';

const EMPTY = { name: '', description: '', maxConsumption: '' };

export default function DevicesPanel() {
  const [devices, setDevices] = useState(null);
  const [users, setUsers] = useState([]);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const load = useCallback(() => {
    api.devices.list().then(setDevices).catch((e) => setError(describeError(e)));
    api.users.list().then(setUsers).catch((e) => setError(describeError(e)));
  }, []);
  useEffect(load, [load]);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const owner = (id) => users.find((u) => u.id === id);
  const max = Math.max(0, ...(devices || []).map((d) => d.maxConsumption));

  function openNew() {
    setForm(EMPTY);
    setEditing('new');
    setError('');
    setNotice('');
  }

  function openEdit(d) {
    setForm({ name: d.name, description: d.description || '', maxConsumption: String(d.maxConsumption) });
    setEditing(d.id);
    setError('');
    setNotice('');
  }

  async function save(e) {
    e.preventDefault();
    setError('');
    const body = { name: form.name, description: form.description, maxConsumption: Number(form.maxConsumption) };
    try {
      if (editing === 'new') {
        await api.devices.create(body);
        setNotice(`Device ${form.name} created.`);
      } else {
        await api.devices.update(editing, body);
        setNotice(`Device ${form.name} updated.`);
      }
      setEditing(null);
      load();
    } catch (err) {
      setError(describeError(err));
    }
  }

  async function remove(d) {
    if (!window.confirm(`Delete device ${d.name}?`)) return;
    setError('');
    try {
      await api.devices.remove(d.id);
      setNotice(`Device ${d.name} deleted.`);
      load();
    } catch (err) {
      setError(describeError(err));
    }
  }

  async function assign(d, userId) {
    setError('');
    try {
      if (userId) {
        await api.devices.assign(d.id, userId);
        setNotice(`${d.name} assigned to ${owner(userId)?.username ?? 'user'}.`);
      } else {
        await api.devices.unassign(d.id);
        setNotice(`${d.name} is no longer assigned.`);
      }
      load();
    } catch (err) {
      setError(describeError(err));
    }
  }

  return (
    <section>
      <div className="toolbar">
        <span className="muted">{devices ? `${devices.length} device${devices.length === 1 ? '' : 's'}` : 'Loading devices…'}</span>
        <button className="btn primary" onClick={openNew}>Add device</button>
      </div>
      <Alert kind="info" onClose={() => setNotice('')}>{notice}</Alert>
      <Alert onClose={() => setError('')}>{error}</Alert>

      {editing && (
        <form className="editor" onSubmit={save}>
          <h2>{editing === 'new' ? 'Add device' : 'Edit device'}</h2>
          <div className="grid">
            <Field label="Name"><input value={form.name} onChange={set('name')} required /></Field>
            <Field label="Maximum consumption (kWh)">
              <input type="number" min="0.01" step="any" value={form.maxConsumption} onChange={set('maxConsumption')} required />
            </Field>
            <Field label="Description"><input value={form.description} onChange={set('description')} /></Field>
          </div>
          <div className="actions">
            <button className="btn primary">Save device</button>
            <button type="button" className="btn ghost" onClick={() => setEditing(null)}>Cancel</button>
          </div>
        </form>
      )}

      {devices && devices.length === 0 && <p className="empty">No devices yet. Add the first one.</p>}
      {devices && devices.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead><tr><th>Name</th><th>Description</th><th>Maximum consumption</th><th>Assigned to</th><th></th></tr></thead>
            <tbody>
              {devices.map((d) => (
                <tr key={d.id}>
                  <td>{d.name}</td>
                  <td>{d.description}</td>
                  <td><LoadBar value={d.maxConsumption} max={max} /></td>
                  <td>
                    <select aria-label={`Owner of ${d.name}`} value={d.userId || ''} onChange={(e) => assign(d, e.target.value)}>
                      <option value="">Unassigned</option>
                      {users.map((u) => <option key={u.id} value={u.id}>{u.username}</option>)}
                    </select>
                  </td>
                  <td className="row-actions">
                    <button className="link" onClick={() => openEdit(d)}>Edit</button>
                    <button className="link danger" onClick={() => remove(d)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
