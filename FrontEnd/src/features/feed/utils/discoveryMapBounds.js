function finiteCoordinate(value) {
  const coordinate = Number(value);
  return Number.isFinite(coordinate) ? coordinate : null;
}

/** Chuẩn hóa bounds từ Google Maps hoặc object thường và chặn viewport anti-meridian chưa thuộc V1. */
export function normalizeDiscoveryMapBounds(bounds) {
  const literal = typeof bounds?.toJSON === 'function' ? bounds.toJSON() : bounds;
  const north = finiteCoordinate(literal?.north);
  const south = finiteCoordinate(literal?.south);
  const east = finiteCoordinate(literal?.east);
  const west = finiteCoordinate(literal?.west);
  if ([north, south, east, west].some((value) => value === null)) return null;
  if (south < -90 || north > 90 || west < -180 || east > 180) return null;
  if (south >= north || west >= east) return null;
  return { north, south, east, west };
}
