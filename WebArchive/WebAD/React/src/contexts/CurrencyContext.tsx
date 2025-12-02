import React, { createContext, useContext, useEffect, useState } from 'react';
import { getRonToEurRate } from '../services/exchange-rate';

type Currency = 'RON' | 'EUR';

type CurrencyContextValue = {
  currency: Currency;
  setCurrency: (c: Currency) => void;
  formatAmount: (amountInRon: number, opts?: { showSign?: boolean }) => string;
  rate?: number | null; // RON -> EUR
};

const CurrencyContext = createContext<CurrencyContextValue | undefined>(undefined);

const STORAGE_KEY = 'bp_currency';

export const CurrencyProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currency, setCurrencyState] = useState<Currency>(() => {
    try {
      const v = localStorage.getItem(STORAGE_KEY);
      if (v === 'EUR' || v === 'RON') return v;
    } catch {}
    return 'RON';
  });

  const [rate, setRate] = useState<number | null>(null);

  const FALLBACK_RATE = 0.20; // used while fetching or if API fails (1 RON ~= 0.20 EUR)

  useEffect(() => {
    try { localStorage.setItem(STORAGE_KEY, currency); } catch {}
  }, [currency]);

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const r = await getRonToEurRate();
        if (mounted) setRate(r);
      } catch (err) {
        console.warn('Failed to fetch exchange rate', err);
        if (mounted) setRate(null);
      }
    })();
    return () => { mounted = false; };
  }, []);

  // If user switches to EUR and we don't have a rate yet, try fetching again
  useEffect(() => {
    if (currency === 'EUR' && rate == null) {
      let mounted = true;
      (async () => {
        try {
          const r = await getRonToEurRate();
          if (mounted) setRate(r);
        } catch (err) {
          console.warn('Retry to fetch exchange rate failed', err);
          // keep null; formatAmount will use fallback
        }
      })();
      return () => { mounted = false; };
    }
  }, [currency, rate]);

  const setCurrency = (c: Currency) => setCurrencyState(c);

  const formatAmount = (amountInRon: number, opts?: { showSign?: boolean }) => {
    const showSign = opts?.showSign ?? true;
    const sign = amountInRon < 0 ? '-' : '';
    const absVal = Math.abs(amountInRon);

    if (currency === 'RON') {
      // display as Lei (local name), no extra spaces before sign
      return `${sign}${absVal.toFixed(2)} Lei`;
    }

    // EUR selected — use fetched rate or fallback value to show EUR immediately
    const r = rate ?? FALLBACK_RATE;
    const converted = absVal * r;
    return `${sign}€${converted.toFixed(2)}`;
  };

  return (
    <CurrencyContext.Provider value={{ currency, setCurrency, formatAmount, rate }}>
      {children}
    </CurrencyContext.Provider>
  );
};

export const useCurrency = () => {
  const ctx = useContext(CurrencyContext);
  if (!ctx) throw new Error('useCurrency must be used within CurrencyProvider');
  return ctx;
};

export default CurrencyContext;
