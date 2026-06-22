import React from 'react';
import { Link } from 'react-router-dom';
import { Heart, Activity, Calendar, Brain, Smile, Users, ArrowRight, CheckCircle2 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const LandingPage = () => {
  const { user } = useAuth();

  return (
    <div className="min-h-screen bg-slate-50 transition-colors duration-300 overflow-x-hidden" style={{ background: 'var(--sp-surface)' }}>
      
      {/* ── Hero Section ── */}
      <section className="relative pt-20 pb-28 sm:pt-28 sm:pb-36 lg:pt-36">
        {/* Abstract shapes */}
        <div className="absolute inset-0 pointer-events-none overflow-hidden flex justify-center z-0">
          <div className="absolute -top-[20%] w-[800px] h-[800px] rounded-full opacity-40 blur-[120px]" style={{ background: 'var(--sp-primary-fixed)' }} />
          <div className="absolute top-[30%] -right-[10%] w-[600px] h-[600px] rounded-full opacity-30 blur-[100px]" style={{ background: 'var(--sp-secondary-container)' }} />
        </div>

        <div className="max-w-[1200px] mx-auto px-6 relative z-10">
          <div className="text-center max-w-3xl mx-auto">
            {/* Tagline Badge */}
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full mb-8 animate-fade-up" 
                 style={{ background: 'var(--sp-surface-low)', border: '1px solid var(--sp-outline-var)' }}>
              <span className="w-2 h-2 rounded-full bg-current animate-pulse" style={{ color: 'var(--sp-primary)' }} />
              <span className="text-xs font-bold uppercase tracking-widest" style={{ color: 'var(--sp-primary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
                Compassionate IVF Support
              </span>
            </div>

            <h1 className="text-4xl sm:text-6xl font-bold tracking-tight leading-[1.1] animate-fade-up" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif', animationDelay: '0.1s' }}>
              Your Intelligent Companion Through the <span style={{ color: 'var(--sp-primary)' }}>IVF Journey</span>
            </h1>
            
            <p className="mt-6 text-lg sm:text-xl leading-relaxed animate-fade-up" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Atkinson Hyperlegible Next", sans-serif', animationDelay: '0.2s' }}>
              Empowering individuals and couples with personalized hormone tracking, smart treatment calendars, automated alerts, and an AI-guided recommendation engine.
            </p>

            <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4 animate-fade-up" style={{ animationDelay: '0.3s' }}>
              {user ? (
                <Link to="/dashboard" className="sp-btn-primary w-full sm:w-auto px-8 h-14 text-base rounded-xl shadow-elevated">
                  Go to My Dashboard <ArrowRight className="w-5 h-5 ml-1" />
                </Link>
              ) : (
                <>
                  <Link to="/auth" className="sp-btn-primary w-full sm:w-auto px-8 h-14 text-base rounded-xl shadow-elevated">
                    Start Free Tracking <ArrowRight className="w-5 h-5 ml-1" />
                  </Link>
                  <Link to="/recommendations" className="sp-btn-secondary w-full sm:w-auto px-8 h-14 text-base rounded-xl bg-white">
                    Try AI recommendations <Brain className="w-5 h-5 ml-1" />
                  </Link>
                </>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* ── Features Grid ── */}
      <section className="py-24 border-y relative z-10" style={{ background: '#fff', borderColor: 'var(--sp-outline-var)' }}>
        <div className="max-w-[1200px] mx-auto px-6">
          <div className="text-center max-w-2xl mx-auto mb-16 animate-fade-up">
            <h2 className="text-3xl sm:text-4xl font-bold tracking-tight" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
              Everything You Need to Navigate IVF
            </h2>
            <p className="mt-4 text-lg" style={{ color: 'var(--sp-on-surface-var)' }}>
              Streamlining clinical milestones, drug schedules, and mental wellbeing into a beautifully simple central app.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {[
              { icon: Activity, title: 'Cycle Trackers', desc: 'Log hormones dosage, follicular development, daily moods, sleep hours, and more.', color: 'primary' },
              { icon: Calendar, title: 'Injection Alerts', desc: 'Review automated hormone schedules and check off dosages to sustain adherence.', color: 'teal' },
              { icon: Brain,    title: 'AI Clinical Rules', desc: 'Evaluate age, AMH, and diagnostics to match statistically optimal treatment plans.', color: 'primary' },
              { icon: Users,    title: 'Anonymous Forums', desc: 'Connect and share support, suggestions, and stories safely with others.', color: 'teal' },
            ].map((feat, idx) => (
              <div key={idx} className="sp-card p-6 md:p-8 animate-fade-up" style={{ animationDelay: `${0.1 * (idx + 1)}s` }}>
                <div className={`w-12 h-12 rounded-xl flex items-center justify-center mb-6`}
                     style={{ 
                       background: feat.color === 'primary' ? 'var(--sp-primary-container)' : 'var(--sp-secondary-container)',
                       color: feat.color === 'primary' ? 'var(--sp-primary)' : 'var(--sp-secondary)'
                     }}>
                  <feat.icon className="w-6 h-6" />
                </div>
                <h3 className="text-lg font-bold mb-3" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{feat.title}</h3>
                <p className="text-sm leading-relaxed" style={{ color: 'var(--sp-on-surface-var)' }}>{feat.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── Wellness Section ── */}
      <section className="py-24 relative z-10" style={{ background: 'var(--sp-surface)' }}>
        <div className="max-w-[1200px] mx-auto px-6">
          <div className="lg:grid lg:grid-cols-2 lg:gap-16 items-center">
            
            <div className="mb-12 lg:mb-0 animate-fade-up">
              <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full mb-6" style={{ background: 'var(--sp-surface-highest)', color: 'var(--sp-on-surface-var)' }}>
                <Smile className="w-4 h-4" /> <span className="text-xs font-bold tracking-wide uppercase" style={{ fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Mental Wellbeing</span>
              </div>
              
              <h2 className="text-3xl sm:text-4xl font-bold tracking-tight leading-[1.2]" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
                Nurturing Your Emotional Health Every Step of the Way
              </h2>
              
              <p className="mt-6 text-lg leading-relaxed" style={{ color: 'var(--sp-on-surface-var)' }}>
                We understand that IVF takes an emotional toll. IVF Companion incorporates mental wellness modules, guided breathing tips, quotes, and forums to maintain a positive headspace.
              </p>

              <ul className="mt-8 space-y-5">
                {[
                  'Curated mindfulness practices and lightweight breathing exercises.',
                  'Daily uplifting quotes generated specifically for IVF resilience.',
                  '100% secure, option-based anonymous community discussions.'
                ].map((item, i) => (
                  <li key={i} className="flex items-start gap-3">
                    <CheckCircle2 className="w-6 h-6 shrink-0 mt-0.5" style={{ color: 'var(--sp-secondary)' }} />
                    <span className="text-base font-medium" style={{ color: 'var(--sp-on-surface)' }}>{item}</span>
                  </li>
                ))}
              </ul>
            </div>

            {/* Visual Glassmorphism Block */}
            <div className="relative animate-fade-up" style={{ animationDelay: '0.2s' }}>
              <div className="absolute inset-0 rounded-[2rem] blur-[60px] opacity-30" style={{ background: 'linear-gradient(135deg, var(--sp-primary) 0%, var(--sp-secondary) 100%)' }}></div>
              <div className="relative rounded-[2rem] p-8 md:p-10 border shadow-elevated" style={{ background: 'rgba(255,255,255,0.85)', backdropFilter: 'blur(20px)', borderColor: 'var(--sp-outline-var)' }}>
                <span className="text-xs font-bold uppercase tracking-widest" style={{ color: 'var(--sp-primary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Wellness Tip of the Day</span>
                <blockquote className="mt-6 text-xl font-medium italic leading-relaxed" style={{ color: 'var(--sp-on-surface)' }}>
                  "Oocytes respond strongly to a relaxed nervous system. Take a 5-minute pause now, do three deep abdominal breaths, and remind yourself that you are doing everything you can. You are stronger than you know."
                </blockquote>
                <div className="mt-8 flex items-center gap-4">
                  <div className="w-12 h-12 rounded-full flex items-center justify-center" style={{ background: 'var(--sp-surface-container)' }}>
                    <Heart className="w-5 h-5 fill-current" style={{ color: 'var(--sp-error)' }} />
                  </div>
                  <div>
                    <p className="text-sm font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>IVF Companion Mindful Care</p>
                    <p className="text-xs" style={{ color: 'var(--sp-outline)' }}>Supporting Ovarian Reserves Health</p>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>
      </section>

      {/* ── CTA Section ── */}
      <section className="py-24 relative overflow-hidden z-10" style={{ background: 'var(--sp-inverse-surface)' }}>
        <div className="absolute inset-0 opacity-10" style={{ backgroundImage: 'radial-gradient(circle at center, var(--sp-primary) 0%, transparent 70%)' }}></div>
        <div className="max-w-4xl mx-auto px-6 text-center relative z-10">
          <h2 className="text-3xl sm:text-5xl font-bold tracking-tight text-white mb-6" style={{ fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
            Ready to Take Control of Your Treatment?
          </h2>
          <p className="text-lg mb-10 max-w-2xl mx-auto" style={{ color: 'var(--sp-inverse-on-surface)' }}>
            Join thousands of patients and doctors utilizing IVF Companion to track milestones and organize hormone plans under one elegant screen.
          </p>
          <div className="flex justify-center">
            <Link to="/auth" className="inline-flex items-center justify-center h-14 px-8 rounded-xl text-base font-bold transition-transform hover:-translate-y-1 shadow-elevated" 
                  style={{ background: 'var(--sp-surface)', color: 'var(--sp-primary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
              Get Started Now <ArrowRight className="w-5 h-5 ml-2" />
            </Link>
          </div>
        </div>
      </section>

    </div>
  );
};

export default LandingPage;
