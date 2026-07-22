import { useState } from 'react';
import { Search, ShieldAlert, ShieldCheck } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import DataTable from '../../../components/common/DataTable.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

export default function AdminUsersPage() {
  const { data, setUserStatus, currentUserId } = useApp();
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  const filteredRows = data.users.filter((user) => user.displayName.toLowerCase().includes(query.toLowerCase()));
  
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
            placeholder="Tìm theo tên hiển thị..." 
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
              setUserStatus(row.id, row.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE');
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
              setPage(1); // Reset to page 1 on page size change
            }
          }}
          columns={[
            { 
              key: 'displayName', 
              label: 'Tên hiển thị',
              render: (row) => (
                <div className="flex items-center gap-3">
                  <div className="h-9 w-9 rounded-full bg-gray-100 flex items-center justify-center text-sm font-bold text-gray-700">
                    {row.displayName?.charAt(0).toUpperCase()}
                  </div>
                  <div className="min-w-0">
                    <p className="font-semibold text-sm text-gray-900 truncate max-w-[180px] sm:max-w-[250px]">{row.displayName}</p>
                    <p className="text-xs text-gray-500 truncate max-w-[180px] sm:max-w-[250px]">{row.email}</p>
                  </div>
                </div>
              ) 
            },
            { 
              key: 'role', 
              label: 'Vai trò',
              render: (row) => (
                <span className={`text-[11px] font-medium px-2.5 py-1 rounded-md ${row.role === 'ADMIN' ? 'bg-gray-800 text-white' : 'bg-gray-100 text-gray-700'}`}>
                  {row.role === 'ADMIN' ? 'Quản trị viên' : 'Người dùng'}
                </span>
              )
            },
            { 
              key: 'status', 
              label: 'Trạng thái', 
              render: (row) => (
                <span className={`inline-flex items-center gap-1.5 text-[11px] font-medium px-2.5 py-1 rounded-md ${row.status === 'ACTIVE' ? 'bg-gray-50 text-gray-700 border border-gray-200' : 'bg-gray-100 text-gray-500'}`}>
                  <span className={`w-1.5 h-1.5 rounded-full ${row.status === 'ACTIVE' ? 'bg-green-500' : 'bg-gray-400'}`}></span>
                  {row.status === 'ACTIVE' ? 'Hoạt động' : 'Đã khóa'}
                </span>
              ) 
            },
            {
              key: 'action',
              label: 'Thao tác',
              className: 'text-right',
              render: (row) => (
                <Button
                  size="sm"
                  variant={row.status === 'ACTIVE' ? 'secondary' : 'primary'}
                  disabled={row.id === currentUserId}
                  onClick={() => setUserStatus(row.id, row.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE')}
                  title="Phím tắt: Enter"
                >
                  {row.status === 'ACTIVE' ? (
                    <><ShieldAlert size={14} /> Khóa</>
                  ) : (
                    <><ShieldCheck size={14} /> Mở khóa</>
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
