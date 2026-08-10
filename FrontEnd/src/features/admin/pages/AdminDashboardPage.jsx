import { useEffect, useMemo, useState } from 'react';
import { Activity, AlertTriangle, Ban, FileText, Users } from 'lucide-react';
import { adminApi } from '../../../api/index.js';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import {
  formatDashboardCount,
  formatDashboardDate,
  normalizeDashboardUserEngagement,
} from '../utils/dashboardUserEngagement.js';

const DASHBOARD_DAYS = 30;
const CHART = Object.freeze({ width: 960, height: 380, left: 54, right: 28, top: 28, bottom: 56 });

/**
 * Trang tổng quan Admin chỉ tải aggregate; không hiển thị lịch sử hoạt động chi tiết của từng người dùng.
 */
export default function AdminDashboardPage() {
  const [dashboard, setDashboard] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    Promise.all([
      adminApi.getUsers({ page: 0, size: 1 }, controller.signal),
      adminApi.getUsers({ status: 'BLOCKED', page: 0, size: 1 }, controller.signal),
      adminApi.getPosts({ page: 0, size: 1 }, controller.signal),
      adminApi.getModerationCases({ status: 'OPEN', page: 0, size: 1 }, controller.signal),
      adminApi.getUserEngagementDashboard({ days: DASHBOARD_DAYS }, controller.signal),
    ])
      .then(([users, blocked, posts, reports, engagement]) => setDashboard({
        cards: [
          { label: 'Người dùng', value: users.totalElements, icon: Users, theme: 'border-sky-100 from-sky-50 to-white text-sky-700', iconTheme: 'bg-sky-100' },
          { label: 'Bài viết', value: posts.totalElements, icon: FileText, theme: 'border-violet-100 from-violet-50 to-white text-violet-700', iconTheme: 'bg-violet-100' },
          { label: 'Báo cáo đang chờ', value: reports.totalElements, icon: AlertTriangle, theme: 'border-amber-100 from-amber-50 to-white text-amber-700', iconTheme: 'bg-amber-100' },
          { label: 'Tài khoản khóa', value: blocked.totalElements, icon: Ban, theme: 'border-rose-100 from-rose-50 to-white text-rose-700', iconTheme: 'bg-rose-100' },
        ],
        engagement: normalizeDashboardUserEngagement(engagement),
      }))
      .catch((requestError) => {
        if (!controller.signal.aborted && requestError.code !== 'ERR_CANCELED') {
          setError(requestError.message);
        }
      });
    return () => controller.abort();
  }, []);

  if (!dashboard && !error) return <LoadingState />;
  if (error) return <EmptyState title="Không thể tải Dashboard" description={error} />;

  return <section className="space-y-7">
    <div>
      <h1 className="text-5xl font-bold">Bảng điều khiển</h1>
      <p className="mt-3 text-base text-zinc-500">Tổng quan và mức tương tác trong 30 ngày gần nhất.</p>
    </div>

    <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
      {dashboard.cards.map(({ label, value, icon: Icon, theme, iconTheme }) => (
        <div key={label} className={`min-h-40 rounded-2xl border bg-gradient-to-br p-7 shadow-sm ${theme}`}>
          <div className={`flex h-11 w-11 items-center justify-center rounded-xl ${iconTheme}`}><Icon size={24} /></div>
          <p className="mt-5 text-base text-gray-500">{label}</p>
          <p className="text-4xl font-bold text-zinc-950">{formatDashboardCount(value)}</p>
        </div>
      ))}
    </div>
    <DashboardEngagement engagement={dashboard.engagement} />
  </section>;
}

function DashboardEngagement({ engagement }) {
  return <div className="grid gap-6 xl:grid-cols-[minmax(0,1.65fr)_minmax(350px,0.9fr)]">
    <DailyInteractionChart items={engagement.dailyInteractions} fromDate={engagement.fromDate} toDate={engagement.toDate} />
    <FeaturedUsers users={engagement.featuredUsers} date={engagement.toDate} />
  </div>;
}

