// src/pages/Dashboard.tsx
import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  LogOut,
  User,
  TrendingUp,
  DollarSign,
  Globe,
  Bell,
  MessageCircle,
  X,
} from 'lucide-react';
import StockCard from '@/components/StockCard';
import NewsSection from '@/components/NewsSection';
import MarketOverview from '@/components/MarketOverview';
import FavoriteStocks from '@/components/FavoriteStocks';
import LiborRates from '@/components/LiborRates';

type ChatMessage = {
  role: 'user' | 'assistant';
  content: string;
};

export default function Dashboard() {
  const { id } = useParams<{ id: string }>();
  const [currency, setCurrency] = useState('USD');
  const [userName, setUserName] = useState('');
  const [chatOpen, setChatOpen] = useState(false);
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [chatInput, setChatInput] = useState('');
  const chatEndRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  // 1) On mount: validate JWT and decode sub → userName
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      setUserName(payload.sub || '');
    } catch {
      localStorage.removeItem('token');
      navigate('/login');
    }
  }, [navigate]);

  // 2) Auto-scroll chat to bottom when messages update
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessages]);

  const staticRates: Record<string, number> = {
    USD: 1.0,
    EUR: 0.93,
    GBP: 0.82,
    JPY: 144.5,
    CAD: 1.35,
    INR: 82.37,
  };

  const currencies = [
    { value: 'USD', label: 'USD ($)' },
    { value: 'EUR', label: 'EUR (€)' },
    { value: 'GBP', label: 'GBP (£)' },
    { value: 'JPY', label: 'JPY (¥)' },
    { value: 'CAD', label: 'CAD (C$)' },
    { value: 'INR', label: 'INR (₹)' },
  ];

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  // 3) Send & stream chat via Ollama, with system prompt
  const sendChatMessage = async () => {
    const trimmed = chatInput.trim();
    if (!trimmed) return;

    // build the conversation for the API call
    const systemPrompt = {
      role: 'system' as const,
      content:
        'You are a financial markets assistant. Reply in one or two concise sentences only about markets, stocks, or currency. Do NOT include any <think> tags.',
    };
    const history = [
      systemPrompt,
      ...chatMessages.map(m => ({ role: m.role, content: m.content })),
      { role: 'user' as const, content: trimmed },
    ];

    // update UI: add user and placeholder assistant
    const updated = [...chatMessages, { role: 'user', content: trimmed }];
    setChatMessages(updated);
    setChatInput('');
    setChatMessages(prev => [...prev, { role: 'assistant', content: '' }]);
    const assistantIndex = updated.length;

    try {
      const res = await fetch('http://localhost:11434/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          model: 'qwen3:0.6b',
          stream: true,
          messages: history.map(m => ({ role: m.role, content: m.content })),
        }),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);

      const reader = res.body!.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          if (!line.trim()) continue;
          let parsed: any;
          try {
            parsed = JSON.parse(line);
          } catch {
            continue;
          }
          const chunk = parsed.message?.content;
          if (chunk) {
            setChatMessages(prev => {
              const copy = [...prev];
              copy[assistantIndex].content += chunk;
              return copy;
            });
          }
        }
      }

      // strip any stray <think>…</think> fragments
      setChatMessages(prev => {
        const copy = [...prev];
        copy[assistantIndex].content = copy[assistantIndex].content
          .replace(/<think>.*?<\/think>/gis, '')
          .trim();
        return copy;
      });
    } catch {
      setChatMessages(prev => {
        const copy = [...prev];
        copy[assistantIndex].content = '❌ Error fetching response';
        return copy;
      });
    }
  };

  const onChatKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendChatMessage();
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-blue-900 to-indigo-900 text-gray-100">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-gray-800/80 backdrop-blur-md border-b border-gray-700/60 shadow-sm">
        <div className="container mx-auto px-6 py-4 flex items-center justify-between">
          {/* Logo & Title */}
          <div className="flex items-center space-x-4">
            <div className="relative">
              <img
                src="/lovable-uploads/9c2f154b-9310-4158-b375-1ee6e2fa6df5.png"
                alt="Market95 Logo"
                className="w-10 h-10 rounded-lg shadow-sm"
              />
              <div className="absolute -top-1 -right-1 w-3 h-3 bg-green-400 rounded-full animate-pulse" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-white">Market95</h1>
              <div className="text-xs text-gray-400 font-medium">
                Los Angeles Financial Markets
              </div>
            </div>
          </div>
          {/* Controls */}
          <div className="flex items-center space-x-4">
            <Select value={currency} onValueChange={setCurrency}>
              <SelectTrigger className="w-36 bg-gray-800 border-gray-700 hover:bg-gray-700 transition-all duration-200">
                <Globe className="w-4 h-4 text-gray-400 mr-2" />
                <SelectValue />
              </SelectTrigger>
              <SelectContent className="bg-gray-800 backdrop-blur-sm border border-gray-700">
                {currencies.map(c => (
                  <SelectItem key={c.value} value={c.value} className="hover:bg-gray-700">
                    {c.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <div className="flex items-center space-x-3 bg-gray-800/70 px-4 py-2 rounded-lg border border-gray-700">
              <div className="w-8 h-8 bg-gradient-to-br from-blue-600 to-indigo-700 rounded-full flex items-center justify-center">
                <User className="w-4 h-4 text-white" />
              </div>
              <div className="hidden md:block">
                <div className="text-sm font-semibold text-white">{userName}</div>
                <div className="text-xs text-gray-400">Premium User</div>
              </div>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={handleLogout}
              className="text-gray-300 hover:text-white hover:bg-red-800 transition-all duration-200"
            >
              <LogOut className="w-4 h-4" />
              <span className="hidden md:inline ml-2">Logout</span>
            </Button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container mx-auto px-6 py-8 space-y-8">
        {/* Welcome Section */}
        <section className="relative">
          <div className="absolute inset-0 bg-gray-800/30 rounded-2xl" />
          <div className="relative bg-gray-900/70 backdrop-blur-sm rounded-2xl p-8 border border-gray-700 shadow-xl">
            <div className="flex items-start justify-between">
              <div>
                <h2 className="text-4xl font-bold text-white mb-2">
                  Welcome back, {userName.split(' ')[0]}!
                </h2>
                <p className="text-gray-400 text-lg">
                  Here’s what’s happening in the markets today.
                </p>
              </div>
              <div className="hidden lg:flex items-center space-x-2">
                <div className="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center shadow-lg">
                  <TrendingUp className="w-6 h-6 text-white" />
                </div>
                <div className="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center shadow-lg">
                  <DollarSign className="w-6 h-6 text-white" />
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Market Overview */}
        <div className="transform hover:scale-[1.01] transition-transform duration-200">
          <MarketOverview currency={currency} rates={staticRates} isLoading={false} />
        </div>

        {/* Info Cards Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          <FavoriteStocks />
          <LiborRates />
          <Card className="lg:col-span-2 bg-gray-900/70 backdrop-blur-sm border-gray-700 shadow-xl hover:shadow-2xl transition-all duration-300">
            <CardHeader className="pb-4">
              <CardTitle className="text-xl font-bold flex items-center gap-2 text-white">
                <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse" />
                Data Sources
              </CardTitle>
            </CardHeader>
            <CardContent>
              {/* Data sources details... */}  
            </CardContent>
          </Card>
        </div>

        {/* Featured Stocks */}
        <section>
          <h3 className="text-2xl font-bold text-white mb-6 flex items-center gap-3">
            <div className="w-1 h-8 bg-gradient-to-b from-blue-400 to-indigo-500 rounded-full" />
            Featured Stocks
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {['AAPL','GOOGL','MSFT','AMZN','TSLA','NVDA','META','NFLX'].map(
              (sym, idx) => (
                <div
                  key={sym}
                  className="transform hover:scale-[1.03] transition-all duration-300"
                  style={{ animationDelay: `${idx * 100}ms` }}
                >
                  <StockCard symbol={sym} currency={currency} />
                </div>
              )
            )}
          </div>
        </section>

        {/* News Section */}
        <section className="transform hover:scale-[1.005] transition-transform duration-300">
          <NewsSection />
        </section>
      </main>

      {/* Chat Toggle */}
      <button
        onClick={() => setChatOpen(o => !o)}
        className="fixed bottom-6 right-6 w-14 h-14 bg-blue-600 hover:bg-blue-700 text-white rounded-full shadow-lg flex items-center justify-center transition-colors"
      >
        <MessageCircle className="w-6 h-6" />
      </button>

      {/* Chat Window */}
      {chatOpen && (
        <div className="fixed bottom-20 right-6 w-80 h-96 bg-gray-900/90 backdrop-blur-sm rounded-xl shadow-2xl flex flex-col">
          <div className="flex items-center justify-between px-4 py-2 bg-gray-800 rounded-t-xl">
            <div className="text-white font-semibold">Market Bot</div>
            <button onClick={() => setChatOpen(false)}>
              <X className="w-5 h-5 text-gray-400 hover:text-white" />
            </button>
          </div>
          <div className="flex-1 overflow-y-auto px-3 py-2 space-y-2">
            {chatMessages.map((m, i) => (
              <div
                key={i}
                className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                <div
                  className={`rounded-lg px-3 py-1 max-w-[70%] ${
                    m.role === 'user' ? 'bg-blue-600 text-white' : 'bg-gray-700 text-gray-100'
                  }`}
                >
                  {m.content}
                </div>
              </div>
            ))}
            <div ref={chatEndRef} />
          </div>
          <div className="px-3 py-2 bg-gray-800 rounded-b-xl flex items-center space-x-2">
            <textarea
              value={chatInput}
              onChange={e => setChatInput(e.target.value)}
              onKeyDown={onChatKeyDown}
              rows={1}
              className="flex-1 resize-none bg-gray-700 text-gray-100 placeholder-gray-400 px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Type a message…"
            />
            <Button onClick={sendChatMessage} className="p-2 bg-blue-600 hover:bg-blue-700">
              Send
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
