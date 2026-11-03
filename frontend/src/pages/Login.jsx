import { useState } from 'react';
import { api, describeError } from '../api.js';
import { useAuth } from '../auth.jsx';
import { Alert, Field } from '../components/ui.jsx';

export default function Login() {
  const { login } = useAuth();
  const [mode, setMode] = useState('login'); // 'login' | 'register'
  const [form, setForm] = useState({ username: '', password: '', name: '', email: '' });
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [busy, setBusy] = useState(false);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function submit(e) {
    e.preventDefault();
    setError('');
    setNotice('');
    setBusy(true);
    try {
      if (mode === 'register') {
        await api.register(form);
        setNotice('Account created. Sign in with your new username and password.');
        setMode('login');
        setForm({ ...form, password: '' });
      } else {
        await login(form.username, form.password);
      }
    } catch (err) {
      setError(describeError(err));
    } finally {
      setBusy(false);
    }
  }

  const isRegister = mode === 'register';

  return (
    <div className="login">
      <section className="login-side">
        <h1>Energy Management</h1>
        <p>Monitor your smart energy meters and see which devices are assigned to you.</p>
      </section>
      <section className="login-form">
        <form onSubmit={submit}>
          <h2>{isRegister ? 'Create an account' : 'Sign in'}</h2>
          <Alert kind="info">{notice}</Alert>
          <Alert>{error}</Alert>
          <Field label="Username">
            <input value={form.username} onChange={set('username')} autoComplete="username" required autoFocus />
          </Field>
          {isRegister && (
            <>
              <Field label="Full name">
                <input value={form.name} onChange={set('name')} autoComplete="name" required />
              </Field>
              <Field label="Email">
                <input type="email" value={form.email} onChange={set('email')} autoComplete="email" required />
              </Field>
            </>
          )}
          <Field label="Password">
            <input
              type="password"
              value={form.password}
              onChange={set('password')}
              autoComplete={isRegister ? 'new-password' : 'current-password'}
              minLength={isRegister ? 6 : undefined}
              required
            />
          </Field>
          <button className="btn primary wide" disabled={busy}>
            {busy ? 'Please wait…' : isRegister ? 'Create account' : 'Sign in'}
          </button>
          <p className="switch">
            {isRegister ? 'Already have an account?' : 'New here?'}{' '}
            <button type="button" className="link" onClick={() => { setMode(isRegister ? 'login' : 'register'); setError(''); setNotice(''); }}>
              {isRegister ? 'Sign in' : 'Create an account'}
            </button>
          </p>
        </form>
      </section>
    </div>
  );
}
