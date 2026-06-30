import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard, Calendar, Brain, Users, FileText,
  LogOut, Menu, X, Bell, ChevronDown, CheckCircle2, Clock
} from 'lucide-react';
import api from '../utils/api';

const NAV_LINKS = [
  { href: '/dashboard',       label: 'Dashboard',    icon: LayoutDashboard, roles: ['ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_ADMIN'] },
  { href: '/appointments',    label: 'Appointments', icon: Calendar,        roles: ['ROLE_PATIENT'] },
  { href: '/recommendations', label: 'AI Engine',    icon: Brain,           roles: ['ROLE_PATIENT', 'ROLE_DOCTOR'] },
  { href: '/community',       label: 'Community',    icon: Users,           roles: ['ROLE_PATIENT', 'ROLE_DOCTOR'] },
  { href: '/reports',         label: 'Reports',      icon: FileText,        roles: ['ROLE_PATIENT'] },
];

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    if (user) {
      fetchNotifications();
    }
  }, [user]);

  const fetchNotifications = async () => {
    try {
      const res = await api.get('/api/notifications');
      setNotifications(res.data);
    } catch (e) {
      console.error('Failed to fetch notifications:', e);
    }
  };

  const handleMarkAsRead = async (id) => {
    try {
      await api.put(`/api/notifications/${id}/read`);
      setNotifications(notifications.map(n => n.id === id ? { ...n, read: true } : n));
    } catch (e) {
      console.error('Failed to mark notification as read:', e);
    }
  };

  const unreadCount = notifications.filter(n => !n.read).length;

  const handleLogout = () => {
    logout();
    navigate('/');
    setProfileOpen(false);
  };

  const visibleLinks = user
    ? NAV_LINKS.filter(l => l.roles.includes(user.role))
    : [];

  const roleLabel = {
    ROLE_PATIENT: 'Patient',
    ROLE_DOCTOR:  'Physician',
    ROLE_ADMIN:   'Admin',
  }[user?.role] ?? '';

  const initials = user?.fullName
    ? user.fullName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
    : '?';

  return (
    <>
      <header className="sp-nav sticky top-0 z-50">
        <div className="max-w-[1200px] mx-auto px-6 h-16 flex items-center justify-between gap-6">

          {/* Logo */}
          <Link to="/" className="flex items-center gap-2.5 flex-shrink-0">
            <div className="w-8 h-8 rounded-lg flex items-center justify-center"
                 style={{ background: 'var(--sp-primary)' }}>
              <span className="text-white font-bold text-sm font-display">IVF</span>
            </div>
            <span className="font-bold text-lg tracking-tight hidden sm:block"
                  style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
              IVF <span style={{ color: 'var(--sp-primary)' }}>Companion</span>
            </span>
          </Link>

          {/* Desktop Nav Links */}
          {user && (
            <nav className="hidden md:flex items-center gap-1 flex-1 justify-center">
              {visibleLinks.map(({ href, label, icon: Icon }) => {
                const active = location.pathname === href;
                return (
                  <Link
                    key={href}
                    to={href}
                    className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all duration-200"
                    style={{
                      fontFamily: '"Plus Jakarta Sans", sans-serif',
                      background:  active ? 'var(--sp-primary-fixed)' : 'transparent',
                      color:       active ? 'var(--sp-primary)'       : 'var(--sp-on-surface-var)',
                    }}
                    onMouseEnter={e => {
                      if (!active) {
                        e.currentTarget.style.background = 'var(--sp-surface-container)';
                        e.currentTarget.style.color = 'var(--sp-on-surface)';
                      }
                    }}
                    onMouseLeave={e => {
                      if (!active) {
                        e.currentTarget.style.background = 'transparent';
                        e.currentTarget.style.color = 'var(--sp-on-surface-var)';
                      }
                    }}
                  >
                    <Icon className="w-4 h-4" />
                    {label}
                  </Link>
                );
              })}
            </nav>
          )}

          {/* Right Side */}
          <div className="flex items-center gap-3 flex-shrink-0">
            {user ? (
              <>
                {/* Notification bell */}
                <div className="relative">
                  <button
                    onClick={() => { setNotifOpen(!notifOpen); setProfileOpen(false); }}
                    className="relative w-10 h-10 rounded-lg flex items-center justify-center transition-colors"
                    style={{ background: notifOpen ? 'var(--sp-surface-highest)' : 'var(--sp-surface-container)' }}
                    title="Notifications"
                  >
                    <Bell className="w-5 h-5" style={{ color: 'var(--sp-on-surface-var)' }} />
                    {unreadCount > 0 && (
                      <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full" style={{ background: 'var(--sp-error)' }} />
                    )}
                  </button>

                  {notifOpen && (
                    <div className="absolute right-0 top-full mt-2 w-80 max-h-96 overflow-y-auto rounded-xl border py-2 animate-fade-in z-50 shadow-elevated"
                         style={{ background: '#fff', borderColor: 'var(--sp-outline-var)' }}>
                      <div className="px-4 py-2.5 border-b flex justify-between items-center" style={{ borderColor: 'var(--sp-surface-container)' }}>
                        <h4 className="text-sm font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Notifications</h4>
                        {unreadCount > 0 && (
                          <span className="text-xs font-semibold px-2 py-0.5 rounded-full" style={{ background: 'var(--sp-error-container)', color: 'var(--sp-on-error-container)' }}>
                            {unreadCount} new
                          </span>
                        )}
                      </div>
                      <div className="flex flex-col">
                        {notifications.length === 0 ? (
                          <div className="py-8 text-center">
                            <Bell className="w-8 h-8 mx-auto mb-2 opacity-20" />
                            <p className="text-sm" style={{ color: 'var(--sp-outline)' }}>You're all caught up!</p>
                          </div>
                        ) : (
                          notifications.map(notif => (
                            <div key={notif.id} className="px-4 py-3 border-b last:border-0 hover:bg-slate-50 transition-colors relative"
                                 style={{ borderColor: 'var(--sp-surface-container)' }}>
                              <div className="flex gap-3">
                                <div className="mt-0.5">
                                  {notif.type === 'APPOINTMENT' ? <Calendar className="w-4 h-4 text-blue-500" /> :
                                   notif.type === 'REMINDER' ? <Clock className="w-4 h-4 text-amber-500" /> :
                                   <Bell className="w-4 h-4 text-slate-400" />}
                                </div>
                                <div className="flex-1 min-w-0">
                                  <p className={`text-sm ${!notif.read ? 'font-bold' : ''}`} style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
                                    {notif.message}
                                  </p>
                                  <p className="text-xs mt-1" style={{ color: 'var(--sp-outline)' }}>
                                    {new Date(notif.createdAt).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' })}
                                  </p>
                                </div>
                                {!notif.read && (
                                  <button onClick={() => handleMarkAsRead(notif.id)} className="flex-shrink-0 text-slate-400 hover:text-blue-500 transition-colors" title="Mark as read">
                                    <CheckCircle2 className="w-4 h-4" />
                                  </button>
                                )}
                              </div>
                              {!notif.read && (
                                <div className="absolute left-0 top-0 bottom-0 w-1" style={{ background: 'var(--sp-primary)' }} />
                              )}
                            </div>
                          ))
                        )}
                      </div>
                    </div>
                  )}
                </div>

                {/* Profile dropdown */}
                <div className="relative">
                  <button
                    onClick={() => { setProfileOpen(o => !o); setNotifOpen(false); }}
                    className="flex items-center gap-2.5 px-3 py-1.5 rounded-lg border transition-all"
                    style={{
                      borderColor: profileOpen ? 'var(--sp-primary)' : 'var(--sp-outline-var)',
                      background: 'var(--sp-surface-lowest)',
                    }}
                  >
                    <div className="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold"
                         style={{ background: 'var(--sp-primary-container)', color: 'var(--sp-on-primary)' }}>
                      {initials}
                    </div>
                    <div className="hidden sm:block text-left">
                      <p className="text-sm font-semibold leading-none" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
                        {user.fullName?.split(' ')[0] || user.username}
                      </p>
                      <p className="text-xs mt-0.5" style={{ color: 'var(--sp-outline)' }}>{roleLabel}</p>
                    </div>
                    <ChevronDown className="w-4 h-4 hidden sm:block" style={{ color: 'var(--sp-outline)', transform: profileOpen ? 'rotate(180deg)' : 'none', transition: 'transform 0.2s' }} />
                  </button>

                  {profileOpen && (
                    <div className="absolute right-0 top-full mt-2 w-56 rounded-xl border py-2 animate-fade-in z-50"
                         style={{ background: '#fff', borderColor: 'var(--sp-outline-var)', boxShadow: '0 8px 40px rgba(0,75,186,0.12)' }}>
                      <div className="px-4 py-3 border-b" style={{ borderColor: 'var(--sp-surface-container)' }}>
                        <p className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{user.fullName}</p>
                        <p className="text-xs mt-0.5" style={{ color: 'var(--sp-outline)' }}>{user.email}</p>
                      </div>
                      <button
                        onClick={handleLogout}
                        className="w-full flex items-center gap-3 px-4 py-2.5 text-sm font-medium transition-colors"
                        style={{ color: 'var(--sp-error)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}
                        onMouseEnter={e => e.currentTarget.style.background = 'var(--sp-error-container)'}
                        onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
                      >
                        <LogOut className="w-4 h-4" />
                        Sign Out
                      </button>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div className="flex items-center gap-3">
                <Link to="/auth" className="sp-btn-secondary text-sm py-2 px-5">Log in</Link>
                <Link to="/auth" className="sp-btn-primary text-sm py-2 px-5">Get Started</Link>
              </div>
            )}

            {/* Mobile hamburger */}
            {user && (
              <button
                onClick={() => setMobileOpen(o => !o)}
                className="md:hidden w-10 h-10 rounded-lg flex items-center justify-center"
                style={{ background: 'var(--sp-surface-container)' }}
              >
                {mobileOpen ? <X className="w-5 h-5" style={{ color: 'var(--sp-on-surface)' }} /> : <Menu className="w-5 h-5" style={{ color: 'var(--sp-on-surface)' }} />}
              </button>
            )}
          </div>
        </div>

        {/* Mobile menu */}
        {mobileOpen && user && (
          <div className="md:hidden border-t px-4 py-3 space-y-1 animate-fade-in"
               style={{ borderColor: 'var(--sp-outline-var)', background: '#fff' }}>
            {visibleLinks.map(({ href, label, icon: Icon }) => {
              const active = location.pathname === href;
              return (
                <Link
                  key={href}
                  to={href}
                  onClick={() => setMobileOpen(false)}
                  className="flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-semibold transition-colors"
                  style={{
                    fontFamily: '"Plus Jakarta Sans", sans-serif',
                    background: active ? 'var(--sp-primary-fixed)' : 'transparent',
                    color:      active ? 'var(--sp-primary)'       : 'var(--sp-on-surface-var)',
                  }}
                >
                  <Icon className="w-5 h-5" />
                  {label}
                </Link>
              );
            })}
            <button
              onClick={handleLogout}
              className="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-semibold transition-colors"
              style={{ color: 'var(--sp-error)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}
            >
              <LogOut className="w-5 h-5" />
              Sign Out
            </button>
          </div>
        )}
      </header>
    </>
  );
}
