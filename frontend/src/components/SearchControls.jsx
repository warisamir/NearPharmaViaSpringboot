import { useState, useRef, useEffect } from 'react';

const MODES = [
  { value: 'driving',   label: '🚗 Drive'   },
  { value: 'walking',   label: '🚶 Walk'    },
  { value: 'bicycling', label: '🚴 Cycle'   },
  { value: 'transit',   label: '🚌 Transit' },
];

export default function SearchControls({
  mode, onModeChange,
  radius, onRadiusChange,
  availableChains,
  activeChains, onToggleChain,
  onLocate, loading,
}) {
  const [localRadius, setLocalRadius] = useState(radius);
  const timerRef = useRef(null);

  useEffect(() => {
    setLocalRadius(radius);
  }, [radius]);

  const handleRadiusChange = (newRadius) => {
    setLocalRadius(newRadius);
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => {
      onRadiusChange(newRadius);
    }, 500);
  };

  return (
    <div className="controls">
      {/* Locate + mode */}
      <div className="controls-row">
        <button className="btn btn-primary" onClick={onLocate} disabled={loading}>
          {loading
            ? <><span className="spinner" /> Locating…</>
            : '📍 Near Me'}
        </button>
        <select value={mode} onChange={e => onModeChange(e.target.value)}>
          {MODES.map(m => (
            <option key={m.value} value={m.value}>{m.label}</option>
          ))}
        </select>
      </div>

      {/* Radius slider */}
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
          <span style={{ fontSize: '.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>
            SEARCH RADIUS
          </span>
          <span style={{ fontSize: '.8rem', fontWeight: 700, color: 'var(--brand-dark)' }}>
            {localRadius} km
          </span>
        </div>
        <input
          type="range"
          min="1" max="50" step="1"
          value={localRadius}
          onChange={e => handleRadiusChange(Number(e.target.value))}
          style={{ width: '100%', accentColor: 'var(--brand)', cursor: 'pointer' }}
        />
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '.68rem', color: 'var(--text-muted)' }}>
          <span>1 km</span>
          <span>25 km</span>
          <span>50 km</span>
        </div>
      </div>

      {/* Chain filter chips — derived from live results */}
      {availableChains.length > 0 && (
        <div>
          <div style={{ fontSize: '.7rem', color: 'var(--text-muted)', fontWeight: 600, marginBottom: 4 }}>
            FILTER BY CHAIN
          </div>
          <div className="chip-row">
            {availableChains.map(chain => (
              <button
                key={chain}
                className={`chip ${activeChains.includes(chain) ? 'active' : ''}`}
                onClick={() => onToggleChain(chain)}
              >
                {chain}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
