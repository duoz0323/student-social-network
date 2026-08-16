import { useEffect, useState } from 'react';
import { FileText, Heart, MessageCircle, Plus, Repeat2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useApp } from '../../../../contexts/AppContext.jsx';
import { EmptyState, LoadingState } from '../../../../components/common/StateBlock.jsx';
import { formatNumber, shortTime } from '../../../../utils/formatters.js';
import { useAuth } from '../../../auth/hooks/useAuth.js';
import { ADMIN_PERMISSIONS } from '../../constants/adminRbac.js';
import { collaboratorApi } from '../services/collaboratorApi.js';
import { buildInteractionSeries, buildTrendChart } from '../utils/collaboratorDashboard.js';

const RANGE_OPTIONS = Object.freeze([
  { value: 7, label: '7 ngày' },
  { value: 30, label: '30 ngày' },
  { value: 90, label: '3 tháng' },
]);

const METRIC_STYLES = Object.freeze([
  { key: 'totalPosts', label: 'Bài đã đăng', Icon: FileText, iconClass: 'bg-indigo-100 text-indigo-600', glowClass: 'bg-indigo-100' },
  { key: 'totalLikesReceived', label: 'Lượt thích nhận được', Icon: Heart, iconClass: 'bg-rose-100 text-rose-600', glowClass: 'bg-rose-100' },
  { key: 'totalCommentsReceived', label: 'Bình luận nhận được', Icon: MessageCircle, iconClass: 'bg-cyan-100 text-cyan-600', glowClass: 'bg-cyan-100' },
  { key: 'totalRepostsReceived', label: 'Lượt đăng lại', Icon: Repeat2, iconClass: 'bg-violet-100 text-violet-600', glowClass: 'bg-violet-100' },
]);

export default function CollaboratorDashboardPage() {
  const { currentUser } = useApp();
  const auth = useAuth();
  const [days, setDays] = useState(7);
  const [state, setState] = useState({ data: null, error: '' });

  useEffect(() => {
    const controller = new AbortController();
    collaboratorApi.getDashboard(days, controller.signal)
      .then((data) => setState({ data, error: '' }))
      .catch((error) => !controller.signal.aborted && setState({ data: null, error: error.message }));
    return () => controller.abort();
  }, [days]);

  if (!state.data && !state.error) return <LoadingState />;
  if (state.error) return <EmptyState title="Không thể tải Dashboard" description={state.error} />;

  const series = buildInteractionSeries(state.data.interactionTrend, days);
  return (
    <section className="space-y-6">
      <DashboardHeader displayName={currentUser?.displayName} canCreatePost={auth.hasPermission(ADMIN_PERMISSIONS.COLLABORATOR_POST_CREATE)} />
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {METRIC_STYLES.map((metric) => <MetricCard key={metric.key} {...metric} value={state.data[metric.key]} />)}
      </div>
      <div className="grid gap-5 xl:grid-cols-[minmax(0,2fr)_minmax(270px,1fr)]">
        <TrendPanel days={days} setDays={setDays} series={series} />
        <TopPosts posts={state.data.topPosts} />
      </div>
    </section>
  );
}

function DashboardHeader({ displayName, canCreatePost }) {
  return <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
    <div>
      <h1 className="text-2xl font-bold tracking-tight text-zinc-950 sm:text-3xl">Xin chào, {displayName || 'Cộng tác viên'} <span aria-hidden="true"></span></h1>
      
    </div>
    {canCreatePost ? <Link to="/admin/collaborator/posts?create=1" className="inline-flex h-11 items-center justify-center gap-2 self-start rounded-xl bg-indigo-600 px-5 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:self-auto">
      <Plus size={17} /> Tạo bài viết
    </Link> : null}
  </header>;
}

function MetricCard({ label, value, Icon, iconClass, glowClass }) {
  return <article className="relative overflow-hidden rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
    <div className={`pointer-events-none absolute -right-8 -top-8 h-24 w-24 rounded-full opacity-70 blur-2xl ${glowClass}`} />
    <div className="relative flex items-start justify-between gap-3">
      <div><p className="max-w-36 text-xs font-bold uppercase leading-5 tracking-wide text-zinc-500">{label}</p><p className="mt-3 text-3xl font-bold tracking-tight text-zinc-950">{Number(value || 0).toLocaleString('vi-VN')}</p></div>
      <span className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full ${iconClass}`}><Icon size={18} fill={label.includes('thích') ? 'currentColor' : 'none'} /></span>
    </div>
  </article>;
}

function TrendPanel({ days, setDays, series }) {
  return <article className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-6">
    <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div><h2 className="text-lg font-bold text-zinc-950">Hiệu quả nội dung</h2><p className="mt-1 text-xs text-zinc-500">Tổng lượt thích, bình luận và đăng lại theo ngày.</p></div>
      <div className="flex rounded-xl bg-zinc-100 p-1" aria-label="Khoảng thời gian thống kê">
        {RANGE_OPTIONS.map((option) => <button key={option.value} type="button" onClick={() => setDays(option.value)} className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition ${days === option.value ? 'bg-indigo-600 text-white shadow-sm' : 'text-zinc-600 hover:text-zinc-950'}`}>{option.label}</button>)}
      </div>
    </div>
    <InteractionTrendChart series={series} />
  </article>;
}

