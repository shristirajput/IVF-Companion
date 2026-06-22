import React, { useEffect, useState } from 'react';
import api from '../utils/api';
import { useAuth } from '../context/AuthContext';
import { FileText, Download, Activity, AlertCircle } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="px-4 py-3 rounded-xl border" style={{ background: '#fff', borderColor: 'var(--sp-outline-var)', boxShadow: '0 8px 32px rgba(42,100,217,0.12)' }}>
      <p className="text-xs font-semibold mb-1" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{label}</p>
      <p className="text-lg font-bold" style={{ color: 'var(--sp-primary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{payload[0].value} pg/mL</p>
    </div>
  );
};

export default function ReportsPage() {
  const { user } = useAuth();
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [downloading, setDownloading] = useState(false);

  useEffect(() => { fetchLogs(); }, []);

  const fetchLogs = async () => {
    try {
      setLoading(true);
      const res = await api.get('/api/health-logs');
      setLogs(res.data);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };

  const handleDownloadPdf = async () => {
    if (!user?.patientId) {
      alert('Patient profile not found.');
      return;
    }
    try {
      setDownloading(true);
      const res = await api.get(`/api/reports/download/${user.patientId}`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `IVF_Companion_Report_${user.patientId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (e) { alert('Failed to generate PDF report.'); }
    finally { setDownloading(false); }
  };

  if (loading) {
    return (
      <div className="max-w-[1000px] mx-auto px-6 py-8">
        <div className="sp-skeleton h-10 w-64 mb-8 rounded-xl" />
        <div className="sp-skeleton h-[400px] w-full mb-6 rounded-xl" />
        <div className="grid grid-cols-3 gap-6">
          <div className="sp-skeleton h-32 rounded-xl" />
          <div className="sp-skeleton h-32 rounded-xl" />
          <div className="sp-skeleton h-32 rounded-xl" />
        </div>
      </div>
    );
  }

  const chartData = logs.slice().reverse().map(log => ({
    date: new Date(log.date).toLocaleDateString(undefined, { month: 'short', day: 'numeric' }),
    Estrogen: log.hormoneLevel
  })).filter(log => log.Estrogen != null);

  return (
    <div className="max-w-[1000px] mx-auto px-6 py-8 animate-fade-in">
      
      <div className="sp-page-header flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
        <div>
          <h1 className="sp-page-title flex items-center gap-3">
            <div className="w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0" style={{ background: 'var(--sp-primary-container)' }}>
              <FileText className="w-6 h-6" style={{ color: 'var(--sp-primary)' }} />
            </div>
            Medical Reports
          </h1>
          <p className="sp-page-subtitle mt-2">View your hormone trends and export your clinical summary.</p>
        </div>
        
        <button
          onClick={handleDownloadPdf}
          disabled={downloading}
          className="sp-btn-primary px-6 h-12 text-sm disabled:opacity-75 disabled:transform-none"
        >
          {downloading ? (
            <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
          ) : (
            <Download className="w-4 h-4 mr-2" />
          )}
          {downloading ? 'Generating PDF...' : 'Download Full PDF'}
        </button>
      </div>

      <div className="grid grid-cols-1 gap-6">
        
        {/* Analytics Chart */}
        <div className="sp-card p-6 md:p-8 animate-fade-up">
          <div className="flex items-center gap-3 mb-6">
            <div className="sp-icon-container sp-icon-primary"><Activity className="w-5 h-5" /></div>
            <div>
              <h2 className="text-lg font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Estrogen Trend Analysis</h2>
              <p className="text-xs" style={{ color: 'var(--sp-outline)' }}>E2 measured in pg/mL</p>
            </div>
          </div>
          
          <div className="h-80 w-full relative z-10">
            {chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorE2" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#004bba" stopOpacity={0.2}/>
                      <stop offset="95%" stopColor="#004bba" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e7eeff" />
                  <XAxis dataKey="date" axisLine={false} tickLine={false} tick={{ fill: '#737785', fontSize: 11 }} dy={10} />
                  <YAxis axisLine={false} tickLine={false} tick={{ fill: '#737785', fontSize: 11 }} dx={-10} />
                  <Tooltip content={<CustomTooltip />} />
                  <Area type="monotone" dataKey="Estrogen" stroke="#004bba" strokeWidth={3} fillOpacity={1} fill="url(#colorE2)" activeDot={{ r: 6, fill: '#004bba', stroke: '#fff', strokeWidth: 2 }} />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex flex-col items-center justify-center text-center rounded-xl" style={{ background: 'var(--sp-surface-low)' }}>
                <AlertCircle className="w-10 h-10 mb-3" style={{ color: 'var(--sp-outline-var)' }} />
                <p className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Not enough data</p>
                <p className="text-xs mt-1" style={{ color: 'var(--sp-outline)' }}>Log hormone levels to generate chart.</p>
              </div>
            )}
          </div>
        </div>

        {/* Data Summary */}
        <div className="grid grid-cols-2 md:grid-cols-3 gap-6 animate-fade-up" style={{ animationDelay: '0.1s' }}>
          <div className="sp-card p-6 flex flex-col items-center justify-center text-center">
            <p className="text-xs font-bold uppercase tracking-widest mb-2" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Total Logs</p>
            <p className="text-4xl font-bold" style={{ color: 'var(--sp-secondary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{logs.length}</p>
          </div>
          <div className="sp-card p-6 flex flex-col items-center justify-center text-center">
            <p className="text-xs font-bold uppercase tracking-widest mb-2" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Latest E2</p>
            <p className="text-4xl font-bold" style={{ color: 'var(--sp-error)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
              {chartData.length > 0 ? chartData[chartData.length - 1].Estrogen : '--'}
            </p>
          </div>
          <div className="sp-card p-6 flex flex-col items-center justify-center text-center col-span-2 md:col-span-1">
            <p className="text-xs font-bold uppercase tracking-widest mb-2" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Days Tracked</p>
            <p className="text-4xl font-bold" style={{ color: 'var(--sp-primary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
              {logs.length > 0 ? new Set(logs.map(l => l.date)).size : 0}
            </p>
          </div>
        </div>

      </div>
    </div>
  );
}
