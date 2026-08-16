import { useCallback, useEffect, useState } from 'react';
import { ArrowDown, ArrowRight, ArrowUp, Hash, RefreshCw } from 'lucide-react';
import { Link } from 'react-router-dom';
import { adminApi } from '../../../api/index.js';
import Button from '../../../components/common/Button.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import { normalizeHashtagAnalytics } from '../utils/adminHashtagAnalytics.js';

const PRESETS = [['7D', '7 ngày'], ['30D', '30 ngày'], ['90D', '90 ngày'], ['6M', '6 tháng'], ['1Y', '1 năm']];
const number = new Intl.NumberFormat('vi-VN');

/** Trang Analytics chỉ đọc, tách biệt hoàn toàn khỏi nghiệp vụ CRUD hashtag. */
export default function AdminHashtagAnalyticsPage() {
  const [range, setRange] = useState('30D');
  const [revision, setRevision] = useState(0);
  const [state, setState] = useState({ data: null, loading: true, error: '' });
  const load = useCallback(async (signal) => {
    setState((current) => ({ ...current, loading: true, error: '' }));
    try {
      const response = await adminApi.getHashtagAnalytics({ range }, signal);
      if (!signal.aborted) setState({ data: normalizeHashtagAnalytics(response), loading: false, error: '' });
    } catch (requestError) {
      if (!signal.aborted) setState({ data: null, loading: false, error: requestError.message || 'Không thể tải thống kê hashtag.' });
    }
  }, [range]);
  useEffect(() => {
    const controller = new AbortController();
    const timer = window.setTimeout(() => load(controller.signal), 0);
    return () => { window.clearTimeout(timer); controller.abort(); };
  }, [load, revision]);

  return <section className="space-y-5 rounded-[28px] bg-zinc-50 p-4 sm:p-6">
    <header className="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
      <div className="flex gap-3"><span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-violet-100 text-violet-700"><Hash size={22} /></span><div><h1 className="text-2xl font-bold text-zinc-950">Thống kê Hashtag</h1><p className="mt-1 text-sm text-zinc-500">Theo dõi mức độ sử dụng và xu hướng hashtag trong các bài viết trên hệ thống.</p></div></div>
      <div className="flex flex-wrap items-center gap-2 rounded-2xl border border-zinc-200 bg-white p-2 shadow-sm">
        {PRESETS.map(([value, label]) => <button key={value} type="button" onClick={() => setRange(value)} className={`rounded-xl px-3 py-2 text-sm font-semibold ${range === value ? 'bg-zinc-950 text-white' : 'text-zinc-600 hover:bg-zinc-100'}`}>{label}</button>)}
        {state.data ? <span className="px-2 text-xs font-semibold text-zinc-500">{formatDate(state.data.fromDate)} - {formatDate(state.data.toDate)}</span> : null}
        <Button variant="secondary" size="sm" disabled={state.loading} onClick={() => setRevision((value) => value + 1)}><RefreshCw size={14} /><span className="sr-only">Làm mới</span></Button>
      </div>
    </header>
    {state.loading ? <LoadingState message="Đang tổng hợp thống kê hashtag..." /> : null}
    {!state.loading && state.error ? <ErrorState message={state.error} retry={() => setRevision((value) => value + 1)} /> : null}
    {!state.loading && !state.error && state.data ? <AnalyticsContent data={state.data} range={range} /> : null}
  </section>;
}

