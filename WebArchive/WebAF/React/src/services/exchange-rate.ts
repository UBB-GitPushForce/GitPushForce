const CACHE_KEY = 'bp_exch_rate';
const TTL_MS = 1000 * 60 * 60; // 1 hour

type Cache = { rate: number; ts: number };

export async function getRonToEurRate(): Promise<number> {
  try {
    const cached = localStorage.getItem(CACHE_KEY);
    if (cached) {
      const parsed: Cache = JSON.parse(cached);
      if (Date.now() - parsed.ts < TTL_MS) {
        return parsed.rate;
      }
    }
  } catch (err) {
    // ignore
  }

  // Fetch from exchangerate.host (no API key, free)
  const url = 'https://api.exchangerate.host/latest?base=RON&symbols=EUR';
  const res = await fetch(url);
  if (!res.ok) throw new Error('Exchange API returned ' + res.status);
  const data = await res.json();
  // data.rates.EUR is amount of EUR for 1 RON
  const rate = Number(data?.rates?.EUR);
  if (!rate || Number.isNaN(rate)) throw new Error('Invalid rate from API');

  try {
    localStorage.setItem(CACHE_KEY, JSON.stringify({ rate, ts: Date.now() }));
  } catch {}

  return rate;
}

export default getRonToEurRate;
