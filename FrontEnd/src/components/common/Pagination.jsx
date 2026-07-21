import { ChevronLeft, ChevronRight, MoreHorizontal } from 'lucide-react';
import Button from './Button.jsx';

export default function Pagination({ 
  currentPage, 
  totalPages, 
  onPageChange,
  totalItems,
  pageSize,
  onPageSizeChange // New prop for dynamic page size
}) {
  if (totalPages <= 1 && totalItems <= 10) return null; // Only hide if total items is small too

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
    <div className="flex items-center justify-between border-t border-gray-100 bg-white px-4 py-3 sm:px-6">
      <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
        <div className="flex items-center gap-4">
          {totalItems !== undefined && (
            <p className="text-sm text-gray-500">
              Hiển thị <span className="font-semibold text-gray-900">{totalItems > 0 ? (currentPage - 1) * pageSize + 1 : 0}</span> đến{' '}
              <span className="font-semibold text-gray-900">
                {Math.min(currentPage * pageSize, totalItems)}
              </span>{' '}
              trong <span className="font-semibold text-gray-900">{totalItems}</span>
            </p>
          )}
          {onPageSizeChange && (
            <div className="flex items-center gap-2">
              <span className="text-sm text-gray-500">Số dòng:</span>
              <select
                value={pageSize}
                onChange={(e) => onPageSizeChange(Number(e.target.value))}
                className="text-sm border-gray-200 rounded-md py-1 pl-2 pr-6 focus:ring-gray-400 focus:border-gray-400"
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
          <nav className="isolate inline-flex -space-x-px rounded-lg shadow-sm" aria-label="Pagination">
            <button
              onClick={() => onPageChange(currentPage - 1)}
              disabled={currentPage === 1}
              className="relative inline-flex items-center rounded-l-lg px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-200 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span className="sr-only">Previous</span>
              <ChevronLeft className="h-4 w-4" aria-hidden="true" />
            </button>
            
            {getPageNumbers().map((page, index) => {
              if (page === '...') {
                return (
                  <span
                    key={`ellipsis-${index}`}
                    className="relative inline-flex items-center px-4 py-2 text-sm font-semibold text-gray-700 ring-1 ring-inset ring-gray-200 focus:outline-offset-0"
                  >
                    <MoreHorizontal className="h-4 w-4 text-gray-400" />
                  </span>
                );
              }

              return (
                <button
                  key={page}
                  onClick={() => onPageChange(page)}
                  aria-current={page === currentPage ? 'page' : undefined}
                  className={`relative inline-flex items-center px-4 py-2 text-sm font-semibold focus:z-20 focus:outline-offset-0 ring-1 ring-inset ring-gray-200 ${
                    page === currentPage
                      ? 'z-10 bg-gray-900 text-white hover:bg-gray-800'
                      : 'text-gray-900 hover:bg-gray-50'
                  }`}
                >
                  {page}
                </button>
              );
            })}
            
            <button
              onClick={() => onPageChange(currentPage + 1)}
              disabled={currentPage === totalPages}
              className="relative inline-flex items-center rounded-r-lg px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-200 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span className="sr-only">Next</span>
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
          className="text-xs bg-white text-gray-700 border-gray-200 hover:bg-gray-50"
        >
          Trước
        </Button>
        <span className="text-sm font-medium text-gray-700 self-center">
          Trang {currentPage} / {totalPages || 1}
        </span>
        <Button
          variant="secondary"
          disabled={currentPage === totalPages || totalPages === 0}
          onClick={() => onPageChange(currentPage + 1)}
          className="text-xs bg-white text-gray-700 border-gray-200 hover:bg-gray-50"
        >
          Sau
        </Button>
      </div>
    </div>
  );
}
