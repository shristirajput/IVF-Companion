import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../utils/api';
import {
  Activity, ActivitySquare, Pill, Plus, CalendarPlus, PackageOpen,
  Upload, FileCheck, TrendingUp, Heart, Moon, Zap, ArrowRight, CheckCircle2, User
} from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Link } from 'react-router-dom';

function StatCard({ icon: Icon, iconClass, label, value, sub, delay }) {
  return (
    <div className="sp-stat-card animate-fade-up" style={{ animationDelay: delay }}>
      <div className="flex items-start gap-4">
        <div className={`sp-icon-container ${iconClass}`}>
          <Icon className="w-5 h-5" />
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{label}</p>
          <p className="text-2xl font-bold mt-0.5 truncate" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{value}</p>
          {sub && <p className="text-xs mt-1" style={{ color: 'var(--sp-outline)' }}>{sub}</p>}
        </div>
      </div>
    </div>
  );
}

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="px-4 py-3 rounded-xl border" style={{ background: '#fff', borderColor: 'var(--sp-outline-var)', boxShadow: '0 8px 32px rgba(42,100,217,0.12)' }}>
      <p className="text-xs font-semibold mb-1" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{label}</p>
      <p className="text-lg font-bold" style={{ color: 'var(--sp-primary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{payload[0].value} pg/mL</p>
    </div>
  );
};

