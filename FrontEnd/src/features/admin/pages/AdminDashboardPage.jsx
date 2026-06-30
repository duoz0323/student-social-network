import Badge from '../../../components/common/Badge.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

export default function AdminDashboardPage() {
  const { data } = useApp();
  const cards = [
    { label: 'Nguoi dung', value: data.users.length },
    { label: 'Bai viet', value: data.posts.length },
    { label: 'Bao cao cho xu ly', value: data.reports.filter((report) => report.status === 'PENDING').length },
    { label: 'Tai khoan bi khoa', value: data.users.filter((user) => user.status === 'BLOCKED').length },
  ];

  return (
    <section>
      <h1 className="text-3xl font-black">Tong quan quan tri</h1>
      <p className="mt-2 text-zinc-500">Chi so co ban cho MVP, khong trien khai dashboard nang cao.</p>
      <div className="mt-6 grid grid-cols-4 gap-4">
        {cards.map((card) => (
          <div key={card.label} className="rounded-2xl border border-zinc-200 bg-white p-5">
            <p className="text-sm text-zinc-500">{card.label}</p>
            <p className="mt-3 text-3xl font-black">{card.value}</p>
          </div>
        ))}
      </div>
      <div className="mt-6 rounded-2xl border border-zinc-200 bg-white p-5">
        <h2 className="font-black">Trang thai pham vi</h2>
        <div className="mt-3 flex gap-2">
          <Badge tone="success">MVP_CURRENT</Badge>
          <Badge tone="warning">Mock data</Badge>
        </div>
      </div>
    </section>
  );
}