function AnalyticsContent({ data, range }) {
  const k = data.kpis;
  if (!k.totalHashtags) return <EmptyState title="Chưa có hashtag" description="Hệ thống chưa có dữ liệu hashtag để thống kê." />;
  const cards = [
    ['Tổng Hashtag', k.totalHashtags, 'Hiện có trong hệ thống', 'border-l-violet-500'],
    ['Hashtag được sử dụng', k.usedHashtags, 'Có ít nhất 1 bài sử dụng trong kỳ', 'border-l-blue-500'],
    ['Hashtag mới', k.newHashtags, `Được tạo trong ${presetDescription(range)}`, 'border-l-emerald-500', formatChange(k.newHashtagsChangeRate), k.newHashtagsChangeRate !== null && k.newHashtagsChangeRate < 0 ? 'text-rose-600' : 'text-emerald-600'],
    ['Bài có Hashtag', k.postsWithHashtag, 'Trong khoảng thời gian đã chọn', 'border-l-cyan-500'],
    ['Tỷ lệ bài có Hashtag', `${formatDecimal(k.usageRate)}%`, `${number.format(k.postsWithHashtag)} / ${number.format(k.totalPosts)} bài`, 'border-l-amber-500'],
    ['Trung bình bài / Hashtag', formatDecimal(k.averagePostsPerUsedHashtag), 'Trên các hashtag có sử dụng trong kỳ', 'border-l-pink-500'],
  ];
  return <div className="space-y-5">
    <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6">{cards.map(([label, value, helper, accent, change, changeTone]) => <Kpi key={label} {...{ label, value, helper, accent, change, changeTone }} />)}</div>
    <Panel title="Xu hướng sử dụng Hashtag" subtitle="Số bài viết có gắn hashtag theo thời gian."><TrendChart items={data.trend} granularity={data.granularity} /></Panel>
    <div className="grid gap-5 xl:grid-cols-[minmax(0,1.5fr)_minmax(300px,.8fr)]"><Panel title="Hashtag phổ biến" subtitle="Các hashtag xuất hiện trong nhiều bài viết nhất trong khoảng thời gian đã chọn."><PopularBars items={data.popularHashtags} /></Panel><Panel title="Phân bố Hashtag" subtitle="Mức độ tập trung sử dụng hashtag trong hệ thống."><DistributionDonut values={data.distribution} /></Panel></div>
    <Panel title="Hashtag tăng trưởng nhanh" subtitle="So sánh số bài sử dụng hashtag với kỳ trước có cùng độ dài."><GrowthTable rows={data.growthHashtags} /></Panel>
    <div className="grid gap-5 2xl:grid-cols-[minmax(0,1.7fr)_minmax(280px,.65fr)]"><Panel title="Hashtag hoạt động gần đây" subtitle="Các hashtag vừa được sử dụng trong bài viết mới nhất."><RecentTable rows={data.recentHashtags} /></Panel><Panel title="Hashtag ít được sử dụng" subtitle="Hashtag đang có rất ít bài viết liên kết."><LowUsageList items={data.lowUsageHashtags} /></Panel></div>
  </div>;
}

function Kpi({ label, value, helper, accent, change, changeTone }) { return <article className={`rounded-2xl border border-zinc-200 border-l-4 bg-white p-4 shadow-sm ${accent}`}><p className="text-[10px] font-bold uppercase tracking-[.08em] text-zinc-500">{label}</p><strong className="mt-3 block text-2xl text-zinc-950">{typeof value === 'number' ? number.format(value) : value}</strong><p className="mt-1 text-xs text-zinc-500">{helper}</p>{change ? <p className={`mt-2 text-xs font-semibold ${changeTone}`}>{change}</p> : null}</article>; }
function Panel({ title, subtitle, children }) { return <section className="min-w-0 rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm sm:p-5"><h2 className="text-base font-bold text-zinc-950">{title}</h2><p className="mt-1 text-xs text-zinc-500">{subtitle}</p><div className="mt-5">{children}</div></section>; }

