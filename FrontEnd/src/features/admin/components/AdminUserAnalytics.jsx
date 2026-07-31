import { TrendingUp } from 'lucide-react';
import { useAdminUserStatistics } from '../hooks/useAdminUserStatistics.js';
import { formatWeekRange } from '../utils/adminReportStatistics.js';

export default function AdminUserAnalytics({ refreshKey }) {
  const { activeUsers, blockedUsers, weeklyTrend, loading, error } = useAdminUserStatistics(refreshKey);
  const prefersReducedMotion = typeof window !== 'undefined'
    && window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;

  if (loading) return <AnalyticsLoading />;

  return (
    <aside className="space-y-5">
      {error ? <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700">{error}</p> : null}
      <AccountStatusChart active={activeUsers} blocked={blockedUsers} prefersReducedMotion={prefersReducedMotion} />
      <WeeklyUserChart days={weeklyTrend} prefersReducedMotion={prefersReducedMotion} />
    </aside>
  );
}

function AccountStatusChart({ active, blocked, prefersReducedMotion }) {
  const total = active + blocked;
  const activePercentage = total ? (active / total) * 100 : 0;
  const blockedPercentage = total ? (blocked / total) * 100 : 0;

  return (
    <section className="rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
      <ChartAnimationStyles />
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold text-zinc-500">Phân loại</p>
          <h2 className="mt-1 text-sm font-bold text-zinc-950">Trạng thái tài khoản</h2>
        </div>
        <span className="rounded-md bg-zinc-50 px-2 py-1 text-[10px] font-semibold uppercase text-zinc-500">Toàn bộ</span>
      </div>

      <div className="relative mx-auto my-5 h-32 w-32" role="img" aria-label={`Tổng ${total} tài khoản, ${active} đang hoạt động, ${blocked} đã khóa`}>
        <svg className="h-full w-full" viewBox="0 0 120 120" aria-hidden="true">
          <circle cx="60" cy="60" r="46" fill="none" stroke="#e5e7eb" strokeWidth="16" />
          {activePercentage > 0 ? (
            <StatusArc percentage={activePercentage} rotation={-90} color="#0868c7" animate={!prefersReducedMotion} />
          ) : null}
          {blockedPercentage > 0 ? (
            <StatusArc percentage={blockedPercentage} rotation={activePercentage * 3.6 - 90} color="#d62828" animate={!prefersReducedMotion} />
          ) : null}
        </svg>
        <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
          <strong className={prefersReducedMotion ? 'text-xl text-zinc-950' : 'admin-user-total-pop text-xl text-zinc-950'}>{total}</strong>
          <span className="text-[9px] font-semibold uppercase tracking-wide text-zinc-400">Tổng cộng</span>
        </div>
      </div>

      <ul className="space-y-2">
        <LegendItem color="#0868c7" label="Đang hoạt động" value={active} />
        <LegendItem color="#d62828" label="Đã khóa" value={blocked} />
      </ul>
    </section>
  );
}

function StatusArc({ percentage, rotation, color, animate }) {
  return (
    <circle
      cx="60"
      cy="60"
      r="46"
      pathLength="100"
      fill="none"
      stroke={color}
      strokeWidth="16"
      strokeDasharray={`${percentage} ${100 - percentage}`}
      transform={`rotate(${rotation} 60 60)`}
    >
      {animate ? <animate attributeName="stroke-dasharray" from="0 100" to={`${percentage} ${100 - percentage}`} dur="0.65s" fill="freeze" /> : null}
    </circle>
  );
}

function WeeklyUserChart({ days, prefersReducedMotion }) {
  const maximum = Math.max(...days.map((day) => day.count), 1);

  return (
    <section className="rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold text-zinc-500">Người dùng mới</p>
          <h2 className="mt-1 text-sm font-bold text-zinc-950">Tuần hiện tại</h2>
        </div>
        <TrendingUp className="h-4 w-4 text-blue-600" aria-hidden="true" />
      </div>
      <p className="mt-1 text-[10px] text-zinc-400">{formatWeekRange(days)}</p>

      <div className="mt-5 flex h-36 items-end gap-2 border-b border-zinc-100 pb-2" role="img" aria-label="Số tài khoản mới theo từng ngày trong tuần hiện tại">
        {days.map((day, index) => {
          const height = day.count ? Math.max((day.count / maximum) * 100, 12) : 5;
          return (
            <div key={day.dateKey} className="flex h-full min-w-0 flex-1 flex-col justify-end gap-2 text-center" title={`${day.label}: ${day.count} người dùng mới`}>
              <span className={`text-[10px] font-semibold text-zinc-500 ${prefersReducedMotion ? '' : 'admin-user-value-rise'}`} style={{ '--value-delay': `${0.55 + index * 0.08}s` }}>
                {day.count}
              </span>
              <span
                className={`mx-auto w-full max-w-6 rounded-t-sm ${day.isCurrentDay ? 'bg-blue-600' : 'bg-blue-200'} ${prefersReducedMotion ? '' : 'admin-user-bar-rise'}`}
                style={prefersReducedMotion ? { height: `${height}%` } : { '--bar-height': `${height}%`, '--bar-delay': `${index * 0.08}s` }}
              />
              <span className={`text-[9px] font-semibold ${day.isCurrentDay ? 'text-blue-600 underline' : 'text-zinc-500'}`}>{day.label}</span>
            </div>
          );
        })}
      </div>
      <p className="mt-4 text-xs leading-5 text-zinc-500">
        Số tài khoản mới được tạo theo từng ngày, tính từ Thứ 2 đến Chủ nhật của tuần hiện tại.
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
      @keyframes admin-user-total-pop {
        from { opacity: 0; transform: scale(0.72); }
        to { opacity: 1; transform: scale(1); }
      }
      @keyframes admin-user-bar-rise {
        from { height: 0; opacity: 0.25; transform: translateY(5px); }
        to { height: var(--bar-height); opacity: 1; transform: translateY(0); }
      }
      @keyframes admin-user-value-rise {
        from { opacity: 0; transform: translateY(8px); }
        to { opacity: 1; transform: translateY(0); }
      }
      .admin-user-total-pop {
        animation: admin-user-total-pop 0.38s cubic-bezier(0.22, 1, 0.36, 1) both;
      }
      .admin-user-bar-rise {
        height: 0;
        animation: admin-user-bar-rise 0.48s cubic-bezier(0.22, 1, 0.36, 1) var(--bar-delay) both;
      }
      .admin-user-value-rise {
        opacity: 0;
        animation: admin-user-value-rise 0.32s ease-out var(--value-delay) forwards;
      }
    `}</style>
  );
}

function AnalyticsLoading() {
  return (
    <aside className="space-y-5" aria-label="Đang tải thống kê người dùng">
      {[0, 1].map((item) => <div key={item} className="h-64 animate-pulse rounded-2xl border border-zinc-200 bg-zinc-50" />)}
    </aside>
  );
}
