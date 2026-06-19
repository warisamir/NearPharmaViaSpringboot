import { useEffect, useRef } from 'react';
import {
  MapContainer, TileLayer, Marker, Popup,
  Polyline, CircleMarker, useMap,
} from 'react-leaflet';
import L from 'leaflet';

// Fix default icon paths broken by Vite bundling
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconUrl:       new URL('leaflet/dist/images/marker-icon.png',    import.meta.url).href,
  iconRetinaUrl: new URL('leaflet/dist/images/marker-icon-2x.png', import.meta.url).href,
  shadowUrl:     new URL('leaflet/dist/images/marker-shadow.png',  import.meta.url).href,
});

/** Custom green pin for pharmacies */
const pharmacyIcon = (selected) =>
  L.divIcon({
    className: '',
    html: `<div style="
      width:28px;height:28px;
      background:${selected ? '#FF7043' : '#00897B'};
      border:2.5px solid #fff;
      border-radius:50% 50% 50% 0;
      transform:rotate(-45deg);
      box-shadow:0 2px 6px rgba(0,0,0,.25);
      transition:background .2s;
    "></div>`,
    iconSize:   [28, 28],
    iconAnchor: [14, 28],
    popupAnchor:[0, -30],
  });

/** Fits map to show all markers whenever pharmacies or location change */
function BoundsFitter({ pharmacies, userLocation }) {
  const map = useMap();

  useEffect(() => {
    if (!userLocation) return;
    const points = [
      [userLocation.lat, userLocation.lng],
      ...pharmacies.map(p => [p.coordinates?.lat ?? p.latitude, p.coordinates?.lng ?? p.longitude]),
    ];
    if (points.length === 1) {
      map.setView(points[0], 14);
    } else {
      map.fitBounds(L.latLngBounds(points), { padding: [50, 50] });
    }
  }, [pharmacies, userLocation, map]);

  return null;
}

export default function PharmacyMap({
  userLocation,
  pharmacies,
  selectedId,
  directions,
  onPharmacySelect,
}) {
  const center = userLocation
    ? [userLocation.lat, userLocation.lng]
    : [12.9716, 77.5946];

  // Extract route coordinates from TrueWay Directions response
  // TrueWay returns: { route: { geometry: { coordinates: [[lng,lat], ...] } } }
  const routePositions = (() => {
    try {
      const coords = directions?.route?.geometry?.coordinates;
      if (!coords) return null;
      return coords.map(([lng, lat]) => [lat, lng]); // GeoJSON is [lng,lat], Leaflet needs [lat,lng]
    } catch {
      return null;
    }
  })();

  const formatDistance = (d) => d?.distance ?? '';
  const formatDuration = (d) => d?.duration ?? '';

  return (
    <MapContainer center={center} zoom={13} style={{ height: '100%', width: '100%' }}>
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      <BoundsFitter pharmacies={pharmacies} userLocation={userLocation} />

      {/* User location — pulsing blue dot */}
      {userLocation && (
        <CircleMarker
          center={[userLocation.lat, userLocation.lng]}
          radius={10}
          pathOptions={{ color: '#1565C0', fillColor: '#1565C0', fillOpacity: 0.9, weight: 3 }}
        >
          <Popup>
            <strong>📍 You are here</strong><br />
            {userLocation.lat.toFixed(5)}, {userLocation.lng.toFixed(5)}
          </Popup>
        </CircleMarker>
      )}

      {/* Pharmacy markers */}
      {pharmacies.map((p) => {
        const lat = p.coordinates?.lat ?? p.latitude;
        const lng = p.coordinates?.lng ?? p.longitude;
        if (!lat || !lng) return null;
        return (
          <Marker
            key={p.id ?? p.name}
            position={[lat, lng]}
            icon={pharmacyIcon(p.id === selectedId)}
            eventHandlers={{ click: () => onPharmacySelect(p) }}
          >
            <Popup>
              <div style={{ minWidth: 160 }}>
                <strong>{p.name}</strong><br />
                <span style={{ fontSize: '.8rem', color: '#666' }}>{p.address}</span>
                {p.distance && (
                  <div style={{ marginTop: 4, fontSize: '.8rem' }}>
                    📏 {p.distance} &nbsp; ⏱ {p.duration}
                  </div>
                )}
                <button
                  onClick={() => onPharmacySelect(p)}
                  style={{
                    marginTop: 8, width: '100%', padding: '5px 0',
                    background: '#00897B', color: '#fff', border: 'none',
                    borderRadius: 6, cursor: 'pointer', fontSize: '.8rem', fontWeight: 600,
                  }}
                >
                  Get Directions
                </button>
              </div>
            </Popup>
          </Marker>
        );
      })}

      {/* Route polyline */}
      {routePositions && (
        <Polyline
          positions={routePositions}
          pathOptions={{ color: '#1565C0', weight: 5, opacity: .75, dashArray: '8 4' }}
        />
      )}
    </MapContainer>
  );
}