function TrendChart({ items, granularity }) {
  const [active, setActive] = useState(null);
  const width = 900, height = 290, left = 42, right = 20, top = 18, bottom = 34;
  const max = Math.max(...items.flatMap((item) => [item.postsWithHashtag, item.totalPosts]), 1);
  const x = (index) => left + index * (width - left - right) / Math.max(items.length - 1, 1);
  const y = (value) => top + (height - top - bottom) * (1 - value / max);
  const path = (key) => items.map((item, index) => `${index ? 'L' : 'M'}${x(index)},${y(item[key])}`).join(' ');
  const labelEvery = Math.max(1, Math.ceil(items.length / 8));
  const activeItem = active === null ? null : items[active];
  if (!items.length) return <EmptyInline text="Chưa có dữ liệu xu hướng trong kỳ." />;
  return <div className="overflow-x-auto"><div className="mb-3 flex gap-5 text-xs font-semibold text-zinc-600"><Legend color="#7c3aed" label="Bài có Hashtag" /><Legend color="#94a3b8" label="Tổng bài viết" dashed /></div><svg viewBox={`0 0 ${width} ${height}`} className="min-w-[680px]" role="img" aria-label="Xu hướng số bài viết có hashtag">{[0, .25, .5, .75, 1].map((ratio) => <line key={ratio} x1={left} x2={width - right} y1={y(max * ratio)} y2={y(max * ratio)} stroke="#e4e4e7" />)}<path d={path('totalPosts')} fill="none" stroke="#94a3b8" strokeWidth="2.5" strokeDasharray="6 5" /><path d={path('postsWithHashtag')} fill="none" stroke="#7c3aed" strokeWidth="3" strokeLinejoin="round" />{items.map((item, index) => <g key={item.period}><circle cx={x(index)} cy={y(item.postsWithHashtag)} r="3.5" fill="#fff" stroke="#7c3aed" strokeWidth="2" /><circle cx={x(index)} cy={y(item.totalPosts)} r="3" fill="#fff" stroke="#94a3b8" strokeWidth="2" /><circle cx={x(index)} cy={y(item.postsWithHashtag)} r="12" fill="transparent" tabIndex="0" aria-label={`${formatPeriod(item.period, granularity)}: ${number.format(item.postsWithHashtag)} bài có hashtag`} onPointerEnter={() => setActive(index)} onPointerLeave={() => setActive(null)} onFocus={() => setActive(index)} onBlur={() => setActive(null)} />{index % labelEvery === 0 ? <text x={x(index)} y={height - 8} textAnchor="middle" fontSize="10" fill="#71717a">{formatPeriodShort(item.period)}</text> : null}</g>)}{activeItem ? <TrendTooltip item={activeItem} pointX={x(active)} pointY={y(activeItem.postsWithHashtag)} chartWidth={width} chartBottom={height - bottom} granularity={granularity} /> : null}</svg></div>;
}

function TrendTooltip({ item, pointX, pointY, chartWidth, chartBottom, granularity }) { const width = 190, height = 67, x = Math.min(Math.max(pointX - width / 2, 42), chartWidth - 20 - width), y = pointY - height - 12 < 4 ? pointY + 12 : pointY - height - 12; return <g pointerEvents="none"><line x1={pointX} x2={pointX} y1={pointY} y2={chartBottom} stroke="#7c3aed" strokeDasharray="3 3" opacity=".45" /><rect x={x} y={y} width={width} height={height} rx="9" fill="#18181b" /><text x={x + 11} y={y + 19} fontSize="11" fill="#d4d4d8">{formatPeriod(item.period, granularity)}</text><text x={x + 11} y={y + 40} fontSize="12" fontWeight="700" fill="#fff">{number.format(item.postsWithHashtag)} bài có Hashtag</text><text x={x + 11} y={y + 57} fontSize="11" fill="#cbd5e1">Tổng: {number.format(item.totalPosts)} bài viết</text></g>; }

