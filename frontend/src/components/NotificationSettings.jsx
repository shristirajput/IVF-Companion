import React, { useState } from 'react';
import { X, Mail, MessageSquare } from 'lucide-react';
import api from '../utils/api';

export default function NotificationSettings({ onClose }) {
  const [settings, setSettings] = useState({
    emailEnabled: false,
    smsEnabled: false,
    phoneNumber: ''
  });
  const [status, setStatus] = useState(null);

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      setStatus('saving');
      await api.post('/api/notifications/settings', settings);
      setStatus('success');
      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err) {
      console.error(err);
      setStatus('error');
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-[100] animate-fade-in p-4">
      <div className="bg-white dark:bg-gray-800 rounded-2xl w-full max-w-md p-6 shadow-2xl relative">
        <button 
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 dark:hover:text-white"
        >
          <X className="w-5 h-5" />
        </button>

        <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-6">Notification Preferences</h3>

        <form onSubmit={handleSave} className="space-y-6">
          <div className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700/50 rounded-xl border border-gray-100 dark:border-gray-700">
            <div className="flex items-center">
              <div className="w-10 h-10 rounded-full bg-teal-100 dark:bg-teal-900/40 flex items-center justify-center mr-3">
                <Mail className="w-5 h-5 text-teal-600 dark:text-teal-400" />
              </div>
              <div>
                <h4 className="font-semibold text-gray-900 dark:text-white text-sm">Email Alerts</h4>
                <p className="text-xs text-gray-500 dark:text-gray-400">Receive appointment reminders</p>
              </div>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input 
                type="checkbox" 
                className="sr-only peer" 
                checked={settings.emailEnabled}
                onChange={(e) => setSettings({...settings, emailEnabled: e.target.checked})}
              />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer dark:bg-gray-600 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-teal-500"></div>
            </label>
          </div>

          <div className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-xl border border-gray-100 dark:border-gray-700 space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <div className="w-10 h-10 rounded-full bg-rose-100 dark:bg-rose-900/40 flex items-center justify-center mr-3">
                  <MessageSquare className="w-5 h-5 text-rose-600 dark:text-rose-400" />
                </div>
                <div>
                  <h4 className="font-semibold text-gray-900 dark:text-white text-sm">SMS Alerts</h4>
                  <p className="text-xs text-gray-500 dark:text-gray-400">Instant medication reminders</p>
                </div>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input 
                  type="checkbox" 
                  className="sr-only peer" 
                  checked={settings.smsEnabled}
                  onChange={(e) => setSettings({...settings, smsEnabled: e.target.checked})}
                />
                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer dark:bg-gray-600 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-rose-500"></div>
              </label>
            </div>

            {settings.smsEnabled && (
              <div className="pt-2 animate-fade-in">
                <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">Phone Number</label>
                <input 
                  type="tel"
                  placeholder="+1 (555) 000-0000"
                  value={settings.phoneNumber}
                  onChange={(e) => setSettings({...settings, phoneNumber: e.target.value})}
                  required={settings.smsEnabled}
                  className="w-full p-2 text-sm border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:ring-rose-500 focus:border-rose-500"
                />
              </div>
            )}
          </div>

          <button 
            type="submit"
            disabled={status === 'saving'}
            className="w-full py-2.5 bg-gray-900 dark:bg-white text-white dark:text-gray-900 font-bold rounded-xl hover:bg-gray-800 dark:hover:bg-gray-100 transition-colors flex justify-center"
          >
            {status === 'saving' ? 'Saving...' : status === 'success' ? 'Saved! Testing Notification...' : 'Save Preferences'}
          </button>
          
          {status === 'error' && <p className="text-red-500 text-sm text-center">Failed to save settings.</p>}
        </form>
      </div>
    </div>
  );
}
