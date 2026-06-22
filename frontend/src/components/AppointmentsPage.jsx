import React, { useEffect, useState } from 'react';
import api from '../utils/api';
import { Calendar, Clock, User, XCircle } from 'lucide-react';

export default function AppointmentsPage() {
  const [appointments, setAppointments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);

  const [showBookModal, setShowBookModal] = useState(false);
  const [bookForm, setBookForm] = useState({ doctorId: '', dateTime: '', title: '', notes: '' });
  const [bookLoading, setBookLoading] = useState(false);

  useEffect(() => { fetchData(); }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [apptsRes, docsRes] = await Promise.all([
        api.get('/api/patient/appointments').catch(() => ({ data: [] })),
        api.get('/api/patient/appointments/doctors').catch(() => ({ data: [] }))
      ]);
      setAppointments(apptsRes.data);
      setDoctors(docsRes.data);
      if (docsRes.data.length > 0) setBookForm(prev => ({ ...prev, doctorId: docsRes.data[0].id }));
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };

  const handleBookSubmit = async (e) => {
    e.preventDefault();
    try {
      setBookLoading(true);
      await api.post('/api/patient/appointments', bookForm);
      setShowBookModal(false);
      setBookForm({ doctorId: doctors[0]?.id || '', dateTime: '', title: '', notes: '' });
      const res = await api.get('/api/patient/appointments');
      setAppointments(res.data);
    } catch (e) { alert('Failed to book appointment.'); }
    finally { setBookLoading(false); }
  };

  const handleCancel = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this appointment?')) return;
    try {
      await api.put(`/api/patient/appointments/${id}/cancel`);
      const res = await api.get('/api/patient/appointments');
      setAppointments(res.data);
    } catch (e) { alert('Failed to cancel appointment.'); }
  };

  if (loading) {
    return (
      <div className="max-w-[1000px] mx-auto px-6 py-8">
        <div className="sp-skeleton h-10 w-64 mb-8 rounded-xl" />
        <div className="grid md:grid-cols-2 gap-8">
          <div className="sp-skeleton h-64 rounded-xl" />
          <div className="sp-skeleton h-64 rounded-xl" />
        </div>
      </div>
    );
  }

  const sortedAppointments = [...appointments].sort((a, b) => new Date(a.dateTime) - new Date(b.dateTime));
  const upcoming = sortedAppointments.filter(a => new Date(a.dateTime) >= new Date() && a.status !== 'CANCELLED');
  const past = sortedAppointments.filter(a => new Date(a.dateTime) < new Date() || a.status === 'CANCELLED');

  return (
    <div className="max-w-[1000px] mx-auto px-6 py-8 animate-fade-in">
      <div className="sp-page-header flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="sp-page-title">My Appointments</h1>
          <p className="sp-page-subtitle">Manage your clinical visits and check-ups.</p>
        </div>
        <button onClick={() => setShowBookModal(true)} className="sp-btn-primary">
          <Calendar className="w-4 h-4" /> Book Appointment
        </button>
      </div>

      <div className="grid md:grid-cols-2 gap-8">
        {/* Upcoming Appointments */}
        <div>
          <h2 className="text-xl font-bold mb-5" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Upcoming Visits</h2>
          <div className="space-y-4">
            {upcoming.length > 0 ? (
              upcoming.map(appt => (
                <div key={appt.id} className="sp-card p-6" style={{ borderLeft: '4px solid var(--sp-primary)' }}>
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="text-base font-bold leading-tight" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{appt.title}</h3>
                    <span className="sp-badge sp-badge-primary text-[10px] uppercase">{appt.status}</span>
                  </div>
                  <div className="space-y-2 mb-5">
                    <p className="flex items-center text-sm" style={{ color: 'var(--sp-on-surface-var)' }}>
                      <Clock className="w-4 h-4 mr-2" style={{ color: 'var(--sp-primary)' }} /> {new Date(appt.dateTime).toLocaleString()}
                    </p>
                    <p className="flex items-center text-sm" style={{ color: 'var(--sp-on-surface-var)' }}>
                      <User className="w-4 h-4 mr-2" style={{ color: 'var(--sp-primary)' }} /> Dr. {appt.doctor?.user?.fullName || 'Assigned'}
                    </p>
                    {appt.notes && <p className="text-sm italic mt-2" style={{ color: 'var(--sp-outline)' }}>"{appt.notes}"</p>}
                  </div>
                  <div className="pt-4 border-t flex justify-end" style={{ borderColor: 'var(--sp-surface-container)' }}>
                    <button 
                      onClick={() => handleCancel(appt.id)}
                      className="text-sm font-semibold flex items-center transition-colors"
                      style={{ color: 'var(--sp-error)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}
                      onMouseEnter={e => e.currentTarget.style.color = 'var(--sp-on-error-container)'}
                      onMouseLeave={e => e.currentTarget.style.color = 'var(--sp-error)'}
                    >
                      <XCircle className="w-4 h-4 mr-1.5" /> Cancel Visit
                    </button>
                  </div>
                </div>
              ))
            ) : (
              <div className="sp-card flex flex-col items-center justify-center p-8 text-center" style={{ background: 'var(--sp-surface-low)' }}>
                <Calendar className="w-10 h-10 mb-3" style={{ color: 'var(--sp-outline-var)' }} />
                <p className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>No upcoming appointments</p>
              </div>
            )}
          </div>
        </div>

        {/* Past/Cancelled Appointments */}
        <div>
          <h2 className="text-xl font-bold mb-5" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Past & Cancelled</h2>
          <div className="space-y-4">
            {past.length > 0 ? (
              past.map(appt => (
                <div key={appt.id} className="p-6 rounded-xl border opacity-75" 
                     style={{ 
                       background: 'var(--sp-surface-low)',
                       borderColor: 'var(--sp-outline-var)',
                       borderLeft: `4px solid ${appt.status === 'CANCELLED' ? 'var(--sp-error)' : 'var(--sp-outline)'}` 
                     }}>
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="text-base font-bold leading-tight" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif', textDecoration: appt.status === 'CANCELLED' ? 'line-through' : 'none' }}>
                      {appt.title}
                    </h3>
                    <span className="sp-badge text-[10px] uppercase" style={{ background: appt.status === 'CANCELLED' ? 'var(--sp-error-container)' : 'var(--sp-surface-highest)', color: appt.status === 'CANCELLED' ? 'var(--sp-on-error-container)' : 'var(--sp-on-surface-var)' }}>
                      {appt.status}
                    </span>
                  </div>
                  <div className="space-y-2">
                    <p className="flex items-center text-sm" style={{ color: 'var(--sp-on-surface-var)' }}>
                      <Clock className="w-4 h-4 mr-2" style={{ color: 'var(--sp-outline)' }} /> {new Date(appt.dateTime).toLocaleString()}
                    </p>
                    <p className="flex items-center text-sm" style={{ color: 'var(--sp-on-surface-var)' }}>
                      <User className="w-4 h-4 mr-2" style={{ color: 'var(--sp-outline)' }} /> Dr. {appt.doctor?.user?.fullName || 'Assigned'}
                    </p>
                  </div>
                </div>
              ))
            ) : (
              <div className="p-8 rounded-xl border text-center" style={{ background: 'var(--sp-surface-low)', borderColor: 'var(--sp-outline-var)' }}>
                <p className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>No past history</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Book Appointment Modal */}
      {showBookModal && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-4 animate-fade-in"
             style={{ background: 'rgba(17,28,44,0.5)', backdropFilter: 'blur(4px)' }}>
          <div className="w-full max-w-md rounded-2xl p-6" style={{ background: '#fff', boxShadow: '0 24px 80px rgba(0,75,186,0.2)' }}>
            <h3 className="text-xl font-bold mb-5" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Book an Appointment</h3>
            <form onSubmit={handleBookSubmit} className="space-y-4">
              <div>
                <label className="sp-label">Select Doctor</label>
                <select required className="sp-input" value={bookForm.doctorId} onChange={e => setBookForm({...bookForm, doctorId: e.target.value})}>
                  {doctors.map(doc => (
                    <option key={doc.id} value={doc.id}>
                      Dr. {doc.fullName} - {doc.specialization} ({doc.clinicName})
                    </option>
                  ))}
                </select>
              </div>
              
              <div>
                <label className="sp-label">Appointment Title</label>
                <input required type="text" className="sp-input" value={bookForm.title} onChange={e => setBookForm({...bookForm, title: e.target.value})} placeholder="e.g. Initial Consultation" />
              </div>

              <div>
                <label className="sp-label">Date & Time</label>
                <input required type="datetime-local" className="sp-input" value={bookForm.dateTime} onChange={e => setBookForm({...bookForm, dateTime: e.target.value})} />
              </div>

              <div>
                <label className="sp-label">Notes (Optional)</label>
                <textarea rows="2" className="sp-input" value={bookForm.notes} onChange={e => setBookForm({...bookForm, notes: e.target.value})}></textarea>
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setShowBookModal(false)} className="sp-btn-secondary text-sm">Cancel</button>
                <button type="submit" disabled={bookLoading} className="sp-btn-primary text-sm">{bookLoading ? 'Booking...' : 'Confirm Booking'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
