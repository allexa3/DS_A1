import { useCallback, useEffect, useState } from 'react';
import { api, describeError } from '../api.js';
import { useAuth } from '../auth.jsx';
import { Alert, Field } from '../components/ui.jsx';

const EMPTY = { username: '', password: '', name: '', email: '', role: 'CLIENT' };

export default function UsersPanel() {
  const { session } = useAuth();
  const [users, setUsers] = useState(null);
  const [editing, setEditing] = useState(null); // null = closed, 'new' or a user id
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const load = useCallback(() => {
    api.users.list().then(setUsers).catch((e) => setError(describeError(e)));
  }, []);
  useEffect(load, [load]);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  function openNew() {
    setForm(EMPTY);
    setEditing('new');
    setError('');
    setNotice('');
  }

  function openEdit(u) {
    setForm({ username: u.username, password: '', name: u.name, email: u.email, role: u.role });
    setEditing(u.id);
    setError('');
    setNotice('');
  }

  async function save(e) {
    e.preventDefault();
    setError('');
    try {
      if (editing === 'new') {
        await api.users.create(form);
        setNotice(`User ${form.username} created.`);
      } else {
        await api.users.update(editing, form);
        setNotice(`User ${form.username} updated.`);
      }
      setEditing(null);
      load();
    } catch (err) {
      setError(describeError(err));
    }
  }

  async function remove(u) {
    if (!window.confirm(`Delete user ${u.username}? Their devices will be unassigned.`)) return;
    setError('');
    try {
      await api.users.remove(u.id);
      setNotice(`User ${u.username} deleted.`);
      load();
    } catch (err) {
      setError(describeError(err));
    }
  }

  return (
    <section>
      <div className="toolbar">
        <span className="muted">{users ? `${users.length} user${users.length === 1 ? '' : 's'}` : 'Loading users…'}</span>
        <button className="btn primary" onClick={openNew}>Add user</button>
      </div>
      <Alert kind="info" onClose={() => setNotice('')}>{notice}</Alert>
      <Alert onClose={() => setError('')}>{error}</Alert>

      {editing && (
        <form className="editor" onSubmit={save}>
          <h2>{editing === 'new' ? 'Add user' : 'Edit user'}</h2>
          <div className="grid">
            <Field label="Username"><input value={form.username} onChange={set('username')} required /></Field>
            <Field label={editing === 'new' ? 'Password' : 'New password (leave empty to keep)'}>
              <input type="password" value={form.password} onChange={set('password')}
                     required={editing === 'new'} minLength={6} autoComplete="new-password" />
            </Field>
            <Field label="Full name"><input value={form.name} onChange={set('name')} required /></Field>
            <Field label="Email"><input type="email" value={form.email} onChange={set('email')} required /></Field>
            <Field label="Role">
              <select value={form.role} onChange={set('role')}>
                <option value="CLIENT">Client</option>
                <option value="ADMIN">Administrator</option>
              </select>
            </Field>
          </div>
          <div className="actions">
            <button className="btn primary">Save user</button>
            <button type="button" className="btn ghost" onClick={() => setEditing(null)}>Cancel</button>
          </div>
        </form>
      )}

      {users && users.length === 0 && <p className="empty">No users yet. Add the first one.</p>}
      {users && users.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead><tr><th>Username</th><th>Name</th><th>Email</th><th>Role</th><th></th></tr></thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.username}</td>
                  <td>{u.name}</td>
                  <td>{u.email}</td>
                  <td>{u.role === 'ADMIN' ? 'Administrator' : 'Client'}</td>
                  <td className="row-actions">
                    <button className="link" onClick={() => openEdit(u)}>Edit</button>
                    <button className="link danger" onClick={() => remove(u)} disabled={u.id === session.userId}
                            title={u.id === session.userId ? 'You cannot delete your own account' : ''}>Delete</button>
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
