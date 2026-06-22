import React, { useState } from 'react';
import api from '../utils/api';
import { Activity, Brain, ArrowRight, CheckCircle2, AlertCircle, Pill, ShieldCheck, Apple, TrendingUp, ChevronRight } from 'lucide-react';

export default function AiRecommendations() {
  const [formData, setFormData] = useState({
    age: '', amhLevel: '', fshLevel: '', yearsInfertility: '', prevCycles: '', diagnosis: 'UNEXPLAINED'
  });
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    setResult(null);
    try {
      const payload = {
        age: parseInt(formData.age, 10),
        amhLevel: parseFloat(formData.amhLevel),
        fshLevel: parseFloat(formData.fshLevel),
        yearsInfertility: parseInt(formData.yearsInfertility, 10),
        prevCycles: parseInt(formData.prevCycles, 10),
        diagnosis: formData.diagnosis
      };
      const res = await api.post('/api/recommendations/analyze', payload);
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to process clinical rule analysis.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-[1200px] mx-auto px-6 py-8 animate-fade-in">
      
      {/* Header */}
      <div className="mb-10 text-center">
        <div className="w-16 h-16 mx-auto rounded-2xl flex items-center justify-center mb-5"
             style={{ background: 'var(--sp-primary-container)' }}>
          <Brain className="w-8 h-8" style={{ color: 'var(--sp-on-primary-container)' }} />
        </div>
        <h1 className="text-3xl font-bold tracking-tight mb-3" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
          Clinical AI Engine
        </h1>
        <p className="max-w-2xl mx-auto text-base" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Atkinson Hyperlegible Next", sans-serif' }}>
          Evaluate patient parameters through our rule-based inference engine to suggest the most statistically optimal treatment path.
        </p>
      </div>

      <div className="grid lg:grid-cols-2 gap-8">
        
        {/* Form Panel */}
        <div className="sp-card p-6 md:p-8 flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-3 mb-6 border-b pb-4" style={{ borderColor: 'var(--sp-outline-var)' }}>
              <div className="sp-icon-container sp-icon-primary"><Activity className="w-5 h-5" /></div>
              <h2 className="text-lg font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Patient Parameters</h2>
            </div>
            
            {error && (
              <div className="mb-6 p-4 rounded-xl flex items-start gap-3 animate-fade-in" style={{ background: 'var(--sp-error-container)', color: 'var(--sp-on-error-container)' }}>
                <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
                <p className="text-sm font-medium" style={{ fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{error}</p>
              </div>
            )}

            <form id="ai-form" onSubmit={handleSubmit} className="space-y-5">
              <div className="grid grid-cols-2 gap-5">
                <div>
                  <label className="sp-label">Age</label>
                  <input required type="number" name="age" min="18" max="60" value={formData.age} onChange={handleChange} className="sp-input" placeholder="e.g. 32" />
                </div>
                <div>
                  <label className="sp-label">Primary Diagnosis</label>
                  <select name="diagnosis" value={formData.diagnosis} onChange={handleChange} className="sp-input">
                    <option value="UNEXPLAINED">Unexplained</option>
                    <option value="PCOS">PCOS</option>
                    <option value="ENDOMETRIOSIS">Endometriosis</option>
                    <option value="MALE_FACTOR">Male Factor</option>
                    <option value="TUBAL_FACTOR">Tubal Factor</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-5">
                <div>
                  <label className="sp-label">AMH Level (ng/mL)</label>
                  <input required type="number" step="0.1" name="amhLevel" value={formData.amhLevel} onChange={handleChange} className="sp-input" placeholder="e.g. 2.4" />
                </div>
                <div>
                  <label className="sp-label">FSH Level (mIU/mL)</label>
                  <input required type="number" step="0.1" name="fshLevel" value={formData.fshLevel} onChange={handleChange} className="sp-input" placeholder="e.g. 6.8" />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-5">
                <div>
                  <label className="sp-label">Years of Infertility</label>
                  <input required type="number" name="yearsInfertility" min="0" value={formData.yearsInfertility} onChange={handleChange} className="sp-input" placeholder="e.g. 2" />
                </div>
                <div>
                  <label className="sp-label">Previous IVF Cycles</label>
                  <input required type="number" name="prevCycles" min="0" value={formData.prevCycles} onChange={handleChange} className="sp-input" placeholder="e.g. 0" />
                </div>
              </div>
            </form>
          </div>
          
          <button 
            type="submit" 
            form="ai-form"
            disabled={loading}
            className="sp-btn-primary w-full mt-8"
          >
            {loading ? 'Evaluating Parameters...' : <>Run AI Analysis <ArrowRight className="w-4 h-4 ml-1" /></>}
          </button>
        </div>

        {/* Results Panel */}
        <div className="sp-card p-6 md:p-8 flex flex-col justify-center min-h-[500px]">
          {!result && !loading && (
            <div className="text-center animate-fade-in">
              <Brain className="w-16 h-16 mx-auto mb-4" style={{ color: 'var(--sp-surface-highest)' }} />
              <p className="text-sm font-semibold" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Awaiting Parameters</p>
              <p className="text-xs mt-1" style={{ color: 'var(--sp-outline)' }}>Submit the form to generate a protocol.</p>
            </div>
          )}
          
          {loading && (
            <div className="text-center animate-fade-in">
              <div className="w-12 h-12 border-4 border-t-transparent rounded-full animate-spin mx-auto mb-4" style={{ borderColor: 'var(--sp-primary) transparent transparent transparent' }} />
              <p className="text-sm font-bold" style={{ color: 'var(--sp-primary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Evaluating clinical rules...</p>
            </div>
          )}

          {result && !loading && (
            <div className="animate-fade-up h-full flex flex-col">
              <div className="flex justify-between items-center mb-6">
                <h3 className="text-xl font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Analysis Results</h3>
                <span className="sp-badge flex items-center gap-1.5" style={{ background: '#e6f7ec', color: '#007a4d' }}>
                  <CheckCircle2 className="w-3.5 h-3.5" /> High Confidence
                </span>
              </div>
              
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
                
                {/* Protocol */}
                <div className="p-4 rounded-xl" style={{ background: 'var(--sp-surface-low)', border: '1px solid var(--sp-outline-var)' }}>
                  <div className="flex items-center gap-2 mb-3">
                    <div className="w-6 h-6 rounded flex items-center justify-center" style={{ background: 'var(--sp-primary-fixed)' }}>
                      <Activity className="w-3.5 h-3.5" style={{ color: 'var(--sp-on-primary-fixed)' }} />
                    </div>
                    <span className="text-[11px] font-bold uppercase tracking-widest" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Protocol</span>
                  </div>
                  <h4 className="text-lg font-bold mb-3" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{result.recommendedProtocol}</h4>
                  <div className="flex items-center justify-between text-xs mb-1.5">
                    <span style={{ color: 'var(--sp-outline)' }}>Confidence Match</span>
                    <span className="font-bold" style={{ color: 'var(--sp-primary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{result.confidenceScore}%</span>
                  </div>
                  <div className="sp-progress h-1.5"><div className="sp-progress-fill" style={{ width: `${result.confidenceScore}%` }} /></div>
                </div>

                {/* Risk Level */}
                <div className="p-4 rounded-xl" style={{ background: 'var(--sp-surface-low)', border: '1px solid var(--sp-outline-var)' }}>
                  <div className="flex items-center gap-2 mb-3">
                    <div className="w-6 h-6 rounded flex items-center justify-center" style={{ background: '#e6f7ec' }}>
                      <ShieldCheck className="w-3.5 h-3.5" style={{ color: '#007a4d' }} />
                    </div>
                    <span className="text-[11px] font-bold uppercase tracking-widest" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Risk Level</span>
                  </div>
                  <div className="flex items-center gap-2 mb-2">
                    <h4 className="text-base font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Low OHSS Risk</h4>
                  </div>
                  <p className="text-xs" style={{ color: 'var(--sp-outline)' }}>Based on AMH levels and age profile.</p>
                </div>

                {/* Meds */}
                <div className="p-4 rounded-xl" style={{ background: 'var(--sp-surface-low)', border: '1px solid var(--sp-outline-var)' }}>
                  <div className="flex items-center gap-2 mb-3">
                    <div className="w-6 h-6 rounded flex items-center justify-center" style={{ background: 'var(--sp-error-container)' }}>
                      <Pill className="w-3.5 h-3.5" style={{ color: 'var(--sp-error)' }} />
                    </div>
                    <span className="text-[11px] font-bold uppercase tracking-widest" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Suggested Meds</span>
                  </div>
                  <h4 className="text-base font-bold mb-1" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Gonal-F 150 IU</h4>
                  <p className="text-xs" style={{ color: 'var(--sp-outline)' }}>Starting dose recommendation.</p>
                </div>

                {/* Lifestyle */}
                <div className="p-4 rounded-xl" style={{ background: 'var(--sp-surface-low)', border: '1px solid var(--sp-outline-var)' }}>
                  <div className="flex items-center gap-2 mb-3">
                    <div className="w-6 h-6 rounded flex items-center justify-center" style={{ background: 'var(--sp-surface-highest)' }}>
                      <Apple className="w-3.5 h-3.5" style={{ color: 'var(--sp-on-surface-var)' }} />
                    </div>
                    <span className="text-[11px] font-bold uppercase tracking-widest" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Lifestyle</span>
                  </div>
                  <ul className="text-xs space-y-1.5" style={{ color: 'var(--sp-on-surface)' }}>
                    <li className="flex items-center gap-1.5"><span className="w-1 h-1 rounded-full bg-current" /> High protein diet</li>
                    <li className="flex items-center gap-1.5"><span className="w-1 h-1 rounded-full bg-current" /> Hydration (3L/day)</li>
                    <li className="flex items-center gap-1.5"><span className="w-1 h-1 rounded-full bg-current" /> Sleep tracking</li>
                  </ul>
                </div>
              </div>

              {/* Rationale */}
              <div className="p-5 rounded-xl border mt-auto" style={{ background: 'var(--sp-surface)', borderColor: 'var(--sp-surface-container)' }}>
                <h4 className="text-sm font-bold flex items-center gap-2 mb-4" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
                  <TrendingUp className="w-4 h-4" style={{ color: 'var(--sp-primary)' }} /> Clinical Rationale
                </h4>
                <ul className="space-y-2.5">
                  {result.rationalePoints.map((point, idx) => (
                    <li key={idx} className="flex items-start gap-2.5">
                      <div className="w-1.5 h-1.5 rounded-full mt-1.5 flex-shrink-0" style={{ background: 'var(--sp-primary-container)' }} />
                      <p className="text-xs leading-relaxed" style={{ color: 'var(--sp-on-surface-var)' }}>{point}</p>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
