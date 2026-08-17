import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { AlertTriangle, ArrowRight, Bookmark, Eye, EyeOff, FilePlus2, FileText, Heart, MessageCircle, RefreshCw, Repeat2, Trash2 } from 'lucide-react';
import { usePostAnalytics } from '../hooks/usePostAnalytics.js';

const PRESETS = [['7D', '7 ngày'], ['30D', '30 ngày'], ['90D', '90 ngày'], ['6M', '6 tháng'], ['1Y', '1 năm']];
const number = new Intl.NumberFormat('vi-VN');

/** Dashboard bài viết độc lập: chỉ hiển thị analytics, không trộn nghiệp vụ CRUD hoặc hashtag. */
export default function AdminPostAnalyticsPage() {
  const [preset, setPreset] = useState('30D');
  const [custom, setCustom] = useState({ fromDate: '', toDate: '' });
  const filters = useMemo(() => custom.fromDate && custom.toDate ? custom : { range: preset }, [custom, preset]);
  const { data, loading, error, retry } = usePostAnalytics(filters);

  function selectPreset(value) { setPreset(value); setCustom({ fromDate: '', toDate: '' }); }
  return <section className="space-y-5 rounded-[28px] bg-zinc-50 p-4 sm:p-6">
    <header className="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
      <div><p className="text-xs font-bold uppercase tracking-[.18em] text-zinc-500">Phân tích nội dung</p><h1 className="mt-1 text-2xl font-bold text-zinc-950">Thống kê bài viết</h1><p className="mt-1 text-sm text-zinc-500">Theo dõi tăng trưởng nội dung, tương tác và hiệu quả kiểm duyệt.</p></div>
      <div className="flex flex-wrap items-end gap-2 rounded-2xl border border-zinc-200 bg-white p-2 shadow-sm">
        {PRESETS.map(([value, label]) => <button key={value} type="button" onClick={() => selectPreset(value)} className={`rounded-xl px-3 py-2 text-sm font-semibold ${!custom.fromDate && preset === value ? 'bg-zinc-950 text-white' : 'text-zinc-600 hover:bg-zinc-100'}`}>{label}</button>)}
        <DateInput label="Từ" value={custom.fromDate} max={custom.toDate} onChange={(fromDate) => setCustom((value) => ({ ...value, fromDate }))} />
        <DateInput label="Đến" value={custom.toDate} min={custom.fromDate} onChange={(toDate) => setCustom((value) => ({ ...value, toDate }))} />
      </div>
    </header>
    {loading && <LoadingDashboard />}
    {!loading && error && <ErrorDashboard message={error} retry={retry} />}
    {!loading && !error && data && <Dashboard data={data} />}
  </section>;
}

function DateInput({ label, value, min, max, onChange }) {
  return <label className="ml-1 text-xs font-semibold text-zinc-500">{label}<input type="date" value={value} min={min || undefined} max={max || undefined} onChange={(event) => onChange(event.target.value)} className="ml-1 rounded-lg border border-zinc-200 px-2 py-1.5 text-sm text-zinc-800" /></label>;
}

