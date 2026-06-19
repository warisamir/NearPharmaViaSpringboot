import axios from 'axios';

// In dev: Vite proxy forwards /api → localhost:8080 (no VITE_API_URL needed)
// In prod: set VITE_API_URL=https://your-backend.railway.app in Vercel env vars
const BASE = import.meta.env.VITE_API_URL
  ? `${import.meta.env.VITE_API_URL}/api/pharmacies`
  : '/api/pharmacies';

const api = axios.create({ baseURL: BASE });

/**
 * Fetch all pharmacies within [radius] km of [lat, lng], sorted by travel time.
 * @param {number} lat
 * @param {number} lng
 * @param {string} mode    driving | walking | bicycling | transit
 * @param {number} radius  search radius in km (1–50), default 15
 */
export const getDistances = (lat, lng, mode = 'driving', radius = 15) =>
  api.get('/distances', { params: { lat, lng, mode, radius } }).then(r => r.data);

/**
 * Get turn-by-turn directions from [fromLat, fromLng] to pharmacy {id}.
 */
export const getDirections = (id, fromLat, fromLng, mode = 'driving') =>
  api.get(`/${id}/directions`, { params: { fromLat, fromLng, mode } }).then(r => r.data);

/**
 * Find pharmacies near a specific pharmacy (by DB id), optionally filtered by chain.
 */
export const getNearbyPharmacies = (id, radius = 2000, chains = []) => {
  const params = { radius };
  if (chains.length) params.chains = chains;
  return api.get(`/${id}/nearby`, { params }).then(r => r.data);
};

/**
 * Text search for healthcare places near [lat, lng].
 */
export const searchPlaces = (query, lat, lng) =>
  api.get('/places/search', { params: { query, lat, lng } }).then(r => r.data);
