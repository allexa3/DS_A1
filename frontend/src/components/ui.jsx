export function Alert({ kind = 'error', children, onClose }) {
  if (!children) return null;
  return (
    <div className={`alert ${kind}`} role={kind === 'error' ? 'alert' : 'status'}>
      <span>{children}</span>
      {onClose && <button className="link" onClick={onClose}>Dismiss</button>}
    </div>
  );
}

export function Field({ label, children }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
    </label>
  );
}

/** Horizontal bar that shows a device's maximum consumption relative to the largest one in the list. */
export function LoadBar({ value, max }) {
  const pct = max > 0 ? Math.max(4, Math.round((value / max) * 100)) : 0;
  return (
    <div className="load">
      <div className="load-track"><div className="load-fill" style={{ width: `${pct}%` }} /></div>
      <span className="load-value">{value} kWh</span>
    </div>
  );
}