function Dashboard({ data }) {
  const k = data.kpis;
  const cards = [
    ['Tổng bài viết', k.totalPosts, FileText, 'bg-blue-50 text-blue-700'], ['Đang công khai', k.publishedPosts, Eye, 'bg-emerald-50 text-emerald-700'],
    ['Bài mới trong kỳ', k.newPosts, FilePlus2, 'bg-violet-50 text-violet-700', k.newPostsChangeRate], ['Đã ẩn', k.hiddenPosts, EyeOff, 'bg-amber-50 text-amber-700'],
    ['Đã xóa', k.deletedPosts, Trash2, 'bg-rose-50 text-rose-700'], ['Tổng tương tác', k.totalInteractions, Heart, 'bg-pink-50 text-pink-700', k.interactionsChangeRate],
  ];
  return <><p className="text-xs text-zinc-500">Dữ liệu từ {formatDate(data.fromDate)} đến {formatDate(data.toDate)} (UTC)</p>
    <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6">{cards.map(([label, value, Icon, theme, change]) => <KpiCard key={label} {...{ label, value, Icon, theme, change }} />)}</div>
    {k.totalPosts === 0 ? <EmptyInline text="Chưa có bài viết để thống kê." /> : <>
      <div className="grid gap-4 xl:grid-cols-[minmax(0,1.7fr)_minmax(300px,.8fr)]"><Panel title="Bài viết mới theo thời gian" subtitle="Số bài được tạo trong từng ngày"><LineChart points={data.trend} /></Panel><Panel title="Phân bố trạng thái" subtitle="Trạng thái hiện tại của toàn bộ bài viết"><StatusDonut values={data.statusDistribution} /></Panel></div>
      <div className="grid gap-4 xl:grid-cols-[minmax(320px,.8fr)_minmax(0,1.6fr)]"><Panel title="Tương tác trong kỳ" subtitle={`Trung bình ${formatDecimal(data.interactions.averagePerPost)} tương tác / bài mới`}><InteractionBars values={data.interactions} /></Panel><Panel title="Top 5 bài viết nổi bật" subtitle="Xếp theo tổng Like, Comment, Save và Repost"><TopPosts rows={data.topPosts} /></Panel></div>
    </>}
  </>;
}

function KpiCard({ label, value, Icon, theme, change }) { return <article className="rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm"><div className={`flex h-9 w-9 items-center justify-center rounded-xl ${theme}`}><Icon size={18} /></div><p className="mt-4 text-xs font-bold uppercase tracking-wide text-zinc-500">{label}</p><div className="mt-1 flex items-end justify-between gap-2"><strong className="text-2xl text-zinc-950">{number.format(value)}</strong>{change !== undefined && <Change value={change} />}</div></article>; }
function Change({ value }) { if (value === null) return <span className="text-xs font-semibold text-blue-600">Mới phát sinh</span>; const positive = Number(value) >= 0; return <span className={`text-xs font-semibold ${positive ? 'text-emerald-600' : 'text-rose-600'}`}>{positive ? '+' : ''}{value}% kỳ trước</span>; }
function Panel({ title, subtitle, children }) { return <article className="min-w-0 rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm sm:p-5"><h2 className="font-bold text-zinc-950">{title}</h2><p className="mt-1 text-xs text-zinc-500">{subtitle}</p><div className="mt-5">{children}</div></article>; }

function LineChart({ points }) {
  const [activeIndex, setActiveIndex] = useState(null);
  const width = 760, height = 260, left = 36, top = 15, bottom = 28;
  const max = Math.max(...points.map((point) => point.count), 1), x = (index) => left + index * (width - left - 12) / Math.max(points.length - 1, 1), y = (value) => top + (height - top - bottom) * (1 - value / max);
  const path = points.map((point, index) => `${index ? 'L' : 'M'}${x(index)},${y(point.count)}`).join(' '), labelEvery = Math.max(1, Math.ceil(points.length / 7));
  const activePoint = activeIndex === null ? null : points[activeIndex];
  return <div className="overflow-x-auto"><svg viewBox={`0 0 ${width} ${height}`} className="min-w-[620px]" role="img" aria-label="Biểu đồ số bài viết mới theo ngày">{[0, .25, .5, .75, 1].map((ratio) => <line key={ratio} x1={left} x2={width - 12} y1={y(max * ratio)} y2={y(max * ratio)} stroke="#e4e4e7" />)}<path d={`${path} L${x(points.length - 1)},${height - bottom} L${left},${height - bottom} Z`} fill="#dbeafe" opacity=".75" /><path d={path} fill="none" stroke="#2563eb" strokeWidth="3" strokeLinejoin="round" />{points.map((point, index) => <g key={point.date}><circle cx={x(index)} cy={y(point.count)} r="3" fill="#fff" stroke="#2563eb" strokeWidth="2" /><circle cx={x(index)} cy={y(point.count)} r="11" fill="transparent" tabIndex="0" aria-label={`${formatDate(point.date)}: ${number.format(point.count)} bài viết`} onPointerEnter={() => setActiveIndex(index)} onPointerLeave={() => setActiveIndex(null)} onFocus={() => setActiveIndex(index)} onBlur={() => setActiveIndex(null)} />{index % labelEvery === 0 && <text x={x(index)} y={height - 7} textAnchor="middle" fontSize="10" fill="#71717a">{formatShortDate(point.date)}</text>}</g>)}{activePoint && <ChartTooltip point={activePoint} pointX={x(activeIndex)} pointY={y(activePoint.count)} chartWidth={width} chartBottom={height - bottom} left={left} />}</svg></div>;
}

