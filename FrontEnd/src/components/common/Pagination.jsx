import { ChevronLeft, ChevronRight, MoreHorizontal } from 'lucide-react';
import Button from './Button.jsx';

export default function Pagination({ 
  currentPage = 1,
  totalPages = 0,
  onPageChange,
  totalItems,
  pageSize = 10,
  onPageSizeChange // New prop for dynamic page size
}) {
  // Một trang không cần điều hướng; ẩn control giúp giảm nhiễu nhưng không thay đổi dữ liệu đang hiển thị.
  if (totalPages <= 1) return null;

  // Generate page numbers to display
  const getPageNumbers = () => {
    const pages = [];
    if (totalPages <= 7) {
      for (let i = 1; i <= totalPages; i++) pages.push(i);
    } else {
      if (currentPage <= 4) {
        pages.push(1, 2, 3, 4, 5, '...', totalPages);
      } else if (currentPage >= totalPages - 3) {
        pages.push(1, '...', totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1, totalPages);
      } else {
        pages.push(1, '...', currentPage - 1, currentPage, currentPage + 1, '...', totalPages);
      }
    }
    return pages;
  };

  return (
    <div className="flex items-center justify-between border-t border-[var(--app-border)] bg-[var(--app-surface)] px-4 py-3">
      <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
        <div className="flex items-center gap-4">
          {totalItems !== undefined && (
            <p className="text-sm text-[var(--app-muted)]">
              Hiển thị <span className="font-semibold text-[var(--app-text)]">{totalItems > 0 ? (currentPage - 1) * pageSize + 1 : 0}</span> đến{' '}
              <span className="font-semibold text-[var(--app-text)]">
                {Math.min(currentPage * pageSize, totalItems)}
              </span>{' '}
              trong <span className="font-semibold text-[var(--app-text)]">{totalItems}</span>
            </p>
          )}
          {onPageSizeChange && (
            <div className="flex items-center gap-2">
              <span className="text-sm text-[var(--app-muted)]">Số dòng:</span>
              <select
                value={pageSize}
                onChange={(e) => onPageSizeChange(Number(e.target.value))}
                className="rounded-[var(--radius-control)] border border-[var(--app-border-strong)] bg-[var(--app-control-bg)] py-1.5 pl-2.5 pr-7 text-sm text-[var(--app-text)] outline-none focus:border-[var(--app-brand)] focus:ring-2 focus:ring-[var(--app-focus-ring)]"
              >
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
                <option value={100}>100</option>
              </select>
            </div>
          )}
        </div>
        <div>
          <nav className="isolate inline-flex -space-x-px overflow-hidden rounded-[var(--radius-control)]" aria-label="Phân trang">
            <button
              onClick={() => onPageChange(currentPage - 1)}
              disabled={currentPage === 1}
              className="relative inline-flex items-center border border-[var(--app-border-strong)] bg-[var(--app-surface)] px-2.5 py-2 text-[var(--app-muted)] outline-none hover:bg-[var(--app-surface-soft)] focus:z-20 focus:ring-2 focus:ring-inset focus:ring-[var(--app-brand)] disabled:cursor-not-allowed disabled:opacity-40"
            >
              <span className="sr-only">Trang trước</span>
              <ChevronLeft className="h-4 w-4" aria-hidden="true" />
            </button>
            
            {getPageNumbers().map((page, index) => {
              if (page === '...') {
                return (
                  <span
                    key={`ellipsis-${index}`}
                    className="relative inline-flex items-center border border-[var(--app-border-strong)] bg-[var(--app-surface)] px-3 py-2 text-sm font-semibold text-[var(--app-muted)]"
                  >
                    <MoreHorizontal className="h-4 w-4" />
                  </span>
                );
              }

              return (
                <button
                  key={page}
                  onClick={() => onPageChange(page)}
                  aria-current={page === currentPage ? 'page' : undefined}
                  className={`relative inline-flex items-center border border-[var(--app-border-strong)] px-3.5 py-2 text-sm font-semibold outline-none focus:z-20 focus:ring-2 focus:ring-inset focus:ring-[var(--app-brand)] ${
                    page === currentPage
                      ? 'z-10 bg-[var(--app-active)] text-white'
                      : 'bg-[var(--app-surface)] text-[var(--app-text)] hover:bg-[var(--app-surface-soft)]'
                  }`}
                >
                  {page}
                </button>
              );
            })}
            
            <button
              onClick={() => onPageChange(currentPage + 1)}
              disabled={currentPage === totalPages}
              className="relative inline-flex items-center border border-[var(--app-border-strong)] bg-[var(--app-surface)] px-2.5 py-2 text-[var(--app-muted)] outline-none hover:bg-[var(--app-surface-soft)] focus:z-20 focus:ring-2 focus:ring-inset focus:ring-[var(--app-brand)] disabled:cursor-not-allowed disabled:opacity-40"
            >
              <span className="sr-only">Trang sau</span>
              <ChevronRight className="h-4 w-4" aria-hidden="true" />
            </button>
          </nav>
        </div>
      </div>
      
      {/* Mobile Pagination */}
      <div className="flex flex-1 justify-between sm:hidden">
        <Button
          variant="secondary"
          disabled={currentPage === 1}
          onClick={() => onPageChange(currentPage - 1)}
          className="text-xs"
        >
          Trước
        </Button>
        <span className="self-center text-sm font-medium text-[var(--app-muted)]">
          Trang {currentPage} / {totalPages || 1}
        </span>
        <Button
          variant="secondary"
          disabled={currentPage === totalPages || totalPages === 0}
          onClick={() => onPageChange(currentPage + 1)}
          className="text-xs"
        >
          Sau
        </Button>
      </div>
    </div>
  );
}