export default function PatientDashboard() {
  const { user } = useAuth();
  const [cycle, setCycle] = useState(null);
  const [logs, setLogs] = useState([]);
  const [medications, setMedications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showLogModal, setShowLogModal] = useState(false);
  const [uploadedFiles, setUploadedFiles] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [uploadMsg, setUploadMsg] = useState('');
  const [newLog, setNewLog] = useState({ mood: 'Good', symptoms: '', hormoneLevel: '', sleepHours: '', notes: '' });

  useEffect(() => { fetchDashboardData(); }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [cycleRes, logsRes, medsRes] = await Promise.all([
        api.get('/api/cycles/current').catch(() => ({ data: null })),
        api.get('/api/patient/logs').catch(() => ({ data: [] })),
        api.get('/api/medications/current').catch(() => ({ data: [] }))
      ]);
      setCycle(cycleRes.data);
      setLogs(logsRes.data);
      setMedications(medsRes.data);
    } finally {
      setLoading(false);
    }
  };

  const handleLogSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/api/patient/logs', {
        date: new Date().toISOString().split('T')[0],
        mood: newLog.mood,
        symptoms: newLog.symptoms,
        hormoneLevel: newLog.hormoneLevel ? parseFloat(newLog.hormoneLevel) : null,
        sleepHours: newLog.sleepHours ? parseFloat(newLog.sleepHours) : null,
        notes: newLog.notes
      });
      setShowLogModal(false);
      fetchDashboardData();
      setNewLog({ mood: 'Good', symptoms: '', hormoneLevel: '', sleepHours: '', notes: '' });
    } catch { alert('Failed to save log.'); }
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setUploading(true);
    setUploadMsg('');
    const formData = new FormData();
    formData.append('file', file);
    try {
      const res = await api.post('/api/files/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
      setUploadedFiles(prev => [...prev, { name: file.name, url: res.data.url }]);
      setUploadMsg(`✅ "${file.name}" uploaded!`);
    } catch (err) {
      setUploadMsg(`❌ Upload failed.`);
    } finally {
      setUploading(false);
      e.target.value = null;
    }
  };

  const chartData = logs.slice().reverse()
    .map(l => ({ date: l.date?.slice(5), hormone: l.hormoneLevel }))
    .filter(l => l.hormone != null);

  const cyclePercent = cycle ? Math.min((cycle.currentDay / 14) * 100, 100) : 0;

  if (loading) {
    return (
      <div className="max-w-[1200px] mx-auto px-6 py-8">
        <div className="sp-skeleton h-8 w-64 mb-2 rounded-lg" />
        <div className="sp-skeleton h-4 w-48 mb-8 rounded" />
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          {[...Array(4)].map((_, i) => <div key={i} className="sp-skeleton h-28 rounded-xl" />)}
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="sp-skeleton h-72 rounded-xl lg:col-span-2" />
          <div className="sp-skeleton h-72 rounded-xl" />
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-[1200px] mx-auto px-6 py-8">
      {/* Page Header */}
      <div className="sp-page-header flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="sp-page-title">Good {new Date().getHours() < 12 ? 'Morning' : new Date().getHours() < 18 ? 'Afternoon' : 'Evening'}, {user?.fullName?.split(' ')[0]} 👋</h1>
          <p className="sp-page-subtitle mt-1">
            {cycle ? `Day ${cycle.currentDay} of your IVF cycle — you're doing great.` : 'Track your health journey from here.'}
          </p>
        </div>
        <button
          onClick={() => setShowLogModal(true)}
          className="sp-btn-primary"
        >
          <Plus className="w-4 h-4" /> Log Today
        </button>
      </div>

      {/* Stat Cards Row */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard icon={ActivitySquare} iconClass="sp-icon-primary"  label="Cycle Day"    value={cycle ? `Day ${cycle.currentDay}` : '—'} sub={cycle?.status ?? 'No active cycle'} delay="0.05s" />
        <StatCard icon={TrendingUp}    iconClass="sp-icon-teal"      label="Hormone Logs" value={logs.length}                              sub="Health entries logged"               delay="0.10s" />
        <StatCard icon={Pill}          iconClass="sp-icon-error"     label="Medications"  value={medications.length}                      sub="Active prescriptions"                delay="0.15s" />
        <StatCard icon={Heart}         iconClass="sp-icon-neutral"   label="Last Mood"    value={logs[0]?.mood ?? '—'}                    sub={logs[0]?.date ?? 'No logs yet'}      delay="0.20s" />
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        {/* Profile Card */}
        <div className="sp-card p-6 md:p-8 animate-fade-up">
          <div className="flex items-center gap-3 mb-6">
            <div className="sp-icon-container sp-icon-teal"><User className="w-5 h-5" /></div>
            <div>
              <h2 className="text-lg font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Patient Profile</h2>
              <p className="text-xs" style={{ color: 'var(--sp-outline)' }}>Clinical details</p>
            </div>
          </div>
          <div className="space-y-4">
            <div className="flex justify-between items-center pb-3 border-b" style={{ borderColor: 'var(--sp-surface-container)' }}>
              <span className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Age</span>
              <span className="text-sm font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{user?.age || '—'} yrs</span>
            </div>
            <div className="flex justify-between items-center pb-3 border-b" style={{ borderColor: 'var(--sp-surface-container)' }}>
              <span className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>AMH Level</span>
              <span className="text-sm font-bold" style={{ color: 'var(--sp-primary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{user?.amhLevel || '—'} ng/mL</span>
            </div>
            <div className="flex justify-between items-center pb-3 border-b" style={{ borderColor: 'var(--sp-surface-container)' }}>
              <span className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>FSH Level</span>
              <span className="text-sm font-bold" style={{ color: 'var(--sp-secondary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{user?.fshLevel || '—'} mIU/mL</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Physician</span>
              <span className="text-sm font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{user?.assignedDoctorName ? `Dr. ${user.assignedDoctorName}` : 'Unassigned'}</span>
            </div>
          </div>
        </div>

        {/* Cycle Progress */}
        <div className="sp-card p-6 md:p-8 lg:col-span-2 animate-fade-up" style={{ animationDelay: '0.25s' }}>
          <div className="flex items-center justify-between mb-5">
            <div className="flex items-center gap-3">
              <div className="sp-icon-container sp-icon-primary">
                <ActivitySquare className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-lg font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>IVF Cycle Progress</h2>
                <p className="text-xs" style={{ color: 'var(--sp-outline)' }}>Current stimulation phase</p>
              </div>
            </div>
            {cycle && (
              <span className="sp-badge sp-badge-primary">{cycle.status}</span>
            )}
          </div>

          {cycle ? (
            <>
              <div className="flex justify-between items-baseline mb-3">
                <span className="text-4xl font-bold" style={{ color: 'var(--sp-primary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Day {cycle.currentDay}</span>
                <span className="text-sm" style={{ color: 'var(--sp-on-surface-var)' }}>of ~14 days</span>
              </div>
              <div className="sp-progress mb-2">
                <div className="sp-progress-fill" style={{ width: `${cyclePercent}%` }} />
              </div>
              <p className="text-sm mb-5" style={{ color: 'var(--sp-outline)' }}>Started: {new Date(cycle.startDate).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}</p>
              {cycle.notes && (
                <div className="p-4 rounded-lg" style={{ background: 'var(--sp-surface-low)', border: '1px solid var(--sp-outline-var)' }}>
                  <p className="text-sm italic" style={{ color: 'var(--sp-on-surface-var)' }}>"{cycle.notes}"</p>
                </div>
              )}
              <div className="mt-5 pt-5 border-t flex items-center justify-between" style={{ borderColor: 'var(--sp-surface-container)' }}>
                <div>
                  <p className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Premium Consultation</p>
                  <p className="text-xs" style={{ color: 'var(--sp-outline)' }}>Book a 1-on-1 session with your doctor</p>
                </div>
                <button
                  onClick={async () => {
                    try {
                      const res = await api.post('/api/payments/create-checkout-session');
                      window.location.href = res.data.url;
                    } catch { alert('Payment initialization failed'); }
                  }}
                  className="sp-btn-primary text-sm"
                >
                  Pay $150 <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            </>
          ) : (
            <div className="flex flex-col items-center justify-center py-10 text-center rounded-xl" style={{ background: 'var(--sp-surface-low)', border: '1.5px dashed var(--sp-outline-var)' }}>
              <div className="w-16 h-16 rounded-2xl flex items-center justify-center mb-4" style={{ background: 'var(--sp-primary-fixed)' }}>
                <CalendarPlus className="w-8 h-8" style={{ color: 'var(--sp-primary)' }} />
              </div>
              <h3 className="font-bold text-lg mb-2" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>No Active Cycle</h3>
              <p className="text-sm mb-6 max-w-xs" style={{ color: 'var(--sp-on-surface-var)' }}>Schedule your first IVF consultation to begin your treatment journey.</p>
              <Link to="/appointments" className="sp-btn-primary text-sm">
                <CalendarPlus className="w-4 h-4" /> Book Consultation
              </Link>
            </div>
          )}
        </div>

        {/* Today's Medications */}
        <div className="sp-card p-6 animate-fade-up" style={{ animationDelay: '0.30s' }}>
          <div className="flex items-center gap-3 mb-5">
            <div className="sp-icon-container sp-icon-error">
              <Pill className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Today's Meds</h2>
              <p className="text-xs" style={{ color: 'var(--sp-outline)' }}>{medications.length} active prescription{medications.length !== 1 ? 's' : ''}</p>
            </div>
          </div>
          {medications.length > 0 ? (
            <ul className="space-y-3">
              {medications.map(med => (
                <li key={med.id} className="flex items-start gap-3 p-3 rounded-lg transition-colors" style={{ background: 'var(--sp-surface-low)' }}>
                  <div className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5" style={{ background: 'var(--sp-error-container)' }}>
                    <Pill className="w-4 h-4" style={{ color: 'var(--sp-error)' }} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-sm truncate" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{med.name}</p>
                    <p className="text-xs" style={{ color: 'var(--sp-on-surface-var)' }}>{med.dosage} · {med.timeOfDay}</p>
                    {med.instruction && <p className="text-xs mt-0.5 truncate" style={{ color: 'var(--sp-outline)' }}>{med.instruction}</p>}
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <div className="flex flex-col items-center justify-center py-8 text-center">
              <PackageOpen className="w-10 h-10 mb-3" style={{ color: 'var(--sp-outline-var)' }} />
              <p className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>No Medications</p>
              <p className="text-xs mt-1" style={{ color: 'var(--sp-outline)' }}>Your prescriptions will appear here</p>
            </div>
          )}
        </div>
      </div>

      {/* Chart + Logs Row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">

        {/* Hormone Chart */}
        <div className="sp-card p-6 animate-fade-up" style={{ animationDelay: '0.35s' }}>
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-lg font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Hormone Tracking</h2>
              <p className="text-xs" style={{ color: 'var(--sp-outline)' }}>Estradiol (E2) levels over time</p>
            </div>
            <span className="sp-badge sp-badge-primary">E2 pg/mL</span>
          </div>
          {chartData.length > 0 ? (
            <div className="h-56">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData}>
                  <defs>
                    <linearGradient id="gradHormone" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%"  stopColor="#004bba" stopOpacity={0.2} />
                      <stop offset="95%" stopColor="#004bba" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e7eeff" vertical={false} />
                  <XAxis dataKey="date" stroke="#737785" fontSize={11} tickLine={false} axisLine={false} />
                  <YAxis stroke="#737785" fontSize={11} tickLine={false} axisLine={false} />
                  <Tooltip content={<CustomTooltip />} />
                  <Area type="monotone" dataKey="hormone" stroke="#004bba" strokeWidth={2.5}
                    fillOpacity={1} fill="url(#gradHormone)"
                    activeDot={{ r: 6, fill: '#004bba', stroke: '#fff', strokeWidth: 2 }} />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="h-56 flex flex-col items-center justify-center text-center rounded-xl" style={{ background: 'var(--sp-surface-low)' }}>
              <Activity className="w-10 h-10 mb-3" style={{ color: 'var(--sp-outline-var)' }} />
              <p className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>No data yet</p>
              <p className="text-xs mt-1" style={{ color: 'var(--sp-outline)' }}>Add a daily log with hormone levels</p>
            </div>
          )}
        </div>

        {/* Recent Logs */}
        <div className="sp-card p-6 animate-fade-up" style={{ animationDelay: '0.40s' }}>
          <div className="flex items-center justify-between mb-5">
            <h2 className="text-lg font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Recent Logs</h2>
            <span className="text-xs font-semibold" style={{ color: 'var(--sp-primary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{logs.length} entries</span>
          </div>
          <div className="space-y-3 overflow-y-auto max-h-56 pr-1">
            {logs.length > 0 ? logs.map(log => (
              <div key={log.id} className="flex items-start gap-3 p-3 rounded-lg" style={{ background: 'var(--sp-surface-low)' }}>
                <div className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0"
                     style={{ background: log.mood === 'Great' || log.mood === 'Good' ? 'var(--sp-secondary-container)' : 'var(--sp-surface-highest)' }}>
                  <Heart className="w-4 h-4" style={{ color: log.mood === 'Great' || log.mood === 'Good' ? 'var(--sp-secondary)' : 'var(--sp-outline)' }} />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between">
                    <p className="text-xs font-semibold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{log.date}</p>
                    <span className="sp-badge sp-badge-teal text-xs">{log.mood}</span>
                  </div>
                  {log.symptoms && <p className="text-xs mt-1 truncate" style={{ color: 'var(--sp-on-surface-var)' }}>{log.symptoms}</p>}
                </div>
              </div>
            )) : (
              <div className="flex flex-col items-center justify-center h-40 text-center">
                <Zap className="w-8 h-8 mb-2" style={{ color: 'var(--sp-outline-var)' }} />
                <p className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>No logs yet</p>
                <p className="text-xs mt-1" style={{ color: 'var(--sp-outline)' }}>Tap "Log Today" to get started</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Lab Reports Upload */}
      <div className="sp-card p-6 animate-fade-up" style={{ animationDelay: '0.45s' }}>
        <div className="flex items-center gap-3 mb-2">
          <div className="sp-icon-container" style={{ background: 'var(--sp-on-primary-container)', color: 'var(--sp-primary-container)' }}>
            <FileCheck className="w-5 h-5" style={{ color: 'var(--sp-primary)' }} />
          </div>
          <div>
            <h2 className="text-lg font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Lab Reports & Documents</h2>
            <p className="text-xs" style={{ color: 'var(--sp-outline)' }}>Upload ultrasounds, PDF lab results, or medical documents. Max 10 MB.</p>
          </div>
        </div>
        <div className="flex flex-wrap items-center gap-4 mt-4">
          <label className="sp-btn-primary text-sm cursor-pointer" style={{ background: 'var(--sp-primary-container)' }}>
            <Upload className="w-4 h-4" />
            {uploading ? 'Uploading…' : 'Choose File'}
            <input type="file" className="hidden" accept=".pdf,.jpg,.jpeg,.png" onChange={handleFileUpload} disabled={uploading} />
          </label>
          {uploadMsg && (
            <p className="text-sm font-medium" style={{ color: uploadMsg.startsWith('✅') ? 'var(--sp-secondary)' : 'var(--sp-error)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
              {uploadMsg}
            </p>
          )}
        </div>
        {uploadedFiles.length > 0 && (
          <ul className="mt-4 space-y-2">
            {uploadedFiles.map((f, i) => (
              <li key={i} className="flex items-center justify-between p-3 rounded-lg" style={{ background: 'var(--sp-surface-low)', border: '1px solid var(--sp-outline-var)' }}>
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 flex-shrink-0" style={{ color: 'var(--sp-secondary)' }} />
                  <span className="text-sm font-medium truncate max-w-[200px]" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{f.name}</span>
                </div>
                <a href={`http://localhost:8080${f.url}`} target="_blank" rel="noreferrer"
                   className="text-sm font-semibold" style={{ color: 'var(--sp-primary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
                  Download
                </a>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Log Modal */}
      {showLogModal && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-4 animate-fade-in"
             style={{ background: 'rgba(17,28,44,0.5)', backdropFilter: 'blur(4px)' }}>
          <div className="w-full max-w-md rounded-2xl p-6" style={{ background: '#fff', boxShadow: '0 24px 80px rgba(0,75,186,0.2)' }}>
            <h3 className="text-xl font-bold mb-5" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Add Daily Log</h3>
            <form onSubmit={handleLogSubmit} className="space-y-4">
              <div>
                <label className="sp-label">Mood</label>
                <select value={newLog.mood} onChange={e => setNewLog({ ...newLog, mood: e.target.value })} className="sp-input">
                  {['Great', 'Good', 'Neutral', 'Anxious', 'Poor'].map(m => <option key={m}>{m}</option>)}
                </select>
              </div>
              <div>
                <label className="sp-label">Symptoms</label>
                <input type="text" className="sp-input" placeholder="e.g. Cramping, Fatigue"
                  value={newLog.symptoms} onChange={e => setNewLog({ ...newLog, symptoms: e.target.value })} />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="sp-label">Hormone E2 (pg/mL)</label>
                  <input type="number" step="0.1" className="sp-input" placeholder="Optional"
                    value={newLog.hormoneLevel} onChange={e => setNewLog({ ...newLog, hormoneLevel: e.target.value })} />
                </div>
                <div>
                  <label className="sp-label flex items-center gap-1"><Moon className="w-3 h-3" /> Sleep (hrs)</label>
                  <input type="number" step="0.5" className="sp-input" placeholder="e.g. 7.5"
                    value={newLog.sleepHours} onChange={e => setNewLog({ ...newLog, sleepHours: e.target.value })} />
                </div>
              </div>
              <div>
                <label className="sp-label">Notes</label>
                <textarea rows="3" className="sp-input" placeholder="Any other observations…"
                  value={newLog.notes} onChange={e => setNewLog({ ...newLog, notes: e.target.value })} />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setShowLogModal(false)} className="sp-btn-secondary text-sm">Cancel</button>
                <button type="submit" className="sp-btn-primary text-sm">Save Log</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
