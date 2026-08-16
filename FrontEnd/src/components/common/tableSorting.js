const textCollator = new Intl.Collator('vi', { sensitivity: 'base', numeric: true });

export function nextSortDirection(currentDirection, type = 'text') {
  if (!currentDirection) return type === 'date' ? 'desc' : 'asc';
  if (type === 'date') return currentDirection === 'desc' ? 'asc' : null;
  return currentDirection === 'asc' ? 'desc' : null;
}

function comparableValue(value, type) {
  if (value === null || value === undefined || value === '') return null;
  if (type === 'number') {
    const number = Number(value);
    return Number.isNaN(number) ? null : number;
  }
  if (type === 'date') {
    const timestamp = new Date(value).getTime();
    return Number.isNaN(timestamp) ? null : timestamp;
  }
  if (type === 'boolean') return value ? 1 : 0;
  return String(value).trim();
}

export function sortTableRows(rows, column, direction) {
  if (!column || !direction) return rows;
  const type = column.sortType || 'text';
  const getValue = column.sortValue || ((row) => row[column.key]);

  return rows.map((row, index) => ({ row, index })).sort((left, right) => {
    const leftValue = comparableValue(getValue(left.row), type);
    const rightValue = comparableValue(getValue(right.row), type);
    if (leftValue === null && rightValue === null) return left.index - right.index;
    if (leftValue === null) return 1;
    if (rightValue === null) return -1;

    const comparison = type === 'text'
      ? textCollator.compare(leftValue, rightValue)
      : leftValue - rightValue;
    return comparison === 0
      ? left.index - right.index
      : comparison * (direction === 'asc' ? 1 : -1);
  }).map((item) => item.row);
}
