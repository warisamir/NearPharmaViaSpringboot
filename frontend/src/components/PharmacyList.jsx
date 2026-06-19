import PharmacyCard from './PharmacyCard';

export default function PharmacyList({
  pharmacies,
  activeChains,
  selectedId,
  onSelect,
  onGetDirections,
  loading,
  error,
}) {
  const visible = activeChains.length
    ? pharmacies.filter(p => p.chain && activeChains.some(c => p.chain.includes(c)))
    : pharmacies;

  if (loading) {
    return (
      <div className="empty-state">
        <div className="emoji">🔍</div>
        <p>Searching for pharmacies near you…</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="empty-state">
        <div className="emoji">⚠️</div>
        <p>{error}</p>
      </div>
    );
  }

  if (!visible.length) {
    return (
      <div className="empty-state">
        <div className="emoji">💊</div>
        <p>
          {pharmacies.length
            ? 'No pharmacies match the selected chains. Try removing a filter.'
            : 'Tap "Near Me" to find pharmacies around your current location.'}
        </p>
      </div>
    );
  }

  return (
    <div className="pharmacy-list">
      <div style={{ padding: '8px 16px 4px', fontSize: '.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>
        {visible.length} PHARMACIES FOUND
      </div>
      {visible.map((p, i) => (
        <PharmacyCard
          key={p.id ?? i}
          pharmacy={p}
          isSelected={p.id === selectedId}
          onSelect={onSelect}
          onGetDirections={onGetDirections}
        />
      ))}
    </div>
  );
}
