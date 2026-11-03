import { useState } from 'react';
import Shell from '../components/Shell.jsx';
import UsersPanel from './UsersPanel.jsx';
import DevicesPanel from './DevicesPanel.jsx';

export default function AdminPage() {
  const [tab, setTab] = useState('users');
  return (
    <Shell title="Administration">
      <div className="tabs" role="tablist">
        <button role="tab" aria-selected={tab === 'users'} className={tab === 'users' ? 'on' : ''} onClick={() => setTab('users')}>Users</button>
        <button role="tab" aria-selected={tab === 'devices'} className={tab === 'devices' ? 'on' : ''} onClick={() => setTab('devices')}>Devices</button>
      </div>
      {tab === 'users' ? <UsersPanel /> : <DevicesPanel />}
    </Shell>
  );
}
