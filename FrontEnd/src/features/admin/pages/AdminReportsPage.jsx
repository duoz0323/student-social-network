import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Filter, Eye } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import DataTable from '../../../components/common/DataTable.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import { formatDateTime } from '../../../utils/formatters.js';

export default function AdminReportsPage() {
  const { data, getUserById } = useApp();
  const [status, setStatus] = useState('ALL');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  
  const navigate = useNavigate();

  const filteredRows = data.reports.filter((report) => status === 'ALL' || report.status === status);

  const totalItems = filteredRows.length;
  const totalPages = Math.ceil(totalItems / pageSize);
  
  // Frontend pagination simulation
  const paginatedRows = filteredRows.slice((page - 1) * pageSize, page * pageSize);

  const handleFilterChange = (e) => {
    setStatus(e.target.value);
    setPage(1);
  };

  return (
    <section>
      {/* Header */}
      

      {/* Toolbar */}
      <div className="mb-6 flex flex-col sm:flex-row gap-4 justify-between items-center bg-white p-3 rounded-xl border border-gray-100 shadow-sm animate-slide-up-delayed-1">
        <div className="relative w-full sm:max-w-xs">
          <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-gray-400">
            <Filter size={16} />
          </div>
          <select 
            value={status} 
            onChange={handleFilterChange} 
            className="w-full appearance-none rounded-lg border border-gray-200 bg-gray-50 pl-9 pr-10 py-2 text-sm font-medium focus:outline-none focus:ring-1 focus:ring-gray-400 focus:border-gray-400 focus:bg-white transition-all cursor-pointer shadow-sm"
          >
            <option value="ALL">Tất cả trạng thái</option>
            <option value="PENDING">Chờ xử lý (PENDING)</option>
            <option value="RESOLVED">Đã giải quyết (RESOLVED)</option>
            <option value="REJECTED">Bị từ chối (REJECTED)</option>
          </select>
          {/* Custom Select Arrow */}
          <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-gray-400">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7"></path></svg>
          </div>
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
              navigate(`/admin/reports/${row.id}`);
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
              key: 'reason', 
              label: 'Lý do báo cáo',
              className: 'max-w-[200px] sm:max-w-[300px]',
              render: (row) => <div className="font-semibold text-sm text-gray-900 truncate" title={row.reason}>{row.reason}</div>
            },
            { 
              key: 'reporterId', 
              label: 'Người báo cáo', 
              render: (row) => (
                <div className="flex items-center gap-2">
                  <div className="h-7 w-7 rounded-lg bg-gray-100 border border-gray-200 flex items-center justify-center text-xs font-bold text-gray-700">
                    {getUserById(row.reporterId)?.displayName?.charAt(0).toUpperCase()}
                  </div>
                  <span className="font-medium text-sm text-gray-700">{getUserById(row.reporterId)?.displayName}</span>
                </div>
              ) 
            },
            { 
              key: 'status', 
              label: 'Trạng thái', 
              render: (row) => (
                <span className={`inline-flex items-center gap-1.5 text-[11px] font-medium px-2.5 py-1 rounded-md ${row.status === 'PENDING' ? 'bg-orange-50 text-orange-700 border border-orange-100' : row.status === 'RESOLVED' ? 'bg-green-50 text-green-700 border border-green-100' : 'bg-gray-100 text-gray-600'}`}>
                  <span className={`w-1.5 h-1.5 rounded-full ${row.status === 'PENDING' ? 'bg-orange-500' : row.status === 'RESOLVED' ? 'bg-green-500' : 'bg-gray-400'}`}></span>
                  {row.status === 'PENDING' ? 'Chờ xử lý' : row.status === 'RESOLVED' ? 'Đã duyệt' : 'Từ chối'}
                </span>
              ) 
            },
            { 
              key: 'createdAt', 
              label: 'Thời gian', 
              render: (row) => <span className="text-xs text-gray-500">{formatDateTime(row.createdAt)}</span> 
            },
            { 
              key: 'action', 
              label: '',
              className: 'text-right',
              render: (row) => (
                <Button 
                  size="sm" 
                  variant="secondary" 
                  onClick={() => navigate(`/admin/reports/${row.id}`)}
                  title="Phím tắt: Enter"
                >
                  <Eye size={14} /> Chi tiết
                </Button>
              ) 
            },
          ]}
        />
      </div>
    </section>
  );
}
