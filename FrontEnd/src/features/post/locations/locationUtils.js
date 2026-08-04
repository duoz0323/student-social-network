export function googleMapsLocationUrl(location) {
  if (!location) return '#';
  const query = location.displayName || `${location.latitude},${location.longitude}`;
  const params = new URLSearchParams({ api: '1', query });
  if (location.placeId) params.set('query_place_id', location.placeId);
  return `https://www.google.com/maps/search/?${params}`;
}
