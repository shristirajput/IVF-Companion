import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../utils/api';
import { Users, Calendar, Activity, Pill, User, ChevronRight, TrendingUp } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

function Modal({ title, onClose, onSubmit, children }) {
  return (
    <div className="fixed inset-0 flex items-center justify-center z-50 p-4 animate-fade-in"
         style={{ background: 'rgba(17,28,44,0.5)', backdropFilter: 'blur(4px)' }}>
      <div className="w-full max-w-md rounded-2xl p-6"
           style={{ background: '#fff', boxShadow: '0 24px 80px rgba(0,75,186,0.2)' }}>
        <h3 className="text-xl font-bold mb-5" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{title}</h3>
        <form onSubmit={onSubmit} className="space-y-4">
          {children}
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={onClose} className="sp-btn-secondary text-sm">Cancel</button>
            <button type="submit" className="sp-btn-primary text-sm">Confirm</button>
          </div>
        </form>
      </div>
    </div>
  );
}

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="px-4 py-3 rounded-xl border" style={{ background: '#fff', borderColor: 'var(--sp-outline-var)', boxShadow: '0 8px 32px rgba(42,100,217,0.12)' }}>
      <p className="text-xs font-semibold mb-1" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{label}</p>
      <p className="text-lg font-bold" style={{ color: 'var(--sp-secondary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{payload[0].value} pg/mL</p>
    </div>
  );
};