function DailyInteractionChart({ items, fromDate, toDate }) {
  const points = useMemo(() => createChartPoints(items), [items]);
  const path = points.map((point, index) => `${index ? 'L' : 'M'} ${point.x} ${point.y}`).join(' ');
  const maximum = Math.max(...items.map((item) => item.interactionCount), 1);
  const baseline = CHART.height - CHART.bottom;

  return <section className="rounded-2xl border border-indigo-100 bg-gradient-to-br from-indigo-50/70 via-white to-sky-50/60 p-6 shadow-sm">
    <div className="flex items-start justify-between gap-4">
      <div>
        <h2 className="text-xl font-bold">Xu hướng tương tác</h2>
        <p className="mt-2 text-sm text-zinc-500">Tổng request nghiệp vụ hợp lệ của USER theo ngày UTC.</p>
      </div>
      <span className="flex items-center gap-2 rounded-full bg-indigo-100 px-3 py-1.5 text-sm font-semibold text-indigo-700"><Activity size={17} /> Tương tác</span>
    </div>
    <div className="mt-6 overflow-x-auto">
      <svg viewBox={`0 0 ${CHART.width} ${CHART.height}`} className="min-w-[700px] w-full" role="img" aria-label={`Biểu đồ tổng tương tác từ ${fromDate} đến ${toDate}`}>
        <defs>
          <linearGradient id="dashboard-interaction-area" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#6366f1" stopOpacity="0.26" />
            <stop offset="100%" stopColor="#38bdf8" stopOpacity="0.02" />
          </linearGradient>
        </defs>
        {[0, 0.25, 0.5, 0.75, 1].map((ratio) => {
          const y = CHART.top + ratio * (baseline - CHART.top);
          return <g key={ratio}>
            <line x1={CHART.left} x2={CHART.width - CHART.right} y1={y} y2={y} stroke="#e4e4e7" strokeDasharray="3 4" />
            <text x={CHART.left - 10} y={y + 4} textAnchor="end" className="fill-zinc-400 text-[12px]">{formatDashboardCount(Math.round(maximum * (1 - ratio)))}</text>
          </g>;
        })}
        {path ? <path d={`${path} L ${points.at(-1).x} ${baseline} L ${points[0].x} ${baseline} Z`} fill="url(#dashboard-interaction-area)" /> : null}
        {path ? <path d={path} fill="none" stroke="#4f46e5" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" /> : null}
        {points.map((point, index) => <g key={items[index].date}>
          <circle cx={point.x} cy={point.y} r="4" fill="#6366f1">
            <title>{`${formatDashboardDate(items[index].date)}: ${formatDashboardCount(items[index].interactionCount)} tương tác`}</title>
          </circle>
          {showAxisLabel(index, items.length) ? <text x={point.x} y={CHART.height - 18} textAnchor="middle" className="fill-zinc-500 text-[12px]">{formatDashboardDate(items[index].date)}</text> : null}
        </g>)}
      </svg>
    </div>
  </section>;
}

function FeaturedUsers({ users, date }) {
  return <section className="min-h-[440px] rounded-2xl border border-violet-100 bg-white shadow-sm">
    <div className="border-b border-violet-100 bg-violet-50/70 px-6 py-5">
      <h2 className="text-xl font-bold text-violet-950">Người dùng nổi bật</h2>
      <p className="mt-2 text-sm text-zinc-500">Xếp hạng ngày {formatDashboardDate(date, { day: '2-digit', month: '2-digit', year: 'numeric' })} (UTC).</p>
    </div>
    {users.length ? <div className="divide-y">
      {users.map((user, index) => <article key={user.userId} className="flex items-center gap-4 px-6 py-4 transition-colors hover:bg-violet-50/50">
        <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-violet-100 text-xs font-bold text-violet-700">{index + 1}</span>
        <Avatar user={user} />
        <div className="min-w-0 flex-1">
          <p className="truncate text-base font-semibold">{user.displayName}</p>
          <p className="mt-1 text-sm text-zinc-500">{formatDashboardCount(user.postCount)} bài viết · {formatDashboardCount(user.interactionCount)} tương tác</p>
        </div>
      </article>)}
    </div> : <div className="px-6 py-16 text-center text-base text-zinc-500">Chưa có dữ liệu tương tác hôm nay.</div>}
  </section>;
}

function Avatar({ user }) {
  if (user.avatarUrl) return <img src={user.avatarUrl} alt="" className="h-11 w-11 rounded-full border-2 border-sky-200 object-cover" />;
  return <div className="flex h-11 w-11 items-center justify-center rounded-full bg-sky-100 text-base font-bold text-sky-700">{user.displayName.charAt(0).toUpperCase()}</div>;
}

function createChartPoints(items) {
  if (!items.length) return [];
  const baseline = CHART.height - CHART.bottom;
  const chartWidth = CHART.width - CHART.left - CHART.right;
  const maximum = Math.max(...items.map((item) => item.interactionCount), 1);
  return items.map((item, index) => ({
    x: CHART.left + (items.length === 1 ? chartWidth / 2 : (index / (items.length - 1)) * chartWidth),
    y: baseline - (item.interactionCount / maximum) * (baseline - CHART.top),
  }));
}

function showAxisLabel(index, length) {
  return index === 0 || index === length - 1 || index === Math.round((length - 1) / 2) || index % 7 === 0;
}
