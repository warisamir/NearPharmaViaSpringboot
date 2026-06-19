import { useState, useEffect } from 'react';

// Default to Bangalore city centre if geolocation is denied / unavailable
const DEFAULT = { lat: 12.9716, lng: 77.5946 };

export function useGeolocation() {
  const [location, setLocation] = useState(null);
  const [error, setError]       = useState(null);
  const [loading, setLoading]   = useState(true);

  useEffect(() => {
    if (!navigator.geolocation) {
      setError('Geolocation not supported by your browser.');
      setLocation(DEFAULT);
      setLoading(false);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      ({ coords }) => {
        setLocation({ lat: coords.latitude, lng: coords.longitude });
        setLoading(false);
      },
      (err) => {
        setError(`Location access denied (${err.message}). Showing Bangalore.`);
        setLocation(DEFAULT);
        setLoading(false);
      },
      { enableHighAccuracy: true, timeout: 10_000 }
    );
  }, []);

  const refresh = () => {
    setLoading(true);
    setError(null);
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => {
        setLocation({ lat: coords.latitude, lng: coords.longitude });
        setLoading(false);
      },
      (err) => {
        setError(`Could not update location: ${err.message}`);
        setLoading(false);
      }
    );
  };

  return { location, error, loading, refresh };
}
