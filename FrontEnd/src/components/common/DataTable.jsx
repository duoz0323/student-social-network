import { EmptyState } from './StateBlock.jsx';
import Pagination from './Pagination.jsx';

export default function DataTable({ 
  columns, 
  rows, 
  emptyText = 'Không có dữ liệu',
  pagination, // { currentPage, totalPages, onPageChange, totalItems, pageSize, onPageSizeChange }
  onRowKeyDown,
  onRowClick,
  onRowDoubleClick,
}) {
  if (!rows.length) return <EmptyState title={emptyText} description="Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm." />;

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
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm flex flex-col max-h-[calc(100vh-220px)]">
      <div className="overflow-x-auto overflow-y-auto flex-1 custom-scrollbar">
        <table className="w-full min-w-[720px] border-collapse text-left text-sm relative">
          <thead className="bg-gray-50/95 backdrop-blur sticky top-0 z-10 border-b border-gray-200 shadow-sm">
            <tr>
              {columns.map((column) => (
                <th 
                  key={column.key} 
                  className={`px-5 py-3.5 text-xs font-semibold text-gray-500 uppercase tracking-wider whitespace-nowrap ${column.className || ''}`}
                >
                  {column.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white">
            {rows.map((row, index) => (
              <tr 
                key={row.id ?? row.userId ?? row.postId ?? row.reportId ?? row.actionId ?? index}
                id={`dt-row-${index}`}
                tabIndex={0}
                onKeyDown={(e) => handleKeyDown(e, index, row)}
                onClick={onRowClick ? () => onRowClick(row) : undefined}
                onDoubleClick={onRowDoubleClick ? () => onRowDoubleClick(row) : undefined}
                className={`hover:bg-blue-50/40 focus:bg-blue-50 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-blue-400 transition-colors duration-150 group ${hasRowAction ? 'cursor-pointer' : 'cursor-default'}`}
              >
                {columns.map((column) => (
                  <td 
                    key={column.key} 
                    className={`px-5 py-3 text-gray-700 ${column.className || ''}`}
                  >
                    {column.render ? column.render(row) : row[column.key]}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {pagination && <Pagination {...pagination} />}
    </div>
  );
}
