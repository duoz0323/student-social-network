import { useState } from 'react';
import Badge from '../../../components/common/Badge.jsx';
import Button from '../../../components/common/Button.jsx';
import DataTable from '../../../components/common/DataTable.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

export default function AdminUsersPage() {
  const { data, setUserStatus, currentUserId } = useApp();
  const [query, setQuery] = useState('');
  const rows = data.users.filter((user) => user.displayName.toLowerCase().includes(query.toLowerCase()));

  return (
    <section>
      <h1 className="text-3xl font-black">Quan ly nguoi dung</h1>
      <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Tim theo ten hien thi" className="my-5 w-full rounded-2xl border border-zinc-200 bg-white px-4 py-3" />
      <DataTable
        rows={rows}
        columns={[
          { key: 'displayName', label: 'Ten hien thi' },
          { key: 'role', label: 'Role' },
          { key: 'status', label: 'Trang thai', render: (row) => <Badge tone={row.status === 'ACTIVE' ? 'success' : 'danger'}>{row.status}</Badge> },
          {
            key: 'action',
            label: 'Thao tac',
            render: (row) => (
              <Button
                size="sm"
                variant={row.status === 'ACTIVE' ? 'danger' : 'secondary'}
                disabled={row.id === currentUserId}
                onClick={() => setUserStatus(row.id, row.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE')}
              >
                {row.status === 'ACTIVE' ? 'Khoa' : 'Mo khoa'}
              </Button>
            ),
          },
        ]}
      />
    </section>
  );
}
