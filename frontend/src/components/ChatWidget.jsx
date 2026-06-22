import React, { useState, useEffect, useRef } from 'react';
import { MessageCircle, X, Send } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import api from '../utils/api';

export default function ChatWidget() {
  const { user } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [stompClient, setStompClient] = useState(null);
  const messagesEndRef = useRef(null);

  const [recipientId, setRecipientId] = useState(null);
  const [recipientName, setRecipientName] = useState('');

  useEffect(() => {
    if (user && user.role === 'ROLE_PATIENT' && user.assignedDoctorUserId) {
      setRecipientId(user.assignedDoctorUserId);
      setRecipientName(user.assignedDoctorName);
    } else if (user && user.role === 'ROLE_DOCTOR') {
      setRecipientId(4);
      setRecipientName('Jane Doe');
    }
  }, [user]);

  useEffect(() => {
    if (isOpen && recipientId) {
      fetchHistory();
      connectStomp();
    }
    return () => {
      if (stompClient) stompClient.deactivate();
    };
  }, [isOpen, recipientId]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const fetchHistory = async () => {
    try {
      const res = await api.get(`/api/chat/messages/${recipientId}`);
      setMessages(res.data);
    } catch (err) { console.error(err); }
  };

  const connectStomp = () => {
    const token = localStorage.getItem('token');
    if (!token) return;

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-chat'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = function () {
      client.subscribe(`/topic/public`, (message) => {
        setMessages((prev) => [...prev, JSON.parse(message.body)]);
      });
    };

    client.activate();
    setStompClient(client);
  };

  const sendMessage = (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;

    const chatMessagePayload = {
      senderId: String(user.id),
      senderName: user.fullName || user.username,
      content: newMessage.trim(),
      timestamp: new Date().toLocaleTimeString()
    };

    setMessages((prev) => [...prev, chatMessagePayload]);

    if (stompClient && stompClient.connected) {
      stompClient.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(chatMessagePayload),
      });
    }
    setNewMessage('');
  };

  if (!user || user.role === 'ROLE_ADMIN') return null;

  return (
    <>
      {/* Floating Chat Button */}
      {!isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          className="fixed bottom-6 right-6 w-14 h-14 rounded-full flex items-center justify-center shadow-elevated transition-transform hover:scale-105 z-50 animate-bounce-soft"
          style={{ background: 'var(--sp-primary)', color: 'var(--sp-on-primary)' }}
        >
          <MessageCircle className="w-6 h-6" />
        </button>
      )}

      {/* Chat Window */}
      {isOpen && (
        <div className="fixed bottom-6 right-6 w-80 sm:w-96 h-[500px] rounded-2xl flex flex-col z-50 overflow-hidden animate-fade-in shadow-elevated"
             style={{ background: 'var(--sp-surface)', border: '1px solid var(--sp-outline-var)' }}>
          
          {/* Header */}
          <div className="p-4 flex justify-between items-center" style={{ background: 'var(--sp-primary)', color: 'var(--sp-on-primary)' }}>
            <div>
              <h3 className="font-bold text-sm" style={{ fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Chat with {recipientName || 'Doctor'}</h3>
              <p className="text-xs opacity-90 mt-0.5">Usually replies in a few hours</p>
            </div>
            <button onClick={() => setIsOpen(false)} className="opacity-80 hover:opacity-100 transition-opacity">
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Messages Area */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3 custom-scrollbar" style={{ background: 'var(--sp-surface-low)' }}>
            {messages.length === 0 ? (
              <div className="text-center text-sm mt-10" style={{ color: 'var(--sp-outline)' }}>No messages yet. Say hi!</div>
            ) : (
              messages.map((msg, idx) => {
                const isMe = msg.senderId === String(user.id);
                return (
                  <div key={msg.id || idx} className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}>
                    <div
                      className={`max-w-[80%] px-3.5 py-2.5 text-[15px] leading-snug`}
                      style={{
                        fontFamily: '"Atkinson Hyperlegible Next", sans-serif',
                        background: isMe ? 'var(--sp-primary)' : 'var(--sp-surface-highest)',
                        color: isMe ? 'var(--sp-on-primary)' : 'var(--sp-on-surface)',
                        borderRadius: isMe ? '16px 16px 4px 16px' : '16px 16px 16px 4px',
                        boxShadow: '0 2px 8px rgba(0,0,0,0.04)'
                      }}
                    >
                      {msg.content}
                    </div>
                  </div>
                );
              })
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input Area */}
          <form onSubmit={sendMessage} className="p-3 border-t flex items-center gap-2" style={{ background: 'var(--sp-surface)', borderColor: 'var(--sp-outline-var)' }}>
            <input
              type="text"
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              placeholder="Type a message..."
              className="flex-1 h-10 px-4 rounded-full text-sm focus:outline-none"
              style={{ background: 'var(--sp-surface-container)', color: 'var(--sp-on-surface)', fontFamily: '"Atkinson Hyperlegible Next", sans-serif' }}
            />
            <button
              type="submit"
              disabled={!newMessage.trim()}
              className="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0 disabled:opacity-50 transition-transform hover:scale-105"
              style={{ background: 'var(--sp-primary)', color: 'var(--sp-on-primary)' }}
            >
              <Send className="w-4 h-4" />
            </button>
          </form>
        </div>
      )}

      <style jsx>{`
        .animate-bounce-soft {
          animation: bounceSoft 2s infinite;
        }
        @keyframes bounceSoft {
          0%, 100% { transform: translateY(0); }
          50% { transform: translateY(-5px); }
        }
      `}</style>
    </>
  );
}
