import { useState } from 'react';
import { Search, Eye, EyeOff } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import DataTable from '../../../components/common/DataTable.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

export default function AdminPostsPage() {
  const { data, getUserById, setPostStatus } = useApp();
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  const filteredRows = data.posts.filter((post) => post.content.toLowerCase().includes(query.toLowerCase()));

  const totalItems = filteredRows.length;
  const totalPages = Math.ceil(totalItems / pageSize);
  
  // Frontend pagination simulation
  const paginatedRows = filteredRows.slice((page - 1) * pageSize, page * pageSize);

  const handleSearch = (e) => {
    setQuery(e.target.value);
    setPage(1);
  };

  return (
    <section>
      

      {/* Toolbar */}
      <div className="mb-6 flex flex-col sm:flex-row gap-4 justify-between items-center bg-white p-3 rounded-xl border border-gray-100 shadow-sm animate-slide-up-delayed-1">
        <div className="relative w-full sm:max-w-md">
          <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-gray-400">
            <Search size={16} />
          </div>
          <input 
            value={query} 
            onChange={handleSearch} 
            placeholder="Tìm theo nội dung bài viết..." 
            className="w-full rounded-lg border border-gray-200 bg-gray-50 pl-9 pr-4 py-2 text-sm font-medium focus:outline-none focus:ring-1 focus:ring-gray-400 focus:border-gray-400 focus:bg-white transition-all shadow-sm" 
          />
        </div>
        <div className="text-sm font-medium text-gray-500 px-2">
          Tổng cộng: <span className="text-gray-900 font-semibold">{totalItems}</span>
        </div>
      </div>

      {/* Data Table */}
      <div className="rounded-xl border border-gray-100 bg-white shadow-sm overflow-hidden mb-6 animate-slide-up-delayed-2">
        <DataTable
          rows={paginatedRows}
          onRowKeyDown={(e, row) => {
            if (e.key === 'Enter') {
              setPostStatus(row.id, row.status === 'HIDDEN' ? 'PUBLISHED' : 'HIDDEN');
            }
          }}
          pagination={{
            currentPage: page,
            totalPages,
            onPageChange: setPage,
            totalItems,
            pageSize,
            onPageSizeChange: (newSize) => {
              setPageSize(newSize);
              setPage(1);
            }
          }}
          columns={[
            { 
              key: 'authorId', 
              label: 'Tác giả', 
              render: (row) => (
                <div className="flex items-center gap-2">
                  <div className="h-7 w-7 rounded-lg bg-gray-100 border border-gray-200 flex items-center justify-center text-xs font-bold text-gray-700">
                    {getUserById(row.authorId)?.displayName?.charAt(0).toUpperCase()}
                  </div>
                  <span className="font-semibold text-sm text-gray-900">{getUserById(row.authorId)?.displayName}</span>
                </div>
              ) 
            },
            { 
              key: 'content', 
              label: 'Nội dung', 
              className: 'max-w-[250px] sm:max-w-[400px]',
              render: (row) => <div className="truncate text-sm text-gray-600" title={row.content}>{row.content}</div> 
            },
            { 
              key: 'status', 
              label: 'Trạng thái', 
              render: (row) => (
                <span className={`inline-flex items-center gap-1.5 text-[11px] font-medium px-2.5 py-1 rounded-md ${row.status === 'PUBLISHED' ? 'bg-gray-50 text-gray-700 border border-gray-200' : row.status === 'HIDDEN' ? 'bg-gray-100 text-gray-500' : 'bg-gray-800 text-white'}`}>
                  <span className={`w-1.5 h-1.5 rounded-full ${row.status === 'PUBLISHED' ? 'bg-green-500' : row.status === 'HIDDEN' ? 'bg-orange-500' : 'bg-gray-400'}`}></span>
                  {row.status === 'PUBLISHED' ? 'Đã đăng' : row.status === 'HIDDEN' ? 'Đã ẩn' : row.status}
                </span>
              ) 
            },
            {
              key: 'action',
              label: '',
              className: 'text-right',
              render: (row) => (
                <Button 
                  size="sm" 
                  variant={row.status === 'HIDDEN' ? 'primary' : 'secondary'} 
                  onClick={() => setPostStatus(row.id, row.status === 'HIDDEN' ? 'PUBLISHED' : 'HIDDEN')}
                  title="Phím tắt: Enter"
                >
                  {row.status === 'HIDDEN' ? (
                    <><Eye size={14} /> Khôi phục</>
                  ) : (
                    <><EyeOff size={14} /> Ẩn bài</>
                  )}
                </Button>
              ),
            },
          ]}
        />
      </div>
    </section>
  );
}