function InteractionTrendChart({ series }) {
  const chart = buildTrendChart(series);
  if (!series.some((point) => point.total > 0)) return <div className="flex h-64 items-center justify-center rounded-xl bg-zinc-50 text-sm text-zinc-500">Chưa có tương tác trong khoảng thời gian này.</div>;

  return <div className="overflow-hidden" aria-label="Biểu đồ tổng tương tác theo ngày">
    <svg viewBox={`0 0 ${chart.width} ${chart.height}`} className="h-auto min-h-64 w-full" role="img">
      <defs><linearGradient id="collaboratorTrendArea" x1="0" x2="0" y1="0" y2="1"><stop offset="0%" stopColor="#6366f1" stopOpacity="0.34" /><stop offset="100%" stopColor="#6366f1" stopOpacity="0.04" /></linearGradient></defs>
      {chart.gridLines.map((line) => <g key={line.value}><line x1={chart.left} x2={chart.width - chart.right} y1={line.y} y2={line.y} stroke="#e4e4e7" strokeWidth="1" /><text x={chart.left - 10} y={line.y + 4} textAnchor="end" className="fill-zinc-400 text-[10px]">{formatNumber(line.value)}</text></g>)}
      <path d={chart.areaPath} fill="url(#collaboratorTrendArea)" />
      <path d={chart.linePath} fill="none" stroke="#4f46e5" strokeWidth="3" strokeLinejoin="round" strokeLinecap="round" />
      {chart.points.map((point) => <g key={point.date}><circle cx={point.x} cy={point.y} r="4" fill="white" stroke="#4f46e5" strokeWidth="2.5"><title>{`${point.label}: ${point.total.toLocaleString('vi-VN')} tương tác`}</title></circle>{point.showLabel ? <text x={point.x} y={chart.height - 8} textAnchor="middle" className="fill-zinc-500 text-[10px]">{point.label}</text> : null}</g>)}
    </svg>
  </div>;
}

function TopPosts({ posts = [] }) {
  return <article className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-6">
    <div className="mb-5 flex items-center justify-between"><h2 className="text-lg font-bold text-zinc-950">Bài viết nổi bật</h2><Link to="/admin/collaborator/posts" className="text-xs font-semibold text-indigo-600 hover:text-indigo-700">Xem tất cả</Link></div>
    {posts.length ? <div className="space-y-5">{posts.slice(0, 3).map((post) => <TopPostItem key={post.postId} post={post} />)}</div> : <p className="py-16 text-center text-sm text-zinc-500">Chưa có bài viết nổi bật.</p>}
  </article>;
}

function TopPostItem({ post }) {
  return <Link to={`/admin/collaborator/posts/${post.postId}`} className="group grid grid-cols-[56px_minmax(0,1fr)] gap-3">
    {post.thumbnail ? <img src={post.thumbnail} alt="" className="h-14 w-14 rounded-xl object-cover" /> : <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-indigo-50 text-indigo-400"><FileText size={22} /></div>}
    <div className="min-w-0"><p className="truncate text-sm font-semibold text-zinc-900 group-hover:text-indigo-600">{post.contentPreview || 'Bài viết chỉ có media'}</p><p className="mt-1 truncate text-xs text-zinc-500">{post.hashtag ? `#${post.hashtag} · ` : ''}{shortTime(post.createdAt)}</p><div className="mt-2 flex gap-3 text-xs text-zinc-500"><span className="inline-flex items-center gap-1"><Heart size={12} className="text-rose-500" fill="currentColor" />{formatNumber(post.likeCount)}</span><span className="inline-flex items-center gap-1"><MessageCircle size={12} />{formatNumber(post.commentCount)}</span><span className="inline-flex items-center gap-1"><Repeat2 size={12} />{formatNumber(post.repostCount)}</span></div></div>
  </Link>;
}
