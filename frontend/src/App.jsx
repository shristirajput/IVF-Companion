import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import LandingPage from './components/LandingPage';
import LoginRegister from './components/LoginRegister';
import PatientDashboard from './components/PatientDashboard';
import DoctorDashboard from './components/DoctorDashboard';
import AdminDashboard from './components/AdminDashboard';
import AiRecommendations from './components/AiRecommendations';
import WellnessCommunity from './components/WellnessCommunity';
import AppointmentsPage from './components/AppointmentsPage';
import ReportsPage from './components/ReportsPage';
import ChatWidget from './components/ChatWidget';
import { useAuth } from './context/AuthContext';

// Auth Guard Component
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, loading } = useAuth();
  
  if (loading) {
    return <div className="flex h-screen items-center justify-center dark:text-white">Loading...</div>;
  }
  
  if (!user) {
    return <Navigate to="/auth" />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" />; // Redirect if role is unauthorized
  }

  return children;
};

// Dynamic Dashboard Router based on Role
const DashboardRouter = () => {
  const { user } = useAuth();
  if (user?.role === 'ROLE_PATIENT') return <PatientDashboard />;
  if (user?.role === 'ROLE_DOCTOR') return <DoctorDashboard />;
  if (user?.role === 'ROLE_ADMIN') return <AdminDashboard />;
  return <Navigate to="/" />;
};

function App() {
  return (
    <div className="min-h-screen flex flex-col" style={{ background: 'var(--sp-surface)' }}>
      <Navbar />
      <main className="flex-1 overflow-x-hidden">
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/auth" element={<LoginRegister />} />
          
          <Route 
            path="/dashboard" 
            element={
              <ProtectedRoute>
                <DashboardRouter />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/appointments" 
            element={
              <ProtectedRoute allowedRoles={['ROLE_PATIENT']}>
                <AppointmentsPage />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/reports" 
            element={
              <ProtectedRoute allowedRoles={['ROLE_PATIENT']}>
                <ReportsPage />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/recommendations" 
            element={
              <AiRecommendations /> // Made public so users can try it out, but could be protected.
            } 
          />
          
          <Route 
            path="/community" 
            element={
              <ProtectedRoute>
                <WellnessCommunity />
              </ProtectedRoute>
            } 
          />
          
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </main>
      <ChatWidget />
    </div>
  );
}

export default App;
