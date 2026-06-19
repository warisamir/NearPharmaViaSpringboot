import { useState, useCallback, useEffect } from 'react';
import PharmacyMap       from './components/PharmacyMap';
import SearchControls    from './components/SearchControls';
import PharmacyList      from './components/PharmacyList';
import DirectionsPanel   from './components/DirectionsPanel';
import { useGeolocation }  from './hooks/useGeolocation';
import { getDistances, getDirections } from './api/pharmacyApi';

export default function App() {
  const { location, loading: geoLoading, error: geoError, refresh } = useGeolocation();

  const [mode,            setMode]            = useState('driving');
  const [radius,          setRadius]          = useState(15);
  const [activeChains,    setActiveChains]    = useState([]);
  const [availableChains, setAvailableChains] = useState([]);
  const [pharmacies,     setPharmacies]     = useState([]);
  const [listLoading,  setListLoading]  = useState(false);
  const [listError,    setListError]    = useState(null);
  const [selectedPharmacy, setSelected] = useState(null);
  const [directions,   setDirections]   = useState(null);
  const [dirLoading,   setDirLoading]   = useState(false);
  const [statusMsg,    setStatusMsg]    = useState('Allow location access to get started');

  // ── Fetch pharmacies whenever location or travel mode changes ──────────────
  const fetchPharmacies = useCallback(async (loc, travelMode, radiusKm) => {
    if (!loc) return;
    setListLoading(true);
    setListError(null);
    setDirections(null);
    setSelected(null);
    try {
      const data = await getDistances(loc.lat, loc.lng, travelMode, radiusKm);
      setPharmacies(data);

      // Derive unique chains from actual results — reset active filter on fresh fetch
      const chains = [...new Set(data.map(p => p.chain).filter(Boolean))].sort();
      setAvailableChains(chains);
      setActiveChains([]);

      setStatusMsg(data.length
        ? `${data.length} pharmacies found nearby`
        : 'No pharmacies found within 5 km');
    } catch (err) {
      const msg = err.response?.data?.message ?? err.message ?? 'Failed to load pharmacies';
      setListError(msg);
      setStatusMsg('Error loading pharmacies');
    } finally {
      setListLoading(false);
    }
  }, []);

  // Auto-fetch once location is available
  useEffect(() => {
    if (location && !geoLoading) {
      fetchPharmacies(location, mode, radius);
    }
  }, [location, geoLoading]); // eslint-disable-line react-hooks/exhaustive-deps

  // Re-fetch when travel mode changes
  const handleModeChange = (newMode) => {
    setMode(newMode);
    if (location) fetchPharmacies(location, newMode, radius);
  };

  // Re-fetch when radius changes
  const handleRadiusChange = (newRadius) => {
    setRadius(newRadius);
    if (location) fetchPharmacies(location, mode, newRadius);
  };

  // ── Chain filter toggle ────────────────────────────────────────────────────
  const toggleChain = (chain) => {
    setActiveChains(prev =>
      prev.includes(chain) ? prev.filter(c => c !== chain) : [...prev, chain]
    );
  };

  // ── Select a pharmacy (centres map) ───────────────────────────────────────
  const handleSelect = (pharmacy) => {
    setSelected(prev => prev?.id === pharmacy.id ? null : pharmacy);
  };

  // ── Get directions to a pharmacy ──────────────────────────────────────────
  const handleGetDirections = async (pharmacy) => {
    if (!location) return;
    setSelected(pharmacy);
    setDirLoading(true);
    setDirections(null);
    try {
      const data = await getDirections(pharmacy.id, location.lat, location.lng, mode);
      setDirections(data);
      setStatusMsg(`Route to ${pharmacy.name} loaded`);
    } catch (err) {
      setStatusMsg('Could not fetch directions');
    } finally {
      setDirLoading(false);
    }
  };

  const clearDirections = () => {
    setDirections(null);
    setSelected(null);
  };

  const handleLocate = () => {
    refresh();
    setStatusMsg('Updating your location…');
  };

  return (
    <div className="app">
      {/* ── Sidebar ──────────────────────────────────────────────────────── */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <h1>💊 NearPharma</h1>
          <p>Find pharmacies near you, sorted by travel time</p>
        </div>

        <SearchControls
          mode={mode}
          onModeChange={handleModeChange}
          radius={radius}
          onRadiusChange={handleRadiusChange}
          availableChains={availableChains}
          activeChains={activeChains}
          onToggleChain={toggleChain}
          onLocate={handleLocate}
          loading={geoLoading || listLoading}
        />

        {geoError && (
          <div style={{ padding: '8px 16px', fontSize: '.78rem', color: '#E65100', background: '#FFF3E0' }}>
            ⚠️ {geoError}
          </div>
        )}

        <DirectionsPanel
          pharmacy={selectedPharmacy}
          directions={directions}
          onClear={clearDirections}
        />

        <PharmacyList
          pharmacies={pharmacies}
          activeChains={activeChains}
          selectedId={selectedPharmacy?.id}
          onSelect={handleSelect}
          onGetDirections={handleGetDirections}
          loading={listLoading || geoLoading}
          error={listError}
        />
      </aside>

      {/* ── Map ──────────────────────────────────────────────────────────── */}
      <div className="map-wrapper">
        <div className="status-bar">{statusMsg}</div>
        <PharmacyMap
          userLocation={location}
          pharmacies={pharmacies}
          selectedId={selectedPharmacy?.id}
          directions={directions}
          onPharmacySelect={handleGetDirections}
        />
      </div>
    </div>
  );
}
