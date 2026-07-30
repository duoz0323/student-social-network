import { useEffect, useRef, useState } from 'react';
import { createPlacesSessionToken, fetchPlaceSuggestions, resolvePlaceSuggestion } from './googlePlacesAdapter.js';

export function usePlaceAutocomplete() {
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const sessionToken = useRef(null);

  useEffect(() => {
    const input = query.trim();
    if (input.length < 2) {
      return undefined;
    }
    let active = true;
    const timer = setTimeout(async () => {
      setLoading(true);
      setError('');
      try {
        sessionToken.current ??= await createPlacesSessionToken();
        const result = await fetchPlaceSuggestions(input, sessionToken.current);
        if (active) setSuggestions(result);
      } catch (reason) {
        if (active) setError(reason.message || 'Không thể tìm địa điểm.');
      } finally {
        if (active) setLoading(false);
      }
    }, 300);
    return () => { active = false; clearTimeout(timer); };
  }, [query]);

  async function select(suggestion) {
    setLoading(true);
    try {
      const location = await resolvePlaceSuggestion(suggestion);
      sessionToken.current = null;
      setQuery('');
      setSuggestions([]);
      return location;
    } finally {
      setLoading(false);
    }
  }

  function changeQuery(value) {
    setQuery(value);
    if (value.trim().length < 2) setSuggestions([]);
  }

  return { query, setQuery: changeQuery, suggestions, loading, error, select };
}
