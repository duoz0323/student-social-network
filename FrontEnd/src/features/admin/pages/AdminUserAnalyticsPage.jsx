import { useMemo, useState } from 'react';
import {
  CalendarDays,
  ChevronDown,
  RefreshCw,
  RotateCcw,
  SlidersHorizontal,
} from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import { useUserEngagementAnalytics } from '../hooks/useUserEngagementAnalytics.js';
import {
  createDefaultAnalyticsFilters,
  formatCount,
  formatMonth,
  formatRate,
  hasEligibleUsers,
  validateAnalyticsFilters,
} from '../utils/userEngagementAnalytics.js';

const CHART = Object.freeze({ width: 920, height: 420, left: 54, right: 58, top: 28, bottom: 46 });

/**
 * Trang Analytics độc lập của Admin, trình bày đúng các chỉ số mà API engagement công bố.
 */
export default function AdminUserAnalyticsPage() {
  const defaults = useMemo(() => createDefaultAnalyticsFilters(), []);
  const [draftFilters, setDraftFilters] = useState(defaults);
  const [appliedFilters, setAppliedFilters] = useState(defaults);
  const [formError, setFormError] = useState('');
  const { monthly, summary, loading, error, retry } = useUserEngagementAnalytics(appliedFilters);

  function updateFilter(field, value) {
    setDraftFilters((current) => ({ ...current, [field]: value }));
    setFormError('');
  }

  function submitFilters(event) {
    event.preventDefault();
    const validationError = validateAnalyticsFilters(draftFilters);
    if (validationError) {
      setFormError(validationError);
      return;
    }
    setAppliedFilters({ ...draftFilters, inactiveDays: Number(draftFilters.inactiveDays) });
  }

  function resetFilters() {
    const nextFilters = createDefaultAnalyticsFilters();
    setDraftFilters(nextFilters);
    setAppliedFilters(nextFilters);
    setFormError('');
  }

  return (
    <section className="space-y-4 transition-[width,transform] min-[1800px]:w-[calc(100%+28rem)] min-[1800px]:-translate-x-56">
      <div className="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-end">
        <AnalyticsFilterForm
          filters={draftFilters}
          currentMonth={defaults.toMonth}
          loading={loading}
          error={formError}
          onChange={updateFilter}
          onSubmit={submitFilters}
          onReset={resetFilters}
        />
      </div>

      {error ? <AnalyticsError message={error} onRetry={retry} /> : null}
      {loading ? <LoadingState message="Đang tải số liệu..." /> : null}

      {!loading && !error && monthly && summary ? (
        hasEligibleUsers(monthly.items) ? (
          <AnalyticsContent monthly={monthly} summary={summary} />
        ) : (
          <EmptyState
            title="Chưa có dữ liệu trong thời gian này"
            description="Hãy chọn khoảng thời gian khác rồi thử lại."
            actionLabel="Tải lại"
            onAction={retry}
          />
        )
      ) : null}
    </section>
  );
}

function AnalyticsFilterForm({ filters, currentMonth, loading, error, onChange, onSubmit, onReset }) {
  return (
    <form onSubmit={onSubmit} className="w-full rounded-xl border border-zinc-200 bg-white p-4">
      <div className="grid gap-3 md:grid-cols-[minmax(0,2fr)_minmax(180px,0.8fr)_auto] md:items-end">
        <CompactFilter label="Thời gian">
          <div className="grid grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-1.5">
            <MonthInput value={filters.fromMonth} max={currentMonth} onChange={(value) => onChange('fromMonth', value)} />
            <span className="text-zinc-300">–</span>
            <MonthInput value={filters.toMonth} max={currentMonth} onChange={(value) => onChange('toMonth', value)} />
          </div>
        </CompactFilter>

        <CompactFilter label="Số ngày không hoạt động">
          <div className="relative">
            <select
              value={filters.inactiveDays}
              onChange={(event) => onChange('inactiveDays', event.target.value)}
              className="h-9 w-full appearance-none rounded-lg border border-zinc-200 bg-white px-3 pr-8 text-xs font-semibold text-zinc-900 outline-none focus:border-zinc-900 focus:ring-2 focus:ring-zinc-200"
            >
              {[15].map((days) => <option key={days} value={days}>{days} ngày</option>)}
            </select>
            <ChevronDown size={13} className="pointer-events-none absolute right-2.5 top-1/2 -translate-y-1/2 text-zinc-500" aria-hidden="true" />
          </div>
        </CompactFilter>

        <div className="flex justify-end gap-2">
          <Button type="submit" size="sm" disabled={loading} className="gap-2 px-4">
            <SlidersHorizontal size={14} aria-hidden="true" /> Xem kết quả
          </Button>
          <Button type="button" size="sm" variant="secondary" disabled={loading} onClick={onReset} className="rounded-lg px-2.5">
            <RotateCcw size={14} aria-hidden="true" />
            <span className="sr-only">Đặt lại bộ lọc</span>
          </Button>
        </div>
      </div>
      {error ? <p className="mt-2 text-right text-xs font-semibold text-red-600">{error}</p> : null}
    </form>
  );
}

