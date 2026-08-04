// Tính vị trí gợi ý kế tiếp khi người dùng điều hướng bằng phím mũi tên.
export function moveSearchSuggestionIndex(currentIndex, optionCount, direction) {
  if (optionCount <= 0) return -1;

  if (direction === 'up') {
    return currentIndex <= 0 ? optionCount - 1 : currentIndex - 1;
  }

  return currentIndex < 0 || currentIndex >= optionCount - 1 ? 0 : currentIndex + 1;
}