function PopularBars({ items }) { const max = Math.max(...items.map((item) => item.postCount), 1); if (!items.length) return <EmptyInline text="Chưa có hashtag được sử dụng trong kỳ." />; return <div className="space-y-3">{items.map((item, index) => <Link key={item.hashtagId} to={`/admin/hashtags?keyword=${encodeURIComponent(item.name)}`} className="group relative grid grid-cols-[1.5rem_minmax(0,1fr)] gap-2 rounded-lg p-1 hover:bg-zinc-50"><span className="text-xs font-bold text-zinc-400">{index + 1}</span><div><div className="mb-1 flex justify-between gap-3 text-xs"><span className="truncate font-semibold text-zinc-900">#{item.name}</span><span className="shrink-0 text-zinc-500">{number.format(item.postCount)} bài</span></div><div className="h-2 rounded-full bg-zinc-100"><div className="h-full rounded-full bg-violet-500" style={{ width: `${item.postCount / max * 100}%` }} /></div></div><span className="pointer-events-none absolute bottom-full right-2 z-10 hidden rounded-lg bg-zinc-900 px-3 py-2 text-xs text-white shadow-lg group-hover:block">#{item.name}: {number.format(item.postCount)} bài · {formatDecimal(item.share)}%</span></Link>)}</div>; }

function DistributionDonut({ values }) { const total = values.topTenPosts + values.otherPosts, topRate = total ? values.topTenPosts / total * 100 : 0, otherRate = total ? 100 - topRate : 0, circumference = 276.46; return <div className="flex flex-col items-center gap-5"><svg viewBox="0 0 120 120" className="h-44 w-44 -rotate-90"><circle cx="60" cy="60" r="44" fill="none" stroke="#e4e4e7" strokeWidth="18" /><circle cx="60" cy="60" r="44" fill="none" stroke="#7c3aed" strokeWidth="18" strokeDasharray={`${circumference * topRate / 100} ${circumference}`}><title>Top 10: {formatDecimal(topRate)}%</title></circle><text x="60" y="57" textAnchor="middle" className="rotate-90 origin-center fill-zinc-950 text-[14px] font-bold">{number.format(total)}</text><text x="60" y="72" textAnchor="middle" className="rotate-90 origin-center fill-zinc-500 text-[8px]">Bài có hashtag</text></svg><div className="w-full space-y-2 text-sm"><DistributionRow color="bg-violet-500" label="Top 10 Hashtag" value={`${formatDecimal(topRate)}%`} /><DistributionRow color="bg-zinc-300" label="Hashtag khác" value={`${formatDecimal(otherRate)}%`} /></div></div>; }
function DistributionRow({ color, label, value }) { return <div className="flex justify-between"><span className="flex items-center gap-2 text-zinc-600"><i className={`h-2.5 w-2.5 rounded-full ${color}`} />{label}</span><strong>{value}</strong></div>; }

function GrowthTable({ rows }) { if (!rows.length) return <EmptyInline text="Chưa có dữ liệu để so sánh với kỳ trước." />; return <div className="overflow-x-auto"><table className="w-full min-w-[680px] text-left text-sm"><thead className="text-xs uppercase text-zinc-500"><tr><th className="pb-3">Hashtag</th><th>Kỳ trước</th><th>Kỳ hiện tại</th><th>Thay đổi</th><th>Xu hướng</th></tr></thead><tbody>{rows.map((row) => { const positive = row.changeRate === null || row.changeRate >= 0; return <tr key={row.hashtagId} className="border-t border-zinc-100"><td className="py-3 font-semibold"><Link to={`/admin/hashtags?keyword=${encodeURIComponent(row.name)}`} className="text-violet-700 hover:underline">#{row.name}</Link></td><td>{number.format(row.previousCount)}</td><td>{number.format(row.currentCount)}</td><td className={positive ? 'text-emerald-600' : 'text-rose-600'}>{row.changeRate === null ? 'Mới phát sinh' : `${row.changeRate > 0 ? '+' : ''}${formatDecimal(row.changeRate)}%`}</td><td>{positive ? <ArrowUp size={17} className="text-emerald-600" /> : <ArrowDown size={17} className="text-rose-600" />}</td></tr>; })}</tbody></table></div>; }

function RecentTable({ rows }) { if (!rows.length) return <EmptyInline text="Chưa có hashtag nào được sử dụng." />; return <div className="overflow-x-auto"><table className="w-full min-w-[900px] text-left text-sm"><thead className="text-xs uppercase text-zinc-500"><tr><th className="pb-3">Hashtag</th><th>Đang liên kết</th><th>Sử dụng trong kỳ</th><th>Ngày tạo</th><th>Lần dùng gần nhất</th><th>Hoạt động</th><th /></tr></thead><tbody>{rows.map((row) => <tr key={row.hashtagId} className="border-t border-zinc-100"><td className="py-3 font-semibold text-violet-700">#{row.name}</td><td>{number.format(row.linkedPostCount)}</td><td>{number.format(row.periodPostCount)}</td><td>{formatDateTime(row.createdAt, false)}</td><td>{formatDateTime(row.latestUsedAt, true)}</td><td>{formatRelative(row.latestUsedAt)}</td><td><Link to={`/admin/hashtags?keyword=${encodeURIComponent(row.name)}`} className="inline-flex items-center gap-1 font-semibold text-blue-600">Xem chi tiết <ArrowRight size={14} /></Link></td></tr>)}</tbody></table></div>; }
function LowUsageList({ items }) { if (!items.length) return <EmptyInline text="Chưa có dữ liệu hashtag." />; return <div className="space-y-2">{items.map((item) => <Link key={item.hashtagId} to={`/admin/hashtags?keyword=${encodeURIComponent(item.name)}`} className="flex items-center justify-between rounded-xl bg-zinc-50 px-3 py-3 text-sm hover:bg-violet-50"><span className="truncate font-semibold text-zinc-800">#{item.name}</span><span className="shrink-0 text-zinc-500">{number.format(item.linkedPostCount)} bài</span></Link>)}<p className="pt-2 text-xs text-zinc-400">Việc chỉnh sửa hoặc xóa chỉ thực hiện tại màn Quản lý Hashtag.</p></div>; }

function Legend({ color, label, dashed = false }) { return <span className="inline-flex items-center gap-1.5"><i className={`w-5 border-t-2 ${dashed ? 'border-dashed' : ''}`} style={{ borderColor: color }} />{label}</span>; }
function ErrorState({ message, retry }) { return <div className="rounded-2xl border border-red-200 bg-red-50 p-5 text-sm text-red-700"><p className="font-semibold">Không thể tải thống kê hashtag</p><p className="mt-1">{message}</p><button type="button" onClick={retry} className="mt-3 rounded-lg bg-zinc-950 px-3 py-2 font-semibold text-white">Thử lại</button></div>; }
function EmptyInline({ text }) { return <div className="rounded-xl bg-zinc-50 p-7 text-center text-sm text-zinc-500">{text}</div>; }
function formatDate(value) { if (!value) return '—'; const [year, month, day] = value.split('-'); return `${day}/${month}/${year}`; }
function formatPeriod(value, granularity) { return granularity === 'MONTH' ? `Tháng ${value.slice(5, 7)}/${value.slice(0, 4)}` : formatDate(value); }
function formatPeriodShort(value) { return value.length === 7 ? `${value.slice(5, 7)}/${value.slice(2, 4)}` : `${value.slice(8, 10)}/${value.slice(5, 7)}`; }
function formatDecimal(value) { return new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 1 }).format(Number(value || 0)); }
function formatChange(value) { return value === null ? 'Mới phát sinh so với kỳ trước' : `${value >= 0 ? '+' : ''}${formatDecimal(value)}% so với kỳ trước`; }
function presetDescription(range) { return ({ '7D': '7 ngày qua', '30D': '30 ngày qua', '90D': '90 ngày qua', '6M': '6 tháng qua', '1Y': '1 năm qua' })[range]; }
function parseUtc(value) { if (!value) return null; return new Date(/[zZ]|[+-]\d\d:\d\d$/.test(value) ? value : `${value}Z`); }
function formatDateTime(value, includeTime) { const date = parseUtc(value); if (!date || Number.isNaN(date.getTime())) return '—'; return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', ...(includeTime ? { timeStyle: 'short' } : {}), timeZone: 'Asia/Ho_Chi_Minh' }).format(date); }
function formatRelative(value) { const date = parseUtc(value); if (!date || Number.isNaN(date.getTime())) return '—'; const minutes = Math.max(0, Math.floor((Date.now() - date.getTime()) / 60000)); if (minutes < 60) return `${minutes} phút trước`; const hours = Math.floor(minutes / 60); if (hours < 24) return `${hours} giờ trước`; return `${Math.floor(hours / 24)} ngày trước`; }
