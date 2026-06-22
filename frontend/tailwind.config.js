/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        // Serene Pulse Design System
        sp: {
          // Surfaces
          'surface':               '#f9f9ff',
          'surface-dim':           '#cfdaf1',
          'surface-bright':        '#f9f9ff',
          'surface-lowest':        '#ffffff',
          'surface-low':           '#f0f3ff',
          'surface-container':     '#e7eeff',
          'surface-high':          '#dee8ff',
          'surface-highest':       '#d8e3fa',
          'on-surface':            '#111c2c',
          'on-surface-var':        '#434654',
          'inverse-surface':       '#263142',
          'inverse-on-surface':    '#ebf1ff',
          // Outlines
          'outline':               '#737785',
          'outline-var':           '#c3c6d6',
          // Primary
          'primary':               '#004bba',
          'on-primary':            '#ffffff',
          'primary-container':     '#2a64d9',
          'on-primary-container':  '#e8ecff',
          'inverse-primary':       '#b2c5ff',
          'primary-fixed':         '#dae2ff',
          'primary-fixed-dim':     '#b2c5ff',
          'on-primary-fixed':      '#001848',
          'on-primary-fixed-var':  '#0040a1',
          // Secondary (Teal)
          'secondary':             '#006970',
          'on-secondary':          '#ffffff',
          'secondary-container':   '#7af1fc',
          'on-secondary-container':'#006e75',
          'secondary-fixed':       '#7df4ff',
          'secondary-fixed-dim':   '#5dd8e2',
          'on-secondary-fixed':    '#002022',
          // Tertiary
          'tertiary':              '#4c545a',
          'on-tertiary':           '#ffffff',
          'tertiary-container':    '#656c73',
          'on-tertiary-container': '#e7eef6',
          // Error
          'error':                 '#ba1a1a',
          'on-error':              '#ffffff',
          'error-container':       '#ffdad6',
          'on-error-container':    '#93000a',
          // Background
          'background':            '#f9f9ff',
          'on-background':         '#111c2c',
          'surface-variant':       '#d8e3fa',
          'surface-tint':          '#1357cc',
        }
      },
      fontFamily: {
        display: ['"Plus Jakarta Sans"', 'sans-serif'],
        body: ['"Atkinson Hyperlegible Next"', '"Atkinson Hyperlegible"', 'sans-serif'],
        sans: ['"Plus Jakarta Sans"', 'sans-serif'],
      },
      fontSize: {
        'headline-lg': ['32px', { lineHeight: '40px', letterSpacing: '-0.02em', fontWeight: '700' }],
        'headline-lg-mobile': ['26px', { lineHeight: '32px', letterSpacing: '-0.01em', fontWeight: '700' }],
        'headline-md': ['24px', { lineHeight: '32px', fontWeight: '600' }],
        'body-lg': ['18px', { lineHeight: '28px', fontWeight: '400' }],
        'body-md': ['16px', { lineHeight: '24px', fontWeight: '400' }],
        'label-md': ['14px', { lineHeight: '20px', letterSpacing: '0.01em', fontWeight: '600' }],
        'label-sm': ['12px', { lineHeight: '16px', fontWeight: '500' }],
      },
      borderRadius: {
        'sm':  '0.25rem',
        DEFAULT:'0.5rem',
        'md':  '0.75rem',
        'lg':  '1rem',
        'xl':  '1.5rem',
        'full':'9999px',
      },
      boxShadow: {
        'card':         '0px 4px 20px rgba(42, 100, 217, 0.08)',
        'card-hover':   '0px 8px 32px rgba(42, 100, 217, 0.14)',
        'elevated':     '0px 8px 40px rgba(0, 75, 186, 0.12)',
        'sm':           '0px 2px 8px rgba(42, 100, 217, 0.06)',
        'inner-glow':   'inset 0 0 0 1.5px rgba(42, 100, 217, 0.25)',
      },
      spacing: {
        'section': '48px',
        'stack':   '16px',
        'gutter':  '24px',
      },
      maxWidth: {
        'container': '1200px',
      },
      animation: {
        'fade-up':    'fadeUp 0.4s ease-out forwards',
        'fade-in':    'fadeIn 0.3s ease-out forwards',
        'pulse-soft': 'pulseSoft 2s ease-in-out infinite',
        'skeleton':   'skeleton 1.5s ease-in-out infinite',
      },
      keyframes: {
        fadeUp: {
          from: { opacity: 0, transform: 'translateY(16px)' },
          to:   { opacity: 1, transform: 'translateY(0)' },
        },
        fadeIn: {
          from: { opacity: 0 },
          to:   { opacity: 1 },
        },
        pulseSoft: {
          '0%, 100%': { opacity: 1 },
          '50%':      { opacity: 0.6 },
        },
        skeleton: {
          '0%':   { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
      },
    },
  },
  plugins: [],
}
