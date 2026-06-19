export default function DirectionsPanel({ pharmacy, directions, onClear }) {
  if (!pharmacy || !directions) return null;

  const route    = directions.route ?? {};
  const distance = route.distance ? `${(route.distance / 1000).toFixed(1)} km` : '—';
  const duration = route.duration ? `${Math.round(route.duration / 60)} min`  : '—';

  return (
    <div className="directions-panel">
      <div className="directions-title">🗺 Route to {pharmacy.name}</div>
      <div className="directions-stats">
        <div className="stat">
          <strong>{distance}</strong> <span>distance</span>
        </div>
        <div className="stat">
          <strong>{duration}</strong> <span>travel time</span>
        </div>
      </div>
      <button className="btn btn-danger" style={{ fontSize: '.78rem', padding: '5px 12px' }} onClick={onClear}>
        ✕ Clear Route
      </button>
    </div>
  );
}