function CompactFilter({ label, children }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-[10px] font-semibold uppercase tracking-[0.06em] text-zinc-500">{label}</span>
      {children}
    </label>
  );
}

function MonthInput({ value, max, onChange }) {
  return (
    <input
      type="month"
      required
      max={max}
      value={value}
      onChange={(event) => onChange(event.target.value)}
      className="h-9 w-full min-w-0 rounded-lg border border-zinc-200 bg-white px-2 text-xs font-semibold text-zinc-900 outline-none focus:border-zinc-900 focus:ring-2 focus:ring-zinc-200"
    />
  );
}

function AnalyticsError({ message, onRetry }) {
  return (
    <div className="flex flex-col gap-3 rounded-2xl border border-red-200 bg-red-50 p-4 sm:flex-row sm:items-center">
      <div className="min-w-0 flex-1">
        <p className="font-semibold text-red-800">Không thể tải thống kê</p>
        <p className="mt-1 text-sm text-red-700">{message}</p>
      </div>
      <Button variant="secondary" size="sm" onClick={onRetry} className="gap-2 border-red-200 text-red-700">
        <RefreshCw size={14} aria-hidden="true" /> Thử lại
      </Button>
    </div>
  );
}

function AnalyticsContent({ monthly, summary }) {
  const cards = [
    {
      label: 'Nhiều người quay lại nhất',
      value: monthly.peakReturningMonth ? formatMonthCompact(monthly.peakReturningMonth) : '—',
      helper: `${formatCount(monthly.peakReturningUserCount)} người`,
      tone: 'border-t-sky-500',
      helperTone: 'text-sky-700',
    },
    {
      label: 'Tỷ lệ quay lại cao nhất',
      value: formatRate(monthly.peakReturnRate),
      helper: monthly.peakReturnRateMonth ? formatMonth(monthly.peakReturnRateMonth) : 'Chưa ghi nhận',
      tone: 'border-t-violet-500',
      helperTone: 'text-violet-700',
    },
    {
      label: 'Tỷ lệ quay lại tháng đã chọn',
      value: formatRate(summary.returnRate),
      helper: formatMonth(summary.month),
      tone: 'border-t-amber-500',
      helperTone: 'text-amber-700',
    },
    {
      label: 'Người dùng được thống kê',
      value: formatCount(summary.eligibleSystemUserCount),
      helper: `Tính đến ${formatEvaluationDate(summary.evaluationDate)}`,
      tone: 'border-t-emerald-500',
      helperTone: 'text-emerald-700',
    },
  ];

  return (
    <div className="space-y-5">
      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {cards.map((card) => <KpiCard key={card.label} {...card} />)}
      </div>

      {/* Trên desktop, đặt biểu đồ và bảng snapshot cạnh nhau để tận dụng chiều ngang và tránh cuộn. */}
      <div className="grid items-stretch gap-5 min-[1800px]:grid-cols-[920px_minmax(320px,1fr)]">
        <ReturningTrendChart monthly={monthly} />
        <MonthlySnapshotTable summary={summary} inactiveDays={monthly.inactiveDays} />
      </div>
    </div>
  );
}

function KpiCard({ label, value, helper, tone, helperTone }) {
  return (
    <article className={`min-h-28 rounded-xl border border-t-2 border-zinc-200 bg-white p-4 ${tone}`}>
      <p className="text-xs font-medium text-zinc-500">{label}</p>
      <p className="mt-3 text-2xl font-semibold tracking-tight text-zinc-950">{value}</p>
      <p className={`mt-1 text-xs font-semibold ${helperTone}`}>{helper}</p>
    </article>
  );
}

