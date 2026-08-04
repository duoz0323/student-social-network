import { EmptyState } from './StateBlock.jsx';
import Pagination from './Pagination.jsx';

export default function DataTable({
  columns,
  rows = [],
  loading = false,
  loadingText = 'Đang tải dữ liệu...',
  emptyText = 'Không có dữ liệu',
  pagination, // { currentPage, totalPages, onPageChange, totalItems, pageSize, onPageSizeChange }
  onRowKeyDown,
  onRowClick,
  onRowDoubleClick,
}) {
  // Loading và empty nằm trong cùng khung bảng để layout Admin không bị nhảy khi dữ liệu đổi trạng thái.
  if (!loading && !rows.length) return (
    <div className="overflow-hidden rounded-[var(--radius-card)] border border-[var(--app-border)] bg-[var(--app-surface)]">
      <EmptyState title={emptyText} description="Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm." />
    </div>
  );

  const hasRowAction = Boolean(onRowClick || onRowDoubleClick);

  const handleKeyDown = (e, index, row) => {
    // Không biến thao tác bàn phím trên button/link bên trong thành click của cả hàng.
    if (e.target !== e.currentTarget) return;
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      const nextRow = document.getElementById(`dt-row-${index + 1}`);
      if (nextRow) nextRow.focus();
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      const prevRow = document.getElementById(`dt-row-${index - 1}`);
      if (prevRow) prevRow.focus();
    } else if ((e.key === 'Enter' || e.key === ' ') && hasRowAction) {
      e.preventDefault();
      // Enter hoặc Space thay thế thao tác chuột để hàng double-click vẫn truy cập được bằng bàn phím.
      (onRowClick || onRowDoubleClick)(row);
    } else if (onRowKeyDown) {
      onRowKeyDown(e, row);
    }
  };

  return (
    <div className="flex max-h-[calc(100vh-220px)] flex-col overflow-hidden rounded-[var(--radius-card)] border border-[var(--app-border)] bg-[var(--app-surface)]">
      <div className="overflow-x-auto overflow-y-auto flex-1 custom-scrollbar">
        <table className="w-full min-w-[720px] border-collapse text-left text-sm relative">
          <thead className="sticky top-0 z-10 border-b border-[var(--app-border)] bg-[var(--app-surface-soft)]">
            <tr>
              {columns.map((column) => (
                <th
                  key={column.key}
                  className={`whitespace-nowrap px-4 py-3 text-xs font-semibold uppercase tracking-[0.04em] text-[var(--app-muted)] ${column.className || ''}`}
                >
                  {column.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--app-border)] bg-[var(--app-surface)]">
            {loading ? (
              <tr>
                <td colSpan={columns.length} className="h-32 px-4 text-center text-sm font-medium text-[var(--app-muted)]" role="status" aria-live="polite">
                  {loadingText}
                </td>
              </tr>
            ) : rows.map((row, index) => (
              <tr 
                key={row.id ?? row.userId ?? row.caseId ?? row.postId ?? row.reportId ?? row.actionId ?? index}
                id={`dt-row-${index}`}
                tabIndex={0}
                onKeyDown={(e) => handleKeyDown(e, index, row)}
                onClick={onRowClick ? () => onRowClick(row) : undefined}
                onDoubleClick={onRowDoubleClick ? () => onRowDoubleClick(row) : undefined}
                className={`group transition-colors duration-[var(--motion-fast)] hover:bg-[var(--app-surface-soft)] focus:bg-[var(--app-surface-soft)] focus:outline-none focus:ring-2 focus:ring-inset focus:ring-[var(--app-brand)] ${hasRowAction ? 'cursor-pointer' : 'cursor-default'}`}
              >
                {columns.map((column) => (
                  <td
                    key={column.key}
                    className={`px-4 py-3 text-[var(--app-text)] ${column.className || ''}`}
                  >
                    {column.render ? column.render(row) : row[column.key]}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {!loading && pagination ? <Pagination {...pagination} /> : null}
    </div>
  );
}
