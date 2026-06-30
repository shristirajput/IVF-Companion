import React, { useEffect, useState } from 'react';
import api from '../utils/api';
import { Users, UserX, Activity, Calendar, FileText, Database } from 'lucide-react';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => { fetchAdminData(); }, []);

  const fetchAdminData = async () => {
    try {
      setLoading(true);
      const [statsRes, usersRes, doctorsRes] = await Promise.all([
        api.get('/api/admin/stats'),
        api.get('/api/admin/users'),
        api.get('/api/admin/doctors')
      ]);
      setStats(statsRes.data);
      setUsers(usersRes.data);
      setDoctors(doctorsRes.data);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };

  const handleToggleActive = async (userId) => {
    try {
      setActionLoading(true);
      await api.put(`/api/admin/users/${userId}/toggle-active`);
      setUsers(prev => prev.map(u => u.id === userId ? { ...u, active: !u.active } : u));
    } catch (e) { alert('Failed to toggle user status. Admin root cannot be blocked.'); }
    finally { setActionLoading(false); }
  };

  const handleAssignDoctor = async (userId, doctorId) => {
    if (!doctorId) return;
    try {
      setActionLoading(true);
      await api.put(`/api/admin/users/${userId}/assign-doctor/${doctorId}`);
      setUsers(prev => prev.map(u => u.id === userId ? { ...u, assignedDoctorId: parseInt(doctorId) } : u));
    } catch (e) { alert('Failed to assign doctor.'); }
    finally { setActionLoading(false); }
  };

  if (loading || !stats) {
    return (
      <div className="max-w-[1200px] mx-auto px-6 py-8">
        <div className="sp-skeleton h-10 w-64 mb-8 rounded-xl" />
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-8">
          {[...Array(6)].map((_, i) => <div key={i} className="sp-skeleton h-32 rounded-xl" />)}
        </div>
        <div className="sp-skeleton h-96 w-full rounded-xl" />
      </div>
    );
  }

  const statCards = [
    { label: 'Total Users', value: stats.totalUsers, icon: Users, color: 'var(--sp-primary)' },
    { label: 'Patients', value: stats.totalPatients, icon: Activity, color: 'var(--sp-secondary)' },
    { label: 'Doctors', value: stats.totalDoctors, icon: Database, color: 'var(--sp-primary)' },
    { label: 'IVF Cycles', value: stats.totalCycles, icon: Activity, color: 'var(--sp-secondary)' },
    { label: 'Appointments', value: stats.totalAppointments, icon: Calendar, color: 'var(--sp-primary)' },
    { label: 'Forum Posts', value: stats.totalForumPosts, icon: FileText, color: 'var(--sp-secondary)' },
  ];

  return (
    <div className="max-w-[1200px] mx-auto px-6 py-8 animate-fade-in">
      <div className="sp-page-header">
        <h1 className="sp-page-title">System Administration</h1>
        <p className="sp-page-subtitle">Platform Overview and User Management</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-8 animate-fade-up">
        {statCards.map((stat, index) => {
          const Icon = stat.icon;
          const isPrimary = stat.color === 'var(--sp-primary)';
          return (
            <div key={index} className="sp-card p-5 flex flex-col items-center justify-center text-center">
              <div className="w-10 h-10 rounded-xl flex items-center justify-center mb-4" 
                   style={{ background: isPrimary ? 'var(--sp-primary-container)' : 'var(--sp-secondary-container)' }}>
                <Icon className="w-5 h-5" style={{ color: stat.color }} />
              </div>
              <p className="text-2xl font-bold mb-1" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{stat.value}</p>
              <p className="text-[10px] font-bold uppercase tracking-widest" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{stat.label}</p>
            </div>
          );
        })}
      </div>

      {/* Users List */}
      <div className="sp-card overflow-hidden animate-fade-up" style={{ animationDelay: '0.1s' }}>
        <div className="p-6 border-b" style={{ background: 'var(--sp-surface-low)', borderColor: 'var(--sp-outline-var)' }}>
          <h2 className="text-base font-bold flex items-center gap-2" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
            <Users className="w-5 h-5" style={{ color: 'var(--sp-primary)' }} />
            Platform Users
          </h2>
        </div>
        
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr style={{ borderBottom: '1px solid var(--sp-surface-container)' }}>
                <th className="p-4 text-xs font-bold uppercase tracking-widest" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>User</th>
                <th className="p-4 text-xs font-bold uppercase tracking-widest" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Role</th>
                <th className="p-4 text-xs font-bold uppercase tracking-widest" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Status</th>
                <th className="p-4 text-xs font-bold uppercase tracking-widest" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Joined</th>
                <th className="p-4 text-xs font-bold uppercase tracking-widest" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Assigned Doctor</th>
                <th className="p-4 text-xs font-bold uppercase tracking-widest text-right" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id} className="transition-colors hover:bg-black/5" style={{ borderBottom: '1px solid var(--sp-surface-container)' }}>
                  <td className="p-4">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0" 
                           style={{ background: 'var(--sp-surface-highest)', color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
                        {u.fullName.charAt(0)}
                      </div>
                      <div>
                        <div className="text-sm font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{u.fullName}</div>
                        <div className="text-xs" style={{ color: 'var(--sp-outline)' }}>{u.email}</div>
                      </div>
                    </div>
                  </td>
                  <td className="p-4">
                    <span className="sp-badge text-[10px]" style={{ background: u.role === 'ADMIN' ? 'var(--sp-primary-container)' : 'var(--sp-surface-highest)', color: u.role === 'ADMIN' ? 'var(--sp-on-primary-container)' : 'var(--sp-on-surface-var)' }}>
                      {u.role}
                    </span>
                  </td>
                  <td className="p-4">
                    <span className="sp-badge text-[10px]" style={{ background: u.active ? '#e6f7ec' : 'var(--sp-error-container)', color: u.active ? '#007a4d' : 'var(--sp-on-error-container)' }}>
                      {u.active ? 'Active' : 'Blocked'}
                    </span>
                  </td>
                  <td className="p-4 text-sm" style={{ color: 'var(--sp-on-surface-var)' }}>
                    {new Date(u.createdAt).toLocaleDateString()}
                  </td>
                  <td className="p-4">
                    {u.role === 'PATIENT' ? (
                      <select 
                        className="sp-input text-xs h-8 py-0 px-2"
                        value={u.assignedDoctorId || ''}
                        onChange={(e) => handleAssignDoctor(u.id, e.target.value)}
                        disabled={actionLoading}
                      >
                        <option value="">-- Assign Doctor --</option>
                        {doctors.map(d => (
                          <option key={d.id} value={d.id}>Dr. {d.user?.fullName || 'Unknown'}</option>
                        ))}
                      </select>
                    ) : (
                      <span className="text-xs text-slate-400">N/A</span>
                    )}
                  </td>
                  <td className="p-4 text-right">
                    <button
                      onClick={() => handleToggleActive(u.id)}
                      disabled={actionLoading || u.username === 'admin'}
                      className="sp-btn-secondary text-[11px] h-8 px-3 disabled:opacity-50"
                      style={{ border: u.active ? '1px solid var(--sp-error)' : '1px solid var(--sp-primary)', color: u.active ? 'var(--sp-error)' : 'var(--sp-primary)' }}
                    >
                      {u.active ? <><UserX className="w-3 h-3 mr-1" /> Block</> : 'Unblock'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