function ReturningTrendChart({ monthly }) {
  const items = monthly.items;
  const countMaximum = roundedAxisMaximum(items.map((item) => item.returningUserCount));
  // Trục tỷ lệ luôn dùng toàn thang 0–100% để phản ánh đúng ý nghĩa phần trăm.
  const rateMaximum = 100;
  const countPoints = createChartPoints(items, (item) => item.returningUserCount, countMaximum);
  const ratePoints = createChartPoints(items, (item) => item.returnRate ?? 0, rateMaximum);
  const countPath = toSvgPath(countPoints);
  const ratePath = toSvgPath(ratePoints);
  const baseline = CHART.height - CHART.bottom;
  const areaPath = countPoints.length ? `${countPath} L ${countPoints.at(-1).x} ${baseline} L ${countPoints[0].x} ${baseline} Z` : '';

  return (
    <section className="flex min-w-0 flex-col rounded-xl border border-zinc-200 bg-white p-4 sm:p-5">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h2 className="text-base font-bold text-zinc-950">
            Người dùng quay lại theo tháng ({monthly.fromMonth} – {monthly.toMonth})
          </h2>
          <p className="mt-1 text-xs text-zinc-500">So sánh số người và tỷ lệ quay lại theo từng tháng.</p>
        </div>
        <div className="flex flex-wrap gap-4 text-[11px] font-semibold text-zinc-600">
          <Legend color="#7c3aed" label="Số người quay lại" />
          <Legend color="#0f766e" label="Tỷ lệ quay lại" dashed />
        </div>
      </div>

      <div className="mt-4 min-w-0 flex-1">
        <svg
          viewBox={`0 0 ${CHART.width} ${CHART.height}`}
          className="h-auto w-full"
          role="img"
          aria-label="Biểu đồ số người dùng quay lại và tỷ lệ tái kích hoạt theo tháng"
        >
          <defs>
            <linearGradient id="returning-area" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#7c3aed" stopOpacity="0.16" />
              <stop offset="100%" stopColor="#7c3aed" stopOpacity="0.01" />
            </linearGradient>
          </defs>

          {[0, 0.25, 0.5, 0.75, 1].map((ratio) => {
            const y = CHART.top + ratio * (CHART.height - CHART.top - CHART.bottom);
            return (
              <g key={ratio}>
                <line x1={CHART.left} x2={CHART.width - CHART.right} y1={y} y2={y} stroke="#e4e4e7" strokeDasharray="3 4" />
                <text x={CHART.left - 10} y={y + 4} textAnchor="end" className="fill-zinc-400 text-[10px]">
                  {formatCount(Math.round(countMaximum * (1 - ratio)))}
                </text>
                <text x={CHART.width - CHART.right + 10} y={y + 4} className="fill-zinc-400 text-[10px]">
                  {Math.round(rateMaximum * (1 - ratio))}%
                </text>
              </g>
            );
          })}

          {areaPath ? <path d={areaPath} fill="url(#returning-area)" /> : null}
          {countPath ? <path d={countPath} fill="none" stroke="#7c3aed" strokeWidth="2.5" strokeLinejoin="round" strokeLinecap="round" /> : null}
          {ratePath ? <path d={ratePath} fill="none" stroke="#0f766e" strokeWidth="2" strokeDasharray="6 5" strokeLinejoin="round" strokeLinecap="round" /> : null}

          {countPoints.map((point, index) => (
            <g key={`count-${items[index].month}`}>
              <circle cx={point.x} cy={point.y} r="4" fill="#7c3aed">
                <title>{`${formatMonth(items[index].month)}: ${formatCount(items[index].returningUserCount)} người quay lại`}</title>
              </circle>
              <text x={point.x} y={CHART.height - 16} textAnchor="middle" className="fill-zinc-500 text-[10px]">
                {formatMonthAxis(items[index].month)}
              </text>
            </g>
          ))}

          {ratePoints.map((point, index) => (
            <circle key={`rate-${items[index].month}`} cx={point.x} cy={point.y} r="3.5" fill="#0f766e">
              <title>{`${formatMonth(items[index].month)}: ${formatRate(items[index].returnRate)}`}</title>
            </circle>
          ))}
        </svg>
      </div>
    </section>
  );
}