/** Hiển thị ngày và số bài ngay trong biểu đồ để tooltip ổn định giữa các trình duyệt. */
function ChartTooltip({ point, pointX, pointY, chartWidth, chartBottom, left }) {
  const tooltipWidth = 154, tooltipHeight = 48, right = chartWidth - 12;
  const tooltipX = Math.min(Math.max(pointX - tooltipWidth / 2, left), right - tooltipWidth);
  const tooltipY = pointY - tooltipHeight - 12 < 4 ? pointY + 12 : pointY - tooltipHeight - 12;
  return <g pointerEvents="none"><line x1={pointX} x2={pointX} y1={pointY} y2={chartBottom} stroke="#2563eb" strokeDasharray="3 3" opacity=".45" /><rect x={tooltipX} y={tooltipY} width={tooltipWidth} height={tooltipHeight} rx="8" fill="#18181b" /><text x={tooltipX + 10} y={tooltipY + 18} fontSize="11" fill="#d4d4d8">{formatDate(point.date)}</text><text x={tooltipX + 10} y={tooltipY + 37} fontSize="13" fontWeight="700" fill="#fff">{number.format(point.count)} bài viết</text></g>;
}

function StatusDonut({ values }) {
  const items = [['Công khai', values.published, '#10b981'], ['Đã ẩn', values.hidden, '#f59e0b'], ['Đã xóa', values.deleted, '#f43f5e']], total = items.reduce((sum, item) => sum + item[1], 0) || 1; let offset = 0;
  return <div className="flex flex-col items-center gap-5 sm:flex-row sm:justify-center"><svg viewBox="0 0 120 120" className="h-44 w-44 -rotate-90"><circle cx="60" cy="60" r="44" fill="none" stroke="#f4f4f5" strokeWidth="18" />{items.map(([label, value, color]) => { const length = value / total * 276.46, node = <circle key={label} cx="60" cy="60" r="44" fill="none" stroke={color} strokeWidth="18" strokeDasharray={`${length} ${276.46 - length}`} strokeDashoffset={-offset}><title>{label}: {number.format(value)}</title></circle>; offset += length; return node; })}<text x="60" y="64" textAnchor="middle" className="rotate-90 origin-center fill-zinc-950 text-[16px] font-bold">{number.format(total)}</text></svg><div className="space-y-3">{items.map(([label, value, color]) => <div key={label} className="flex min-w-40 items-center justify-between gap-4 text-sm"><span className="flex items-center gap-2 text-zinc-600"><i className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: color }} />{label}</span><strong>{number.format(value)}</strong></div>)}</div></div>;
}

