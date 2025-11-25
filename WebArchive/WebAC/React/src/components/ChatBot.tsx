// src/components/ChatBot.tsx
import React, { useEffect, useRef, useState } from 'react';
import '../App.css';
import { useAuth } from '../hooks/useAuth';

type ChatMessage = {
  id: string;
  role: 'user' | 'ai' | 'system';
  text: string;
  time: string;
};

const sampleExpenses = [
  { id: 1, title: 'Grocery', category: 'Food', amount: -45.5, date: '2025-10-01' },
  { id: 2, title: 'Salary', category: 'Income', amount: 2500, date: '2025-10-01' },
  { id: 3, title: 'Train ticket', category: 'Travel', amount: -30, date: '2025-10-03' },
  { id: 4, title: 'Dinner', category: 'Food', amount: -60, date: '2025-10-05' },
  { id: 5, title: 'Coffee', category: 'Food', amount: -5, date: '2025-10-06' },
  { id: 6, title: 'Electricity bill', category: 'Utilities', amount: -120, date: '2025-09-28' },
];

function nowStr() {
  return new Date().toLocaleTimeString();
}

/**
 * Simple heuristic "AI" that answers a few queries based on the mock expenses.
 * In production you'd POST user message + context to your backend AI endpoint.
 */
function generateMockAiReply(message: string, userName: string | undefined) {
  const m = message.toLowerCase();

  // totals
  if (m.includes('total') || m.includes('sum') || m.includes('how much')) {
    const total = sampleExpenses.reduce((s, e) => s + e.amount, 0);
    return `Hello ${userName ?? ''}. Your current balance (sum of recent items) is ${total >= 0 ? '+' : '-'}$${Math.abs(
      Math.round(total * 100) / 100
    )}.`;
  }

  // recent
  if (m.includes('recent') || m.includes('last') || m.includes('latest')) {
    const last = sampleExpenses
      .slice()
      .sort((a, b) => (a.date < b.date ? 1 : -1))
      .slice(0, 3)
      .map((x) => `${x.title} (${x.category}): ${x.amount < 0 ? '-' : '+'}$${Math.abs(x.amount)}`)
      .join('; ');
    return `Your last 3 transactions: ${last}.`;
  }

  // food spending
  if (m.includes('food') || m.includes('grocery')) {
    const sumFood = sampleExpenses.filter((e) => e.category.toLowerCase() === 'food').reduce((s, e) => s + e.amount, 0);
    return `You've spent ${Math.abs(Math.round(sumFood * 100) / 100)} on Food in the listed items.`;
  }

  // simple fallback
  return "I can answer questions about your expenses (e.g. \"What's my total?\", \"Show my recent transactions\", \"How much did I spend on Food?\"). // TO DO: send message to backend AI endpoint to get richer responses.";
}

const ChatBot: React.FC = () => {
  const { user } = useAuth();
  const [messages, setMessages] = useState<ChatMessage[]>(() => [
    {
      id: 'sys-1',
      role: 'system',
      text:
        'Hi — I am your Expenses Assistant. Ask me about your transactions, totals or recent activity. (This is a mock; backend AI calls are TO DO.)',
      time: nowStr(),
    },
  ]);
  const [text, setText] = useState('');
  const [isThinking, setIsThinking] = useState(false);
  const listRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    // auto-scroll to bottom on new message
    const node = listRef.current;
    if (node) node.scrollTop = node.scrollHeight;
  }, [messages]);

  const sendMessage = async () => {
    if (!text.trim()) return;
    const userMsg: ChatMessage = {
      id: `u-${Date.now()}`,
      role: 'user',
      text: text.trim(),
      time: nowStr(),
    };
    setMessages((m) => [...m, userMsg]);
    setText('');
    setIsThinking(true);

    // In production: send message + user context to backend AI service
    // TODO: POST /ai/chat { message, user_id, context: { expenses: ... } }
    // const resp = await apiClient.post('/ai/chat', { message: userMsg.text, user_id: user?.id });

    // simulate AI latency
    setTimeout(() => {
      const aiText = generateMockAiReply(userMsg.text, user?.first_name);
      const aiMsg: ChatMessage = {
        id: `ai-${Date.now()}`,
        role: 'ai',
        text: aiText,
        time: nowStr(),
      };
      setMessages((m) => [...m, aiMsg]);
      setIsThinking(false);
    }, 700);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const clearConversation = () => {
    setMessages([
      {
        id: 'sys-1',
        role: 'system',
        text: 'Conversation cleared. Ask me about your expenses.',
        time: nowStr(),
      },
    ]);
  };

  return (
    <div style={{ marginTop: 12, display: 'flex', flexDirection: 'column', gap: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div className="bp-title">AI Assistant</div>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="bp-add-btn" onClick={clearConversation}>
            Clear
          </button>
        </div>
      </div>

      <div style={{ color: 'var(--muted-dark)' }}>
        Ask questions about your expenses. (This UI uses mock data; backend integration is marked with <code>// TO DO</code>.)
      </div>

      <div
        ref={listRef}
        style={{
          height: 360,
          overflowY: 'auto',
          padding: 12,
          borderRadius: 10,
          border: '1px solid #e4e4ee',
          background: 'var(--card-light)',
          boxSizing: 'border-box',
        }}
      >
        {messages.map((m) => (
          <div key={m.id} style={{ marginBottom: 12, display: 'flex', flexDirection: m.role === 'user' ? 'row-reverse' : 'row', gap: 10 }}>
            {/* updated bubble styles: AI bubbles now purple for better contrast */}
            <div
              style={{
                maxWidth: '78%',
                padding: 10,
                borderRadius: 12,
                background: m.role === 'user'
                  ? 'linear-gradient(90deg,var(--purple-1),var(--purple-2))'
                  : 'linear-gradient(90deg, rgba(124,58,237,0.4), rgba(108,52,235,1))', // <-- changed to purple-ish background
                color: '#fff', // keep text white for readability on purple
                boxShadow: '0 6px 18px rgba(0,0,0,0.4)',
                wordBreak: 'break-word',
              }}
            >
              <div style={{ fontSize: 14 }}>{m.text}</div>
              <div style={{ fontSize: 11, color: 'rgba(255,255,255,0.8)', marginTop: 6, textAlign: 'right' }}>{m.time}</div>
            </div>
          </div>
        ))}

        {isThinking && (
          <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
            <div style={{ width: 12, height: 12, borderRadius: 6, background: 'var(--muted-dark)', animation: 'blink 1s infinite' }} />
            <div style={{ color: 'var(--muted-dark)' }}>AI is typing...</div>
            <style>{`@keyframes blink { 0% {opacity:1} 50% {opacity:0.25} 100% {opacity:1} }`}</style>
          </div>
        )}
      </div>

      <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
        <input
          placeholder="Type your question (e.g. 'What's my total?', 'Show recent transactions')"
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={handleKeyDown}
          style={{ flex: 1, padding: 10, borderRadius: 8, border: '1px solid #e4e4ee' }}
        />
        <button className="bp-add-btn" onClick={sendMessage} disabled={!text.trim() || isThinking}>
          Send
        </button>
      </div>

      <div style={{ color: 'var(--muted-dark)', fontSize: 13 }}>
        Example prompts: <em>"What's my total?"</em>, <em>"Show my recent transactions"</em>, <em>"How much did I spend on Food?"</em>
      </div>
    </div>
  );
};

export default ChatBot;