function Legend({ color, label, dashed = false }) {
  return (
    <span className="inline-flex items-center gap-1.5">
      <span className={`inline-block w-5 border-t-2 ${dashed ? 'border-dashed' : ''}`} style={{ borderColor: color }} />
      {label}
    </span>
  );
}

function MonthlySnapshotTable({ summary, inactiveDays }) {
  const rows = [
    { label: 'Người dùng được thống kê', count: summary.eligibleSystemUserCount },
    { label: 'Đang hoạt động', count: summary.activeUserCount },
    { label: 'Hoạt động lần đầu', count: summary.newActiveUserCount },
    { label: 'Hoạt động thường xuyên', count: summary.regularActiveUserCount },
    { label: 'Quay lại hoạt động', count: summary.returningUserCount },
    { label: 'Mới ngừng hoạt động', count: summary.recentlyInactiveUserCount },
    { label: `Quá ${inactiveDays} ngày chưa quay lại`, count: summary.eligibleInactiveNotReturnedUserCount, highlighted: true },
    { label: 'Chưa từng hoạt động', count: summary.neverActiveUserCount },
    { label: 'Không hoạt động', count: summary.inactiveUserCount },
  ];

  return (
    <section className="overflow-hidden rounded-xl border border-zinc-200 bg-white">
      <div className="flex flex-col gap-2 border-b border-violet-100 bg-violet-50/50 p-4">
        <div>
          <h2 className="text-base font-bold text-zinc-950">Chi tiết tháng {formatMonthCompact(summary.month)}</h2>
          <p className="mt-1 flex items-center gap-1.5 text-xs text-zinc-500">
            <CalendarDays size={13} aria-hidden="true" /> Dữ liệu đến ngày {formatEvaluationDate(summary.evaluationDate)}
          </p>
        </div>
        <span className="w-fit rounded-md bg-amber-100 px-2.5 py-1 text-xs font-semibold text-amber-800">Trên {inactiveDays} ngày</span>
      </div>

      <div className="min-w-0">
        <table className="w-full table-fixed text-left text-sm">
          <colgroup>
            <col className="w-[72%]" />
            <col className="w-[28%]" />
          </colgroup>
          <thead className="bg-violet-50/70 text-[10px] uppercase tracking-[0.1em] text-violet-800">
            <tr>
              <th className="px-4 py-3 font-bold">Nhóm người dùng</th>
              <th className="px-4 py-3 text-right font-bold">Số lượng</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100">
            {rows.map((row) => (
              <tr key={row.label} className={row.highlighted ? 'bg-amber-50/70' : 'hover:bg-violet-50/30'}>
                <td className={`break-words px-4 py-3 font-medium ${row.highlighted ? 'text-amber-900' : 'text-zinc-800'}`}>{row.label}</td>
                <td className={`px-4 py-3 text-right font-semibold ${row.highlighted ? 'text-amber-900' : 'text-zinc-950'}`}>{formatCount(row.count)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function createChartPoints(items, accessor, maximum) {
  const plotWidth = CHART.width - CHART.left - CHART.right;
  const plotHeight = CHART.height - CHART.top - CHART.bottom;
  const divisor = Math.max(items.length - 1, 1);
  return items.map((item, index) => ({
    x: CHART.left + (index / divisor) * plotWidth,
    y: CHART.top + (1 - Math.min(Math.max(accessor(item), 0), maximum) / maximum) * plotHeight,
  }));
}

function toSvgPath(points) {
  return points.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`).join(' ');
}

function roundedAxisMaximum(values, step = 10, ceiling = Number.POSITIVE_INFINITY) {
  const maximum = Math.max(...values, 1);
  return Math.min(Math.ceil(maximum / step) * step, ceiling);
}

function formatMonthCompact(month) {
  if (!month) return '—';
  const [year, value] = month.split('-');
  return `${value}/${year}`;
}

function formatMonthAxis(month) {
  const [year, value] = month.split('-');
  return `${value}/${year}`;
}

function formatEvaluationDate(value) {
  if (!value) return '—';
  const [year, month, day] = value.split('-');
  return `${day}/${month}/${year}`;
}
