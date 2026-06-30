import { useState } from 'react';
import Badge from '../../../components/common/Badge.jsx';
import Button from '../../../components/common/Button.jsx';
import DataTable from '../../../components/common/DataTable.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

export default function AdminPostsPage() {
  const { data, getUserById, setPostStatus } = useApp();
  const [query, setQuery] = useState('');
  const rows = data.posts.filter((post) => post.content.toLowerCase().includes(query.toLowerCase()));

  return (
    <section>
      <h1 className="text-3xl font-black">Quan ly bai viet</h1>
      <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Tim theo noi dung bai viet" className="my-5 w-full rounded-2xl border border-zinc-200 bg-white px-4 py-3" />
      <DataTable
        rows={rows}
        columns={[
          { key: 'authorId', label: 'Tac gia', render: (row) => getUserById(row.authorId)?.displayName },
          { key: 'content', label: 'Noi dung', render: (row) => <span className="line-clamp-1">{row.content}</span> },
          { key: 'status', label: 'Trang thai', render: (row) => <Badge tone={row.status === 'PUBLISHED' ? 'success' : row.status === 'HIDDEN' ? 'warning' : 'danger'}>{row.status}</Badge> },
          {
            key: 'action',
            label: 'Thao tac',
            render: (row) => (
              <Button size="sm" variant="secondary" onClick={() => setPostStatus(row.id, row.status === 'HIDDEN' ? 'PUBLISHED' : 'HIDDEN')}>
                {row.status === 'HIDDEN' ? 'Khoi phuc' : 'An bai'}
              </Button>
            ),
          },
        ]}
      />
    </section>
  );
}