function InteractionBars({ values }) { const items = [['Like', values.likes, Heart, 'bg-pink-500'], ['Comment', values.comments, MessageCircle, 'bg-blue-500'], ['Save', values.saves, Bookmark, 'bg-violet-500'], ['Repost', values.reposts, Repeat2, 'bg-emerald-500']], max = Math.max(...items.map((item) => item[1]), 1); return <div className="space-y-4">{items.map(([label, value, Icon, color]) => <div key={label}><div className="mb-1.5 flex justify-between text-sm"><span className="flex items-center gap-2 text-zinc-600"><Icon size={15} />{label}</span><strong>{number.format(value)}</strong></div><div className="h-2.5 rounded-full bg-zinc-100"><div className={`h-full rounded-full ${color}`} style={{ width: `${value / max * 100}%` }} /></div></div>)}</div>; }

function TopPosts({ rows }) { if (!rows.length) return <EmptyInline text="Chưa có bài viết mới trong khoảng đã chọn." />; return <div className="overflow-x-auto"><table className="w-full min-w-[760px] text-left text-sm"><thead className="text-xs uppercase text-zinc-500"><tr><th className="pb-3">Bài viết</th><th>Tác giả</th><th>Like</th><th>Comment</th><th>Save</th><th>Repost</th><th>Tổng</th><th /></tr></thead><tbody>{rows.map((row) => <tr key={row.postId} className="border-t border-zinc-100"><td className="py-3"><PostIdentity row={row} /></td><td><Author author={row.author} /></td><td>{number.format(row.likes)}</td><td>{number.format(row.comments)}</td><td>{number.format(row.saves)}</td><td>{number.format(row.reposts)}</td><td className="font-bold">{number.format(row.totalInteractions)}</td><td><Link to={`/admin/posts/${row.postId}`} aria-label={`Xem bài ${row.postId}`} className="text-zinc-700 transition hover:text-zinc-950"><ArrowRight size={18} /></Link></td></tr>)}</tbody></table></div>; }

function PostIdentity({ row }) { return <div className="flex min-w-0 max-w-[300px] items-center gap-3">{row.thumbnailUrl ? <img src={row.thumbnailUrl} alt="" className="h-11 w-11 shrink-0 rounded-lg object-cover" /> : <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-zinc-100 text-zinc-400"><FileText size={18} /></div>}<div className="min-w-0"><p className="truncate font-semibold text-zinc-900">{row.contentPreview || 'Bài viết chỉ có media'}</p><span className="text-xs text-zinc-400">#{row.postId}</span></div></div>; }
function Author({ author }) { return <div className="flex items-center gap-2">{author.avatarUrl ? <img src={author.avatarUrl} alt="" className="h-7 w-7 rounded-full object-cover" /> : <span className="flex h-7 w-7 items-center justify-center rounded-full bg-zinc-200 text-xs font-bold">{author.displayName?.[0] || '?'}</span>}<div className="leading-tight"><p className="max-w-32 truncate font-medium">{author.displayName}</p><span className="text-xs text-zinc-400">@{author.username}</span></div></div>; }
function EmptyInline({ text }) { return <div className="rounded-xl bg-zinc-50 p-7 text-center text-sm text-zinc-500">{text}</div>; }
function LoadingDashboard() { return <div className="grid animate-pulse gap-4 sm:grid-cols-2 xl:grid-cols-3">{Array.from({ length: 6 }, (_, index) => <div key={index} className="h-32 rounded-2xl bg-zinc-200" />)}</div>; }
function ErrorDashboard({ message, retry }) { return <div className="flex flex-col items-center rounded-2xl border border-rose-200 bg-rose-50 p-8 text-center"><AlertTriangle className="text-rose-600" /><p className="mt-2 text-sm text-rose-700">{message}</p><button type="button" onClick={retry} className="mt-4 flex items-center gap-2 rounded-xl bg-zinc-950 px-4 py-2 text-sm font-semibold text-white"><RefreshCw size={15} />Thử lại</button></div>; }
function formatDate(value) { return new Intl.DateTimeFormat('vi-VN').format(new Date(`${value}T00:00:00Z`)); }
function formatShortDate(value) { const [, month, day] = value.split('-'); return `${day}/${month}`; }
function formatDecimal(value) { return number.format(Number(value || 0)); }
