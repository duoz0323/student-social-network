import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Badge from '../../../components/common/Badge.jsx';
import Button from '../../../components/common/Button.jsx';
import DataTable from '../../../components/common/DataTable.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import { formatDateTime } from '../../../utils/formatters.js';

export default function AdminReportsPage() {
  const { data, getUserById } = useApp();
  const [status, setStatus] = useState('ALL');
  const navigate = useNavigate();
  const rows = data.reports.filter((report) => status === 'ALL' || report.status === status);

  return (
    <section>
      <h1 className="text-3xl font-black">Quan ly bao cao</h1>
      <select value={status} onChange={(event) => setStatus(event.target.value)} className="my-5 rounded-2xl border border-zinc-200 bg-white px-4 py-3">
        <option value="ALL">Tat ca</option>
        <option value="PENDING">PENDING</option>
        <option value="RESOLVED">RESOLVED</option>
        <option value="REJECTED">REJECTED</option>
      </select>
      <DataTable
        rows={rows}
        columns={[
          { key: 'reason', label: 'Ly do' },
          { key: 'reporterId', label: 'Nguoi bao cao', render: (row) => getUserById(row.reporterId)?.displayName },
          { key: 'status', label: 'Trang thai', render: (row) => <Badge tone={row.status === 'PENDING' ? 'warning' : row.status === 'RESOLVED' ? 'success' : 'danger'}>{row.status}</Badge> },
          { key: 'createdAt', label: 'Thoi gian', render: (row) => formatDateTime(row.createdAt) },
          { key: 'action', label: 'Thao tac', render: (row) => <Button size="sm" variant="secondary" onClick={() => navigate(`/admin/reports/${row.id}`)}>Chi tiet</Button> },
        ]}
      />
    </section>
  );
}
