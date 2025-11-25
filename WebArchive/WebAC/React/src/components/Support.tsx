// src/components/Support.tsx
import React, { useState } from 'react';
import '../App.css';
import { useAuth } from '../hooks/useAuth';

const Support: React.FC<{ navigate?: (to: string) => void }> = ({ navigate }) => {
    const { logout } = useAuth();
    const [isLoggingOut, setIsLoggingOut] = useState(false);
    const [chatOpen, setChatOpen] = useState(false);
    const [msg, setMsg] = useState('');

    const handleLogout = async () => {
        setIsLoggingOut(true);
        try {
            await logout();
        } catch {
            setIsLoggingOut(false);
        }
    };

    const toggleChat = () => {
        setChatOpen(v => !v);
    };

    const sendMessage = () => {
        if (!msg.trim()) return;
        alert('Message sent (mock): ' + msg);
        setMsg('');
    };

    return (
        <>
            {/* Title changed to "?" as requested */}
            <div className="bp-title" style={{ marginTop: 12 }}>Support</div>

            <div style={{ marginTop: 12, display: 'flex', flexDirection: 'column', gap: 10 }}>
                <div style={{ color: 'var(--muted-dark)' }}>If you need help, use one of the options below.</div>

                <div style={{ display: 'flex', gap: 10 }}>
                    <button className="bp-add-btn" onClick={toggleChat}>
                        {chatOpen ? 'Close chat' : 'Start chat'}
                    </button>
                </div>

                <div style={{ color: 'var(--muted-dark)' }}>
                    <div style={{ marginTop: 8 }}><strong>Phone:</strong> +1 (555) 123-4567</div>
                    <div style={{ marginTop: 4 }}><strong>Email:</strong> support@example.com</div>
                </div>

                <div style={{ overflow: 'hidden' }}>
                    <div
                        className={`support-chat-box ${chatOpen ? 'open' : ''}`}
                        style={{
                            transition: 'max-height 320ms ease, opacity 220ms ease',
                            maxHeight: chatOpen ? 240 : 0,
                            opacity: chatOpen ? 1 : 0,
                        }}
                    >
                        <textarea
                            value={msg}
                            onChange={(e) => setMsg(e.target.value)}
                            placeholder="Type your message..."
                            style={{
                                width: '100%',
                                minHeight: 120,
                                padding: 12,
                                borderRadius: 10,
                                border: '1px solid #e4e4ee',
                                boxSizing: 'border-box',
                                resize: 'vertical',
                            }}
                        />
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 10 }}>
                                        <button className="bp-add-btn" onClick={() => { setMsg(''); setChatOpen(false); }}>
                                Cancel
                            </button>
                            <button className="bp-add-btn" onClick={sendMessage}>Send</button>
                        </div>
                    </div>
                </div>
            </div>

            <style>{`
                .support-chat-box { overflow: hidden; max-height: 0; opacity: 0; }
                .support-chat-box.open { max-height: 400px; opacity: 1; }
            `}</style>
        </>
    );
};

export default Support;

