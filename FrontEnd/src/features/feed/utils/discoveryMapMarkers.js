/** Cluster click chỉ đổi viewport; tuyệt đối không suy ra hoặc chọn một Location đại diện. */
export function zoomToCluster(_event, cluster, map) {
  if (cluster?.bounds && map?.fitBounds) map.fitBounds(cluster.bounds);
}

export function markerAccessibleTitle(location) {
  const count = Math.max(0, Number(location?.postCount) || 0);
  return `${location?.displayName || 'Địa điểm'}, ${count} bài viết`;
}