export default function DoctorDashboard() {
  const { user } = useAuth();
  const [patients, setPatients] = useState([]);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [patientLogs, setPatientLogs] = useState([]);
  const [patientMeds, setPatientMeds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showPrescribeModal, setShowPrescribeModal] = useState(false);
  const [showApptModal, setShowApptModal] = useState(false);
  const [medForm, setMedForm] = useState({ name: '', dosage: '', timeOfDay: 'Morning', instruction: '', startDate: '', endDate: '' });
  const [apptForm, setApptForm] = useState({ title: '', dateTime: '', notes: '' });

  useEffect(() => { fetchPatients(); }, []);

  const fetchPatients = async () => {
    try {
      setLoading(true);
      const res = await api.get('/api/doctor/patients');
      setPatients(res.data);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };

  const handleSelectPatient = async (patientId) => {
    const p = patients.find(p => p.patientId === patientId);
    setSelectedPatient(p);
    const [logsRes, medsRes] = await Promise.all([
      api.get(`/api/doctor/patients/${patientId}/logs`),
      api.get(`/api/doctor/patients/${patientId}/medications`)
    ]);
    setPatientLogs(logsRes.data);
    setPatientMeds(medsRes.data);
  };

  const handlePrescribeSubmit = async (e) => {
    e.preventDefault();
    await api.post(`/api/doctor/patients/${selectedPatient.patientId}/medications`, medForm);
    setShowPrescribeModal(false);
    setMedForm({ name: '', dosage: '', timeOfDay: 'Morning', instruction: '', startDate: '', endDate: '' });
    const res = await api.get(`/api/doctor/patients/${selectedPatient.patientId}/medications`);
    setPatientMeds(res.data);
  };

  const handleApptSubmit = async (e) => {
    e.preventDefault();
    await api.post(`/api/doctor/patients/${selectedPatient.patientId}/appointments`, apptForm);
    setShowApptModal(false);
    setApptForm({ title: '', dateTime: '', notes: '' });
  };

  const chartData = patientLogs.slice().reverse()
    .map(l => ({ date: l.date?.slice(5), hormone: l.hormoneLevel }))
    .filter(l => l.hormone != null);

  if (loading) {
    return (
      <div className="max-w-[1200px] mx-auto px-6 py-8 flex gap-6">
        <div className="sp-skeleton w-80 h-96 rounded-xl flex-shrink-0" />
        <div className="flex-1 space-y-4">
          <div className="sp-skeleton h-32 rounded-xl" />
          <div className="sp-skeleton h-64 rounded-xl" />
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-[1200px] mx-auto px-6 py-8 animate-fade-in">
      {/* Page Header */}
      <div className="sp-page-header">
        <h1 className="sp-page-title">Physician Portal</h1>
        <p className="sp-page-subtitle">Welcome back, Dr. {user?.fullName?.split(' ').slice(-1)[0]} — {patients.length} patient{patients.length !== 1 ? 's' : ''} under your care.</p>
      </div>

      <div className="flex flex-col lg:flex-row gap-6">

        {/* ── Sidebar: Patient List ── */}
        <div className="lg:w-72 xl:w-80 flex-shrink-0">
          <div className="sp-card p-5">
            <div className="flex items-center gap-3 mb-4">
              <div className="sp-icon-container sp-icon-primary"><Users className="w-5 h-5" /></div>
              <div>
                <h2 className="text-base font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>My Patients</h2>
                <p className="text-xs" style={{ color: 'var(--sp-outline)' }}>{patients.length} assigned</p>
              </div>
            </div>

            <div className="space-y-2 max-h-[600px] overflow-y-auto pr-1">
              {patients.length > 0 ? patients.map(p => {
                const isSelected = selectedPatient?.patientId === p.patientId;
                const initials = p.fullName?.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
                return (
                  <button
                    key={p.patientId}
                    onClick={() => handleSelectPatient(p.patientId)}
                    className="w-full text-left p-3 rounded-xl border transition-all duration-200"
                    style={{
                      background: isSelected ? 'var(--sp-primary-fixed)' : 'var(--sp-surface-low)',
                      borderColor: isSelected ? 'var(--sp-primary)' : 'var(--sp-outline-var)',
                    }}
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0"
                           style={{
                             background: isSelected ? 'var(--sp-primary-container)' : 'var(--sp-surface-highest)',
                             color: isSelected ? '#fff' : 'var(--sp-on-surface-var)',
                             fontFamily: '"Plus Jakarta Sans", sans-serif'
                           }}>
                        {initials}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-semibold truncate" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{p.fullName}</p>
                        <p className="text-xs" style={{ color: 'var(--sp-outline)' }}>Age {p.age}</p>
                      </div>
                      <div className="flex flex-col items-end gap-1">
                        {p.activeCycleStatus && p.activeCycleStatus !== 'No Active Cycle' && (
                          <span className="sp-badge sp-badge-teal" style={{ fontSize: '10px' }}>{p.activeCycleStatus}</span>
                        )}
                        <ChevronRight className="w-4 h-4" style={{ color: isSelected ? 'var(--sp-primary)' : 'var(--sp-outline-var)' }} />
                      </div>
                    </div>
                  </button>
                );
              }) : (
                <div className="text-center py-8">
                  <Users className="w-10 h-10 mx-auto mb-2" style={{ color: 'var(--sp-outline-var)' }} />
                  <p className="text-sm" style={{ color: 'var(--sp-outline)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>No patients assigned yet</p>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* ── Main Panel ── */}
        <div className="flex-1 min-w-0 space-y-5">
          {!selectedPatient ? (
            <div className="sp-card flex flex-col items-center justify-center py-24 text-center">
              <div className="w-16 h-16 rounded-2xl flex items-center justify-center mb-4" style={{ background: 'var(--sp-surface-container)' }}>
                <User className="w-8 h-8" style={{ color: 'var(--sp-outline)' }} />
              </div>
              <h3 className="text-lg font-bold mb-2" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Select a Patient</h3>
              <p className="text-sm" style={{ color: 'var(--sp-outline)' }}>Choose a patient from the sidebar to view their health record.</p>
            </div>
          ) : (
            <>
              {/* Patient Header Card */}
              <div className="sp-card p-6 animate-fade-up">
                <div className="flex flex-wrap items-center justify-between gap-4">
                  <div className="flex items-center gap-4">
                    <div className="w-14 h-14 rounded-2xl flex items-center justify-center text-xl font-bold flex-shrink-0"
                         style={{ background: 'var(--sp-primary-fixed)', color: 'var(--sp-primary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
                      {selectedPatient.fullName?.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)}
                    </div>
                    <div>
                      <h2 className="text-xl font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{selectedPatient.fullName}</h2>
                      <div className="flex flex-wrap gap-3 mt-1.5">
                        <span className="sp-badge sp-badge-neutral">AMH: {selectedPatient.amhLevel ?? '—'}</span>
                        <span className="sp-badge sp-badge-neutral">FSH: {selectedPatient.fshLevel ?? '—'}</span>
                        {selectedPatient.history && <span className="sp-badge sp-badge-primary">{selectedPatient.history}</span>}
                      </div>
                    </div>
                  </div>
                  <div className="flex gap-3">
                    <button onClick={() => setShowPrescribeModal(true)} className="sp-btn-secondary text-sm">
                      <Pill className="w-4 h-4" /> Prescribe
                    </button>
                    <button onClick={() => setShowApptModal(true)} className="sp-btn-primary text-sm">
                      <Calendar className="w-4 h-4" /> Schedule
                    </button>
                  </div>
                </div>
              </div>

              {/* Hormone Chart */}
              <div className="sp-card p-6 animate-fade-up" style={{ animationDelay: '0.1s' }}>
                <div className="flex items-center justify-between mb-5">
                  <div className="flex items-center gap-3">
                    <div className="sp-icon-container sp-icon-teal"><TrendingUp className="w-5 h-5" /></div>
                    <div>
                      <h3 className="text-base font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Estrogen Tracker (E2)</h3>
                      <p className="text-xs" style={{ color: 'var(--sp-outline)' }}>Measured in pg/mL</p>
                    </div>
                  </div>
                  <span className="sp-badge sp-badge-teal">{chartData.length} data points</span>
                </div>
                {chartData.length > 0 ? (
                  <div className="h-56">
                    <ResponsiveContainer width="100%" height="100%">
                      <AreaChart data={chartData}>
                        <defs>
                          <linearGradient id="gradDocHormone" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="5%"  stopColor="#006970" stopOpacity={0.2} />
                            <stop offset="95%" stopColor="#006970" stopOpacity={0} />
                          </linearGradient>
                        </defs>
                        <CartesianGrid strokeDasharray="3 3" stroke="#e7eeff" vertical={false} />
                        <XAxis dataKey="date" stroke="#737785" fontSize={11} tickLine={false} axisLine={false} />
                        <YAxis stroke="#737785" fontSize={11} tickLine={false} axisLine={false} />
                        <Tooltip content={<CustomTooltip />} />
                        <Area type="monotone" dataKey="hormone" stroke="#006970" strokeWidth={2.5}
                          fillOpacity={1} fill="url(#gradDocHormone)"
                          activeDot={{ r: 6, fill: '#006970', stroke: '#fff', strokeWidth: 2 }} />
                      </AreaChart>
                    </ResponsiveContainer>
                  </div>
                ) : (
                  <div className="h-40 flex flex-col items-center justify-center rounded-xl" style={{ background: 'var(--sp-surface-low)' }}>
                    <Activity className="w-8 h-8 mb-2" style={{ color: 'var(--sp-outline-var)' }} />
                    <p className="text-sm" style={{ color: 'var(--sp-outline)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>No hormone data logged yet</p>
                  </div>
                )}
              </div>

              {/* Logs + Medications Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5 animate-fade-up" style={{ animationDelay: '0.2s' }}>
                {/* Patient Logs */}
                <div className="sp-card p-5 flex flex-col max-h-80">
                  <h3 className="text-base font-bold mb-4" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Health Logs</h3>
                  <div className="overflow-y-auto flex-1 space-y-2 pr-1">
                    {patientLogs.length > 0 ? patientLogs.map(log => (
                      <div key={log.id} className="p-3 rounded-lg" style={{ background: 'var(--sp-surface-low)', border: '1px solid var(--sp-outline-var)' }}>
                        <div className="flex justify-between items-center">
                          <span className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{log.date}</span>
                          <span className="sp-badge sp-badge-teal">E2: {log.hormoneLevel ?? 'N/A'}</span>
                        </div>
                        <p className="text-xs mt-1" style={{ color: 'var(--sp-on-surface-var)' }}>Mood: {log.mood} · {log.symptoms || 'No symptoms'}</p>
                      </div>
                    )) : (
                      <div className="flex flex-col items-center justify-center h-24 text-center">
                        <p className="text-sm" style={{ color: 'var(--sp-outline)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>No logs available</p>
                      </div>
                    )}
                  </div>
                </div>

                {/* Medications */}
                <div className="sp-card p-5 flex flex-col max-h-80">
                  <h3 className="text-base font-bold mb-4" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Active Prescriptions</h3>
                  <div className="overflow-y-auto flex-1 space-y-2 pr-1">
                    {patientMeds.length > 0 ? patientMeds.map(med => (
                      <div key={med.id} className="p-3 rounded-lg" style={{ background: 'var(--sp-surface-low)', border: '1px solid var(--sp-outline-var)' }}>
                        <div className="flex justify-between items-start">
                          <div>
                            <p className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{med.name}</p>
                            <p className="text-xs mt-0.5" style={{ color: 'var(--sp-on-surface-var)' }}>{med.instruction}</p>
                          </div>
                          <div className="text-right flex-shrink-0 ml-2">
                            <span className="sp-badge sp-badge-error">{med.dosage}</span>
                            <p className="text-xs mt-1" style={{ color: 'var(--sp-outline)' }}>Until {new Date(med.endDate).toLocaleDateString()}</p>
                          </div>
                        </div>
                      </div>
                    )) : (
                      <div className="flex flex-col items-center justify-center h-24 text-center">
                        <p className="text-sm" style={{ color: 'var(--sp-outline)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>No prescriptions</p>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </>
          )}
        </div>
      </div>

      {/* Prescribe Modal */}
      {showPrescribeModal && (
        <Modal title="Prescribe Medication" onClose={() => setShowPrescribeModal(false)} onSubmit={handlePrescribeSubmit}>
          <div>
            <label className="sp-label">Medication Name</label>
            <input required type="text" className="sp-input" value={medForm.name} onChange={e => setMedForm({ ...medForm, name: e.target.value })} />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="sp-label">Dosage</label>
              <input required type="text" className="sp-input" placeholder="e.g. 150 IU" value={medForm.dosage} onChange={e => setMedForm({ ...medForm, dosage: e.target.value })} />
            </div>
            <div>
              <label className="sp-label">Time of Day</label>
              <select className="sp-input" value={medForm.timeOfDay} onChange={e => setMedForm({ ...medForm, timeOfDay: e.target.value })}>
                {['Morning', 'Afternoon', 'Evening', 'Night'].map(t => <option key={t}>{t}</option>)}
              </select>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="sp-label">Start Date</label>
              <input required type="date" className="sp-input" value={medForm.startDate} onChange={e => setMedForm({ ...medForm, startDate: e.target.value })} />
            </div>
            <div>
              <label className="sp-label">End Date</label>
              <input required type="date" className="sp-input" value={medForm.endDate} onChange={e => setMedForm({ ...medForm, endDate: e.target.value })} />
            </div>
          </div>
          <div>
            <label className="sp-label">Instructions</label>
            <textarea required rows="2" className="sp-input" value={medForm.instruction} onChange={e => setMedForm({ ...medForm, instruction: e.target.value })} />
          </div>
        </Modal>
      )}

      {/* Appointment Modal */}
      {showApptModal && (
        <Modal title="Schedule Appointment" onClose={() => setShowApptModal(false)} onSubmit={handleApptSubmit}>
          <div>
            <label className="sp-label">Appointment Title</label>
            <input required type="text" className="sp-input" placeholder="e.g. Baseline Ultrasound" value={apptForm.title} onChange={e => setApptForm({ ...apptForm, title: e.target.value })} />
          </div>
          <div>
            <label className="sp-label">Date & Time</label>
            <input required type="datetime-local" className="sp-input" value={apptForm.dateTime} onChange={e => setApptForm({ ...apptForm, dateTime: e.target.value })} />
          </div>
          <div>
            <label className="sp-label">Notes (Optional)</label>
            <textarea rows="2" className="sp-input" value={apptForm.notes} onChange={e => setApptForm({ ...apptForm, notes: e.target.value })} />
          </div>
        </Modal>
      )}
    </div>
  );
}
