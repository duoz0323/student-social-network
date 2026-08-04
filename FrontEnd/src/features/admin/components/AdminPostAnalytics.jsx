import { TrendingUp } from 'lucide-react';
import { useAdminPostStatistics } from '../hooks/useAdminPostStatistics.js';
import { formatWeekRange } from '../utils/adminReportStatistics.js';

export default function AdminPostAnalytics() {
  const { totalPosts, hiddenPosts, weeklyTrend, loading, error } = useAdminPostStatistics();
  const prefersReducedMotion = typeof window !== 'undefined'
    && window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;

  if (loading) return <AnalyticsLoading />;

  return (
    <aside className="space-y-5">
      {error ? <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700">{error}</p> : null}
      <PostStatusChart total={totalPosts} hidden={hiddenPosts} prefersReducedMotion={prefersReducedMotion} />
      <WeeklyPostChart days={weeklyTrend} prefersReducedMotion={prefersReducedMotion} />
    </aside>
  );
}

function PostStatusChart({ total, hidden, prefersReducedMotion }) {
  const hiddenPercentage = total ? Math.min((hidden / total) * 100, 100) : 0;

  return (
    <section className="rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
      <ChartAnimationStyles />
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold text-zinc-500">Phân loại</p>
          <h2 className="mt-1 text-sm font-bold text-zinc-950">Trạng thái bài viết</h2>
        </div>
        <span className="rounded-md bg-zinc-50 px-2 py-1 text-[10px] font-semibold uppercase text-zinc-500">Toàn bộ</span>
      </div>

      <div className="relative mx-auto my-5 h-32 w-32" role="img" aria-label={`Tổng ${total} bài viết, ${hidden} bài đã ẩn`}>
        <svg className="h-full w-full" viewBox="0 0 120 120" aria-hidden="true">
          <circle cx="60" cy="60" r="46" fill="none" stroke="#e5e7eb" strokeWidth="16" />
          {total > 0 ? <circle cx="60" cy="60" r="46" fill="none" stroke="#0868c7" strokeWidth="16" /> : null}
          {hiddenPercentage > 0 ? (
            <circle
              cx="60"
              cy="60"
              r="46"
              pathLength="100"
              fill="none"
              stroke="#a84b00"
              strokeWidth="16"
              strokeDasharray={`${hiddenPercentage} ${100 - hiddenPercentage}`}
              transform="rotate(-90 60 60)"
            >
              {!prefersReducedMotion ? (
                <animate attributeName="stroke-dasharray" from="0 100" to={`${hiddenPercentage} ${100 - hiddenPercentage}`} dur="0.65s" fill="freeze" />
              ) : null}
            </circle>
          ) : null}
        </svg>
        <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
          <strong className={prefersReducedMotion ? 'text-xl text-zinc-950' : 'admin-post-total-pop text-xl text-zinc-950'}>{total}</strong>
          <span className="text-[9px] font-semibold uppercase tracking-wide text-zinc-400">Tổng cộng</span>
        </div>
      </div>

      <ul className="space-y-2">
        <LegendItem color="#0868c7" label="Tổng bài đăng" value={total} />
        <LegendItem color="#a84b00" label="Đã ẩn" value={hidden} />
      </ul>
    </section>
  );
}

function WeeklyPostChart({ days, prefersReducedMotion }) {
  const maximum = Math.max(...days.map((day) => day.count), 1);

  return (
    <section className="rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold text-zinc-500">Xu hướng đăng bài</p>
          <h2 className="mt-1 text-sm font-bold text-zinc-950">Tuần hiện tại</h2>
        </div>
        <TrendingUp className="h-4 w-4 text-blue-600" aria-hidden="true" />
      </div>
      <p className="mt-1 text-[10px] text-zinc-400">{formatWeekRange(days)}</p>

      <div className="mt-5 flex h-36 items-end gap-2 border-b border-zinc-100 pb-2" role="img" aria-label="Số bài viết được tạo theo từng ngày trong tuần hiện tại">
        {days.map((day, index) => {
          const height = day.count ? Math.max((day.count / maximum) * 100, 12) : 5;
          return (
            <div key={day.dateKey} className="flex h-full min-w-0 flex-1 flex-col justify-end gap-2 text-center" title={`${day.label}: ${day.count} bài viết`}>
              <span className={`text-[10px] font-semibold text-zinc-500 ${prefersReducedMotion ? '' : 'admin-post-value-rise'}`} style={{ '--value-delay': `${0.55 + index * 0.08}s` }}>
                {day.count}
              </span>
              <span
                className={`mx-auto w-full max-w-6 rounded-t-sm ${day.isCurrentDay ? 'bg-blue-600' : 'bg-blue-200'} ${prefersReducedMotion ? '' : 'admin-post-bar-rise'}`}
                style={prefersReducedMotion ? { height: `${height}%` } : { '--bar-height': `${height}%`, '--bar-delay': `${index * 0.08}s` }}
              />
              <span className={`text-[9px] font-semibold ${day.isCurrentDay ? 'text-blue-600 underline' : 'text-zinc-500'}`}>{day.label}</span>
            </div>
          );
        })}
      </div>
      <p className="mt-4 text-xs leading-5 text-zinc-500">
        Số bài viết được tạo theo từng ngày, tính từ Thứ 2 đến Chủ nhật của tuần hiện tại.
      </p>
    </section>
  );
}

function LegendItem({ color, label, value }) {
  return (
    <li className="flex items-center gap-2 text-xs text-zinc-600">
      <span className="h-2 w-2 shrink-0 rounded-full" style={{ backgroundColor: color }} />
      <span className="min-w-0 flex-1">{label}</span>
      <strong className="text-zinc-950">{value}</strong>
    </li>
  );
}

function ChartAnimationStyles() {
  return (
    <style>{`
      @keyframes admin-post-total-pop {
        from { opacity: 0; transform: scale(0.72); }
        to { opacity: 1; transform: scale(1); }
      }
      @keyframes admin-post-bar-rise {
        from { height: 0; opacity: 0.25; transform: translateY(5px); }
        to { height: var(--bar-height); opacity: 1; transform: translateY(0); }
      }
      @keyframes admin-post-value-rise {
        from { opacity: 0; transform: translateY(8px); }
        to { opacity: 1; transform: translateY(0); }
      }
      .admin-post-total-pop {
        animation: admin-post-total-pop 0.38s cubic-bezier(0.22, 1, 0.36, 1) both;
      }
      .admin-post-bar-rise {
        height: 0;
        animation: admin-post-bar-rise 0.48s cubic-bezier(0.22, 1, 0.36, 1) var(--bar-delay) both;
      }
      .admin-post-value-rise {
        opacity: 0;
        animation: admin-post-value-rise 0.32s ease-out var(--value-delay) forwards;
      }
    `}</style>
  );
}

function AnalyticsLoading() {
  return (
    <aside className="space-y-5" aria-label="Đang tải thống kê bài viết">
      {[0, 1].map((item) => <div key={item} className="h-64 animate-pulse rounded-2xl border border-zinc-200 bg-zinc-50" />)}
    </aside>
  );
}
