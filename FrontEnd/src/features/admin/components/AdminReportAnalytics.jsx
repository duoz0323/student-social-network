import { TrendingUp } from 'lucide-react';
import { useAdminReportStatistics } from '../hooks/useAdminReportStatistics.js';
import { formatWeekRange } from '../utils/adminReportStatistics.js';

const STATUS_SEGMENTS = [
  { key: 'OPEN', label: 'Chờ xử lý', color: '#a84b00' },
  { key: 'RESOLVED_ACTION_TAKEN', label: 'Đã xử lý vi phạm', color: '#0868c7' },
  { key: 'RESOLVED_NO_VIOLATION', label: 'Không vi phạm', color: '#d62828' },
];

export default function AdminReportAnalytics() {
  const { statusCounts, weeklyTrend, loading, error } = useAdminReportStatistics();
  const prefersReducedMotion = typeof window !== 'undefined'
    && window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;

  if (loading) return <AnalyticsLoading />;

  return (
    <aside className="space-y-5 2xl:sticky 2xl:top-8">
      {error ? <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700">{error}</p> : null}
      <StatusDonutChart counts={statusCounts} prefersReducedMotion={prefersReducedMotion} />
      <WeeklyReportChart days={weeklyTrend} prefersReducedMotion={prefersReducedMotion} />
    </aside>
  );
}

function StatusDonutChart({ counts, prefersReducedMotion }) {
  const segments = STATUS_SEGMENTS.map((segment) => ({ ...segment, count: counts[segment.key] || 0 }));
  const total = segments.reduce((sum, segment) => sum + segment.count, 0);
  const drawingState = segments.reduce((state, segment) => {
    const percentage = total ? (segment.count / total) * 100 : 0;
    return {
      cursorPercentage: state.cursorPercentage + percentage,
      animationCount: state.animationCount + (percentage > 0 ? 1 : 0),
      drawableSegments: [...state.drawableSegments, {
        ...segment,
        percentage,
        rotation: state.cursorPercentage * 3.6 - 90,
        animationOrder: state.animationCount,
      }],
    };
  }, { cursorPercentage: 0, animationCount: 0, drawableSegments: [] });
  const { drawableSegments, animationCount } = drawingState;

  return (
    <section className="rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
      <ChartAnimationStyles />
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold text-zinc-500">Phân loại</p>
          <h2 className="mt-1 text-sm font-bold text-zinc-950">Trạng thái</h2>
        </div>
        <span className="rounded-md bg-zinc-50 px-2 py-1 text-[10px] font-semibold uppercase text-zinc-500">Toàn bộ</span>
      </div>

      <div className="relative mx-auto my-5 h-32 w-32" role="img" aria-label={`Tổng ${total} báo cáo`}>
        <svg className="h-full w-full" viewBox="0 0 120 120" aria-hidden="true">
          <circle cx="60" cy="60" r="46" fill="none" stroke="#e5e7eb" strokeWidth="18" />
          {drawableSegments.map((segment) => (
            <circle
              key={segment.key}
              cx="60"
              cy="60"
              r="46"
              pathLength="100"
              fill="none"
              stroke={segment.color}
              strokeWidth="18"
              strokeDasharray={`${segment.percentage} ${100 - segment.percentage}`}
              transform={`rotate(${segment.rotation} 60 60)`}
            >
              {!prefersReducedMotion && segment.percentage > 0 ? (
                <animate
                  attributeName="stroke-dasharray"
                  from="0 100"
                  to={`${segment.percentage} ${100 - segment.percentage}`}
                  begin={`${segment.animationOrder * 0.52}s`}
                  dur="0.58s"
                  calcMode="spline"
                  keyTimes="0;1"
                  keySplines="0.22 1 0.36 1"
                  fill="freeze"
                />
              ) : null}
            </circle>
          ))}
        </svg>
        <div className="pointer-events-none absolute inset-0 flex items-center justify-center">
          <strong
            className={prefersReducedMotion ? 'text-lg text-zinc-950' : 'admin-report-total-pop text-lg text-zinc-950'}
            style={{ '--total-delay': `${animationCount * 0.52}s` }}
          >
            {total}
          </strong>
        </div>
      </div>

      <ul className="space-y-2">
        {segments.map((segment) => (
          <li key={segment.key} className="flex items-center gap-2 text-xs text-zinc-600">
            <span className="h-2 w-2 shrink-0 rounded-full" style={{ backgroundColor: segment.color }} />
            <span className="min-w-0 flex-1">{segment.label}</span>
            <strong className="text-zinc-950">{segment.count}</strong>
          </li>
        ))}
      </ul>
    </section>
  );
}

function WeeklyReportChart({ days, prefersReducedMotion }) {
  const maximum = Math.max(...days.map((day) => day.count), 1);

  return (
    <section className="rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold text-zinc-500">Xu hướng báo cáo</p>
          <h2 className="mt-1 text-sm font-bold text-zinc-950">Tuần hiện tại</h2>
        </div>
        <TrendingUp className="h-4 w-4 text-blue-600" aria-hidden="true" />
      </div>
      <p className="mt-1 text-[10px] text-zinc-400">{formatWeekRange(days)}</p>

      <div className="mt-5 flex h-36 items-end gap-2 border-b border-zinc-100 pb-2" role="img" aria-label="Số báo cáo theo từng ngày trong tuần hiện tại">
        {days.map((day, index) => {
          const height = day.count ? Math.max((day.count / maximum) * 100, 12) : 5;
          return (
            <div key={day.dateKey} className="flex h-full min-w-0 flex-1 flex-col justify-end gap-2 text-center" title={`${day.label}: ${day.count} báo cáo`}>
              <span
                className={`text-[10px] font-semibold text-zinc-500 ${prefersReducedMotion ? '' : 'admin-report-value-rise'}`}
                style={{ '--value-delay': `${1.15 + index * 0.08}s` }}
              >
                {day.count}
              </span>
              <span
                className={`mx-auto w-full max-w-6 rounded-t-sm ${day.isCurrentDay ? 'bg-blue-600' : 'bg-zinc-100'} ${prefersReducedMotion ? '' : 'admin-report-bar-rise'}`}
                style={prefersReducedMotion
                  ? { height: `${height}%` }
                  : { '--bar-height': `${height}%`, '--bar-delay': `${index * 0.11}s` }}
              />
              <span className={`text-[9px] font-semibold ${day.isCurrentDay ? 'text-blue-600 underline' : 'text-zinc-500'}`}>{day.label}</span>
            </div>
          );
        })}
      </div>

      <p className="mt-4 text-xs leading-5 text-zinc-500">
        Số báo cáo được gửi theo từng ngày, tính từ Thứ 2 đến Chủ nhật của tuần hiện tại.
      </p>
    </section>
  );
}

function ChartAnimationStyles() {
  return (
    <style>{`
      @keyframes admin-report-total-pop {
        from { opacity: 0; transform: scale(0.72); }
        to { opacity: 1; transform: scale(1); }
      }
      @keyframes admin-report-bar-rise {
        from { height: 0; opacity: 0.25; transform: translateY(5px); }
        to { height: var(--bar-height); opacity: 1; transform: translateY(0); }
      }
      @keyframes admin-report-value-rise {
        from { opacity: 0; transform: translateY(8px); }
        to { opacity: 1; transform: translateY(0); }
      }
      .admin-report-total-pop {
        opacity: 0;
        animation: admin-report-total-pop 0.38s cubic-bezier(0.22, 1, 0.36, 1) var(--total-delay) forwards;
      }
      .admin-report-bar-rise {
        height: 0;
        animation: admin-report-bar-rise 0.48s cubic-bezier(0.22, 1, 0.36, 1) var(--bar-delay) both;
      }
      .admin-report-value-rise {
        opacity: 0;
        animation: admin-report-value-rise 0.32s ease-out var(--value-delay) forwards;
      }
    `}</style>
  );
}

function AnalyticsLoading() {
  return (
    <aside className="space-y-5" aria-label="Đang tải thống kê báo cáo">
      {[0, 1].map((item) => <div key={item} className="h-64 animate-pulse rounded-2xl border border-zinc-200 bg-zinc-50" />)}
    </aside>
  );
}
