import { loadPlacesLibrary } from './googleMapsLoader.js';

export async function createPlacesSessionToken() {
  const { AutocompleteSessionToken } = await loadPlacesLibrary();
  return new AutocompleteSessionToken();
}

/** Tìm gợi ý bằng Places Autocomplete Data API, không dùng vị trí GPS của người dùng. */
export async function fetchPlaceSuggestions(input, sessionToken) {
  const { AutocompleteSuggestion } = await loadPlacesLibrary();
  const { suggestions } = await AutocompleteSuggestion.fetchAutocompleteSuggestions({
    input,
    sessionToken,
    includedRegionCodes: ['vn'],
    language: 'vi',
  });
  return suggestions
    .filter((item) => item.placePrediction)
    .map((item) => ({
      placeId: item.placePrediction.placeId,
      label: item.placePrediction.text.toString(),
      prediction: item.placePrediction,
    }));
}

export async function resolvePlaceSuggestion(suggestion) {
  const place = suggestion.prediction.toPlace();
  await place.fetchFields({ fields: ['id', 'displayName', 'formattedAddress', 'location'] });
  if (!place.id || !place.displayName || !place.location) {
    throw new Error('Google Places không trả đủ dữ liệu địa điểm.');
  }
  return {
    placeId: place.id,
    displayName: place.displayName,
    formattedAddress: place.formattedAddress ?? null,
    latitude: place.location.lat(),
    longitude: place.location.lng(),
  };
}
