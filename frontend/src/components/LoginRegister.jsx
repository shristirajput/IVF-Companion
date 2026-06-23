import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LogIn, UserPlus, Mail, Lock, User, Calendar, AlertCircle, Stethoscope, ShieldCheck, Zap } from 'lucide-react';

const DEMO_ACCOUNTS = [
  {
    label: 'Doctor',
    username: 'dr_smith',
    password: 'password',
    name: 'Dr. Sarah Smith',
    icon: Stethoscope,
    color: 'var(--sp-secondary)',
    bg: 'rgba(0,105,112,0.08)',
    border: 'rgba(0,105,112,0.25)',
  },
  {
    label: 'Admin',
    username: 'admin',
    password: 'password',
    name: 'System Administrator',
    icon: ShieldCheck,
    color: 'var(--sp-primary)',
    bg: 'rgba(0,75,186,0.08)',
    border: 'rgba(0,75,186,0.25)',
  },
];

export default function LoginRegister() {
  const [isLogin, setIsLogin] = useState(true);
  const [role, setRole] = useState('PATIENT');
  const [error, setError] = useState('');
  const [loadingLocal, setLoadingLocal] = useState(false);
  const [demoLoading, setDemoLoading] = useState(null);

  const { login, register } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    username: '', password: '', email: '', fullName: '',
    dateOfBirth: '', amhLevel: '', fshLevel: '',
    specialization: '', licenseNumber: '', clinicName: ''
  });

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleDemoLogin = async (account) => {
    setError('');
    setDemoLoading(account.label);
    const result = await login(account.username, account.password);
    if (result.success) {
      navigate('/dashboard');
    } else {
      setError(result.message || 'Demo login failed. Please ensure the backend is running and the database seed data is loaded.');
    }
    setDemoLoading(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoadingLocal(true);

    if (isLogin) {
      const result = await login(formData.username, formData.password);
      if (result.success) {
        navigate('/dashboard');
      } else {
        setError(result.message);
      }
    } else {
      const payload = {
        username: formData.username, password: formData.password,
        email: formData.email, fullName: formData.fullName, role: role
      };
      if (role === 'PATIENT') {
        payload.dateOfBirth = formData.dateOfBirth;
        payload.amhLevel = parseFloat(formData.amhLevel);
        payload.fshLevel = parseFloat(formData.fshLevel);
      } else if (role === 'DOCTOR') {
        payload.specialization = formData.specialization;
        payload.licenseNumber = formData.licenseNumber;
        payload.clinicName = formData.clinicName;
      }
      const result = await register(payload);
      if (result.success) {
        setIsLogin(true);
        setError('Registration successful! Please log in.');
      } else {
        setError(result.message);
      }
    }
    setLoadingLocal(false);
  };

  return (
    <div className="min-h-screen flex items-center justify-center py-12 px-6 animate-fade-in" style={{ background: 'var(--sp-surface)' }}>
      <div className="max-w-md w-full sp-card p-8 md:p-10 shadow-elevated">

        {/* Header */}
        <div className="text-center mb-8">
          <div className="w-12 h-12 rounded-xl flex items-center justify-center mx-auto mb-4" style={{ background: 'var(--sp-primary-container)' }}>
            {isLogin ? <LogIn className="w-6 h-6" style={{ color: 'var(--sp-primary)' }} /> : <UserPlus className="w-6 h-6" style={{ color: 'var(--sp-primary)' }} />}
          </div>
          <h2 className="text-2xl font-bold tracking-tight" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
            {isLogin ? 'Welcome back' : 'Create an account'}
          </h2>
          <p className="mt-2 text-sm" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Atkinson Hyperlegible Next", sans-serif' }}>
            {isLogin ? 'Sign in to your IVF Companion dashboard' : 'Join IVF Companion today'}
          </p>
        </div>

        {/* ── Quick Demo Login Buttons ── */}
        {isLogin && (
          <div className="mb-6 animate-fade-in">
            <div className="flex items-center gap-2 mb-3">
              <Zap className="w-3.5 h-3.5" style={{ color: 'var(--sp-outline)' }} />
              <span className="text-xs font-semibold uppercase tracking-widest" style={{ color: 'var(--sp-outline)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
                Quick Demo Access
              </span>
            </div>
            <div className="grid grid-cols-2 gap-3">
              {DEMO_ACCOUNTS.map((acc) => {
                const Icon = acc.icon;
                const isThis = demoLoading === acc.label;
                return (
                  <button
                    key={acc.label}
                    type="button"
                    onClick={() => handleDemoLogin(acc)}
                    disabled={demoLoading !== null}
                    className="flex items-center gap-3 p-3.5 rounded-xl text-left transition-all hover:scale-[1.02] active:scale-[0.98]"
                    style={{
                      background: acc.bg,
                      border: `1.5px solid ${acc.border}`,
                      opacity: demoLoading && !isThis ? 0.5 : 1,
                      cursor: demoLoading ? 'not-allowed' : 'pointer',
                    }}
                  >
                    <div className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0" style={{ background: acc.color }}>
                      {isThis
                        ? <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin block" />
                        : <Icon className="w-4 h-4 text-white" />
                      }
                    </div>
                    <div className="min-w-0">
                      <p className="text-xs font-bold truncate" style={{ color: acc.color, fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
                        {isThis ? 'Signing in…' : `Login as ${acc.label}`}
                      </p>
                      <p className="text-xs truncate" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
                        {acc.name}
                      </p>
                    </div>
                  </button>
                );
              })}
            </div>

            <div className="flex items-center gap-3 my-5">
              <div className="flex-1 h-px" style={{ background: 'var(--sp-surface-container)' }} />
              <span className="text-xs font-semibold" style={{ color: 'var(--sp-outline)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>or sign in manually</span>
              <div className="flex-1 h-px" style={{ background: 'var(--sp-surface-container)' }} />
            </div>
          </div>
        )}

        {/* Error / Success Banner */}
        {error && (
          <div className="mb-6 p-4 rounded-xl flex items-start gap-3 animate-fade-in"
               style={{
                 background: error.includes('successful') ? '#e6f7ec' : 'var(--sp-error-container)',
                 color: error.includes('successful') ? '#007a4d' : 'var(--sp-on-error-container)'
               }}>
            <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
            <p className="text-sm font-medium" style={{ fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{error}</p>
          </div>
        )}

        {/* Sign In / Sign Up Toggle */}
        <div className="flex p-1 rounded-xl mb-8" style={{ background: 'var(--sp-surface-low)' }}>
          <button
            className="flex-1 py-2.5 text-sm font-bold rounded-lg transition-all"
            style={{
              background: isLogin ? '#fff' : 'transparent',
              color: isLogin ? 'var(--sp-primary)' : 'var(--sp-on-surface-var)',
              boxShadow: isLogin ? '0 2px 8px rgba(0,0,0,0.05)' : 'none',
              fontFamily: '"Plus Jakarta Sans", sans-serif'
            }}
            onClick={() => { setIsLogin(true); setError(''); }}
          >
            Sign In
          </button>
          <button
            className="flex-1 py-2.5 text-sm font-bold rounded-lg transition-all"
            style={{
              background: !isLogin ? '#fff' : 'transparent',
              color: !isLogin ? 'var(--sp-primary)' : 'var(--sp-on-surface-var)',
              boxShadow: !isLogin ? '0 2px 8px rgba(0,0,0,0.05)' : 'none',
              fontFamily: '"Plus Jakarta Sans", sans-serif'
            }}
            onClick={() => { setIsLogin(false); setError(''); }}
          >
            Sign Up
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-5">

          <div>
            <label className="sp-label">Username</label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                <User className="w-4 h-4" style={{ color: 'var(--sp-outline)' }} />
              </div>
              <input required type="text" name="username" value={formData.username} onChange={handleChange} className="sp-input pl-10" placeholder="Username" />
            </div>
          </div>

          {!isLogin && (
            <>
              <div>
                <label className="sp-label">Email Address</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                    <Mail className="w-4 h-4" style={{ color: 'var(--sp-outline)' }} />
                  </div>
                  <input required type="email" name="email" value={formData.email} onChange={handleChange} className="sp-input pl-10" placeholder="you@example.com" />
                </div>
              </div>
              <div>
                <label className="sp-label">Full Name</label>
                <input required type="text" name="fullName" value={formData.fullName} onChange={handleChange} className="sp-input" placeholder="Jane Doe" />
              </div>
            </>
          )}

          <div>
            <label className="sp-label">Password</label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                <Lock className="w-4 h-4" style={{ color: 'var(--sp-outline)' }} />
              </div>
              <input required type="password" name="password" value={formData.password} onChange={handleChange} className="sp-input pl-10" placeholder="••••••••" />
            </div>
          </div>

          {!isLogin && (
            <div className="pt-2">
              <label className="sp-label mb-3">I am a...</label>
              <div className="flex gap-4">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input type="radio" value="PATIENT" checked={role === 'PATIENT'} onChange={() => setRole('PATIENT')}
                         className="w-4 h-4" style={{ accentColor: 'var(--sp-primary)' }} />
                  <span className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Patient</span>
                </label>
                <label className="flex items-center gap-2 cursor-pointer">
                  <input type="radio" value="DOCTOR" checked={role === 'DOCTOR'} onChange={() => setRole('DOCTOR')}
                         className="w-4 h-4" style={{ accentColor: 'var(--sp-primary)' }} />
                  <span className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Doctor</span>
                </label>
              </div>
            </div>
          )}

          {/* Patient Fields */}
          {!isLogin && role === 'PATIENT' && (
            <div className="space-y-4 pt-4 border-t animate-fade-in" style={{ borderColor: 'var(--sp-surface-container)' }}>
              <div>
                <label className="sp-label">Date of Birth</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                    <Calendar className="w-4 h-4" style={{ color: 'var(--sp-outline)' }} />
                  </div>
                  <input required type="date" name="dateOfBirth" value={formData.dateOfBirth} onChange={handleChange} className="sp-input pl-10" />
                </div>
              </div>
              <div className="flex gap-4">
                <div className="flex-1">
                  <label className="sp-label">AMH Level</label>
                  <input required type="number" step="0.1" name="amhLevel" value={formData.amhLevel} onChange={handleChange} className="sp-input" placeholder="e.g. 2.5" />
                </div>
                <div className="flex-1">
                  <label className="sp-label">FSH Level</label>
                  <input required type="number" step="0.1" name="fshLevel" value={formData.fshLevel} onChange={handleChange} className="sp-input" placeholder="e.g. 6.8" />
                </div>
              </div>
            </div>
          )}

          {/* Doctor Fields */}
          {!isLogin && role === 'DOCTOR' && (
            <div className="space-y-4 pt-4 border-t animate-fade-in" style={{ borderColor: 'var(--sp-surface-container)' }}>
              <div>
                <label className="sp-label">Specialization</label>
                <input required type="text" name="specialization" value={formData.specialization} onChange={handleChange} className="sp-input" placeholder="e.g. Endocrinology" />
              </div>
              <div>
                <label className="sp-label">License Number</label>
                <input required type="text" name="licenseNumber" value={formData.licenseNumber} onChange={handleChange} className="sp-input" placeholder="MD12345" />
              </div>
              <div>
                <label className="sp-label">Clinic Name</label>
                <input required type="text" name="clinicName" value={formData.clinicName} onChange={handleChange} className="sp-input" placeholder="Fertility Center" />
              </div>
            </div>
          )}

          <button type="submit" disabled={loadingLocal} className="sp-btn-primary w-full h-12 mt-4 text-base">
            {loadingLocal ? 'Processing...' : isLogin ? 'Sign In' : 'Create Account'}
          </button>
        </form>
      </div>
    </div>
  );
}
