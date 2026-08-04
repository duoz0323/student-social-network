import { useEffect, useState } from 'react';
import { AlertTriangle, Ban, FileText, Users } from 'lucide-react';
import { adminApi } from '../../../api/index.js';
import { LoadingState } from '../../../components/common/StateBlock.jsx';

export default function AdminDashboardPage() {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');
  useEffect(() => {
    const controller = new AbortController();
    Promise.all([
      adminApi.getUsers({ page: 0, size: 1 }, controller.signal),
      adminApi.getUsers({ status: 'BLOCKED', page: 0, size: 1 }, controller.signal),
      adminApi.getPosts({ page: 0, size: 1 }, controller.signal),
      adminApi.getModerationCases({ status: 'OPEN', page: 0, size: 1 }, controller.signal),
    ]).then(([users, blocked, posts, reports]) => setStats([
      { label: 'Người dùng', value: users.totalElements, icon: Users },
      { label: 'Bài viết', value: posts.totalElements, icon: FileText },
      { label: 'Báo cáo đang chờ', value: reports.totalElements, icon: AlertTriangle },
      { label: 'Tài khoản khóa', value: blocked.totalElements, icon: Ban },
    ])).catch((requestError) => setError(requestError.message));
    return () => controller.abort();
  }, []);
  if (!stats && !error) return <LoadingState />;
  return <section><h1 className="mb-8 text-4xl font-bold">Bảng điều khiển</h1>

    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">{(stats ?? []).map(({ label, value, icon: Icon }) => (
      <div key={label} className="rounded-xl border bg-white p-6 shadow-sm"><Icon size={20} /><p className="mt-4 text-sm text-gray-500">{label}</p><p className="text-3xl font-bold">{value}</p></div>
    ))}</div>
  </section>;
}
