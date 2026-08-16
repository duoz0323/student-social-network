import { useEffect, useState } from 'react';
import { Filter, Search } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { adminApi } from '../../../api/index.js';
import DataTable from '../../../components/common/DataTable.jsx';
import { LoadingState } from '../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../utils/formatters.js';
import AdminReportAnalytics from '../components/AdminReportAnalytics.jsx';
import {
  ADMIN_REPORT_STATUSES,
  getAdminReportStatusLabel,
} from '../constants/adminReportLabels.js';

const PROFILE_REPORT_STATUSES = [
  { value: 'PENDING', label: 'Chờ xử lý' },
  { value: 'RESOLVED', label: 'Đã xác nhận vi phạm' },
  { value: 'REJECTED', label: 'Không vi phạm' },
];

function getProfileReportStatusLabel(status) {
  return PROFILE_REPORT_STATUSES.find((item) => item.value === status)?.label || status;
}

export default function AdminReportsPage() {
  const navigate = useNavigate();
  const [reportType, setReportType] = useState('POST');
  const [filters, setFilters] = useState({ status: '', keyword: '', postId: '', fromDate: '', toDate: '' });
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [result, setResult] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    const timer = window.setTimeout(() => {
      setLoading(true);
      const request = reportType === 'PROFILE'
        ? adminApi.getProfileReports({
          status: filters.status,
          keyword: filters.keyword,
          page: page - 1,
          size: pageSize,
        }, controller.signal)
        : adminApi.getModerationCases({ ...filters, page: page - 1, size: pageSize }, controller.signal);
      request
        .then((response) => { setResult(response); setError(''); })
        .catch((requestError) => { if (requestError.code !== 'ERR_CANCELED') setError(requestError.message); })
        .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    }, 250);
    return () => { window.clearTimeout(timer); controller.abort(); };
  }, [filters, page, pageSize, reportType]);

  function updateFilter(name, value) {
    setFilters((current) => ({ ...current, [name]: value }));
    setPage(1);
  }

  return (
    <section className="admin-page-with-analytics h-[calc(100dvh-2rem)] min-h-0 sm:h-[calc(100dvh-3rem)] lg:h-[calc(100dvh-4rem)] 2xl:h-[calc(100dvh-6rem)]">
      <div className="flex h-full min-h-0 min-w-0 flex-col">
        <div className="mb-3 flex shrink-0 gap-2 rounded-xl border bg-white p-2">
          {[
            { value: 'POST', label: 'Bài viết' },
            { value: 'PROFILE', label: 'Trang cá nhân' },
          ].map((item) => (
            <button
              key={item.value}
              type="button"
              className={`rounded-lg px-4 py-2 text-sm font-semibold ${reportType === item.value ? 'bg-zinc-900 text-white' : 'text-zinc-600 hover:bg-zinc-100'}`}
              onClick={() => {
                setReportType(item.value);
                setFilters({ status: '', keyword: '', postId: '', fromDate: '', toDate: '' });
                setPage(1);
              }}
            >
              {item.label}
            </button>
          ))}
        </div>
        <div className="mb-4 grid shrink-0 gap-3 rounded-xl border bg-white p-3 md:grid-cols-2 xl:grid-cols-3">
          <label className="relative"><Search className="absolute left-3 top-3" size={15} />
            <input value={filters.keyword} onChange={(event) => updateFilter('keyword', event.target.value)} placeholder={reportType === 'PROFILE' ? 'Tên người bị báo cáo' : 'Nội dung bài hoặc tác giả'} className="w-full rounded-lg border py-2 pl-9 pr-3" />
          </label>
          <select value={filters.status} onChange={(event) => updateFilter('status', event.target.value)} className="rounded-lg border p-2">
            <option value="">Tất cả trạng thái</option>
            {(reportType === 'PROFILE' ? PROFILE_REPORT_STATUSES : ADMIN_REPORT_STATUSES).map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}
          </select>
          {reportType === 'POST' ? <>
            <input type="number" min="1" value={filters.postId} onChange={(event) => updateFilter('postId', event.target.value)} placeholder="Post ID" className="rounded-lg border p-2" />
            <input type="date" value={filters.fromDate} onChange={(event) => updateFilter('fromDate', event.target.value)} className="rounded-lg border p-2" aria-label="Từ ngày" />
            <input type="date" value={filters.toDate} onChange={(event) => updateFilter('toDate', event.target.value)} className="rounded-lg border p-2" aria-label="Đến ngày" />
          </> : null}
          <div className="flex items-center gap-2 text-sm text-gray-500 md:col-span-2 xl:col-span-3"><Filter size={15} /> Tổng: {result.totalElements} {reportType === 'PROFILE' ? 'vụ việc trang cá nhân' : 'hồ sơ kiểm duyệt'}</div>
        </div>
        {error && <p className="mb-4 rounded-xl bg-red-50 p-3 text-red-700">{error}</p>}
        <div className="min-h-0 flex-1 [&>div]:h-full [&>div]:max-h-none">
          {loading ? <LoadingState /> : reportType === 'PROFILE' ? <DataTable
            rows={result.content}
            emptyText="Không có vụ việc báo cáo trang cá nhân"
            onRowDoubleClick={(row) => navigate(`/admin/profile-reports/${row.caseId}`)}
            pagination={{ currentPage: page, totalPages: result.totalPages, onPageChange: setPage, totalItems: result.totalElements, pageSize, onPageSizeChange: (size) => { setPageSize(size); setPage(1); } }}
            columns={[
              { key: 'reportedDisplayName', label: 'Trang cá nhân', sortType: 'text' },
              { key: 'reportCount', label: 'Số báo cáo', sortType: 'number', render: (row) => `${row.reportCount} người` },
              { key: 'status', label: 'Trạng thái', sortType: 'text', render: (row) => getProfileReportStatusLabel(row.status) },
              { key: 'latestReportedAt', label: 'Gần nhất', sortType: 'date', render: (row) => formatDateTime(row.latestReportedAt) },
            ]}
          /> : <DataTable
            rows={result.content}
            emptyText="Không có hồ sơ kiểm duyệt"
            onRowDoubleClick={(row) => navigate(`/admin/reports/${row.caseId}`)}
            pagination={{ currentPage: page, totalPages: result.totalPages, onPageChange: setPage, totalItems: result.totalElements, pageSize, onPageSizeChange: (size) => { setPageSize(size); setPage(1); } }}
            columns={[
              { key: 'post', label: 'Bài viết', sortType: 'text', sortValue: (row) => row.postContentPreview || row.postAuthorDisplayName, render: (row) => <div><p className="max-w-xs truncate">{row.postContentPreview || 'Bài không có nội dung chữ'}</p><span className="text-xs text-zinc-400">{row.postAuthorDisplayName || `User #${row.postAuthorId}`}</span></div> },
              { key: 'reportCount', label: 'Số báo cáo', sortType: 'number', render: (row) => `${row.reportCount} (${row.distinctReporterCount} người)` },
              { key: 'status', label: 'Trạng thái', sortType: 'text', render: (row) => getAdminReportStatusLabel(row.status) },
              { key: 'latestReportedAt', label: 'Gần nhất', sortType: 'date', render: (row) => formatDateTime(row.latestReportedAt) },
              { key: 'resolvedBy', label: 'Admin xử lý', sortType: 'text', sortValue: (row) => row.resolvedByDisplayName, render: (row) => row.resolvedByDisplayName || '—' },
              { key: 'resolvedAt', label: 'Đã xử lý lúc', sortType: 'date', render: (row) => row.resolvedAt ? formatDateTime(row.resolvedAt) : '—' },
            ]}
          />}
        </div>
      </div>
      <div className="admin-page-analytics h-full min-h-0 overflow-hidden">
        <AdminReportAnalytics />
      </div>
    </section>
  );
}
