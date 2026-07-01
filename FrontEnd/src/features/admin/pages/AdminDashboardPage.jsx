import Badge from '../../../components/common/Badge.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

export default function AdminDashboardPage() {
  const { data } = useApp();
  const cards = [
    { label: 'Người dùng', value: data.users.length },
    { label: 'Bài viết', value: data.posts.length },
    { label: 'Báo cáo chờ xử lý', value: data.reports.filter((report) => report.status === 'PENDING').length },
    { label: 'Tài khoản bị khóa', value: data.users.filter((user) => user.status === 'BLOCKED').length },
  ];

  return (
    <section>
      <h1 className="text-3xl font-black">Tổng quan quản trị</h1>
      <p className="mt-2 text-zinc-500">Chỉ số cơ bản cho MVP, không triển khai dashboard nâng cao.</p>
      <div className="mt-6 grid grid-cols-4 gap-4">
        {cards.map((card) => (
          <div key={card.label} className="rounded-2xl border border-zinc-200 bg-white p-5">
            <p className="text-sm text-zinc-500">{card.label}</p>
            <p className="mt-3 text-3xl font-black">{card.value}</p>
          </div>
        ))}
      </div>
      <div className="mt-6 rounded-2xl border border-zinc-200 bg-white p-5">
        <h2 className="font-black">Trạng thái phạm vi</h2>
        <div className="mt-3 flex gap-2">
          <Badge tone="success">MVP_CURRENT</Badge>
          <Badge tone="warning">Mock data</Badge>
        </div>
      </div>
    </section>
  );
}
