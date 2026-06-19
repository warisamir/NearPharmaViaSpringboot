export default function PharmacyCard({ pharmacy, isSelected, onSelect, onGetDirections }) {
  const { name, address, chain, distance, duration, isOpen, phone } = pharmacy;

  return (
    <div
      className={`pharmacy-card ${isSelected ? 'selected' : ''}`}
      onClick={() => onSelect(pharmacy)}
    >
      <div className="card-header">
        <span className="card-name">{name}</span>
        {isOpen && <span className="badge-24x7">OPEN</span>}
      </div>

      <div className="card-address">{address || 'Address not available'}</div>

      {chain && <span className="card-chain">{chain}</span>}

      {(distance || duration) && (
        <div className="card-meta">
          {distance && (
            <div className="meta-item">
              📏 {distance} <span>away</span>
            </div>
          )}
          {duration && (
            <div className="meta-item">
              ⏱ {duration} <span>ETA</span>
            </div>
          )}
        </div>
      )}

      {phone && (
        <div style={{ fontSize: '.75rem', color: 'var(--text-muted)', marginTop: 4 }}>
          📞 {phone}
        </div>
      )}

      <div className="card-actions">
        <button
          className="btn btn-primary"
          style={{ fontSize: '.78rem', padding: '6px 12px' }}
          onClick={(e) => { e.stopPropagation(); onGetDirections(pharmacy); }}
        >
          🗺 Directions
        </button>
        {phone && (
          <a
            href={`tel:${phone}`}
            className="btn btn-secondary"
            style={{ fontSize: '.78rem', padding: '6px 12px', textDecoration: 'none' }}
            onClick={e => e.stopPropagation()}
          >
            📞 Call
          </a>
        )}
      </div>
    </div>
  );
}
