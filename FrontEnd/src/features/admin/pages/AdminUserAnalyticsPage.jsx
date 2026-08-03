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
    <section className="space-y-5 rounded-[28px] bg-zinc-50 p-4 transition-[width,transform] sm:p-6 min-[1800px]:w-[calc(100%+28rem)] min-[1800px]:-translate-x-56">
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
      {loading ? <LoadingState message="Đang tổng hợp hoạt động người dùng..." /> : null}

      {!loading && !error && monthly && summary ? (
        hasEligibleUsers(monthly.items) ? (
          <AnalyticsContent monthly={monthly} summary={summary} />
        ) : (
          <EmptyState
            title="Chưa có tài khoản đủ điều kiện thống kê"
            description="Khoảng tháng đã chọn chưa có USER ACTIVE hoàn tất hồ sơ tại ngày đánh giá."
            actionLabel="Tải lại dữ liệu"
            onAction={retry}
          />
        )
      ) : null}
    </section>
  );
}

function AnalyticsFilterForm({ filters, currentMonth, loading, error, onChange, onSubmit, onReset }) {
  return (
    <form onSubmit={onSubmit} className="w-full rounded-2xl border border-zinc-200 bg-white p-3 shadow-sm">
      <div className="grid gap-3 md:grid-cols-[minmax(0,2fr)_minmax(180px,0.8fr)_auto] md:items-end">
        <CompactFilter label="Khoảng thời gian">
          <div className="grid grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-1.5">
            <MonthInput value={filters.fromMonth} max={currentMonth} onChange={(value) => onChange('fromMonth', value)} />
            <span className="text-zinc-300">–</span>
            <MonthInput value={filters.toMonth} max={currentMonth} onChange={(value) => onChange('toMonth', value)} />
          </div>
        </CompactFilter>

        <CompactFilter label="Ngưỡng không hoạt động">
          <div className="relative">
            <select
              value={filters.inactiveDays}
              onChange={(event) => onChange('inactiveDays', event.target.value)}
              className="h-9 w-full appearance-none rounded-lg border border-zinc-200 bg-white px-3 pr-8 text-xs font-semibold text-blue-700 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            >
              {[15].map((days) => <option key={days} value={days}>{days} ngày</option>)}
            </select>
            <ChevronDown size={13} className="pointer-events-none absolute right-2.5 top-1/2 -translate-y-1/2 text-blue-600" aria-hidden="true" />
          </div>
        </CompactFilter>

        <div className="flex justify-end gap-2">
          <Button type="submit" size="sm" disabled={loading} className="gap-2 rounded-lg bg-blue-600 px-4 text-white">
            <SlidersHorizontal size={14} aria-hidden="true" /> Áp dụng
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
      <span className="mb-1 block text-[9px] font-bold uppercase tracking-[0.12em] text-zinc-400">{label}</span>
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
      className="h-9 w-full min-w-0 rounded-lg border border-zinc-200 bg-white px-2 text-xs font-semibold text-blue-700 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
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
      label: 'Tháng có nhiều người quay lại',
      value: monthly.peakReturningMonth ? formatMonthCompact(monthly.peakReturningMonth) : '—',
      helper: `${formatCount(monthly.peakReturningUserCount)} người quay lại`,
      accent: 'border-l-blue-500',
      helperTone: 'text-blue-600',
    },
    {
      label: 'Tỷ lệ quay lại cao nhất',
      value: formatRate(monthly.peakReturnRate),
      helper: monthly.peakReturnRateMonth ? formatMonth(monthly.peakReturnRateMonth) : 'Chưa đủ dữ liệu',
      accent: 'border-l-indigo-500',
      helperTone: 'text-indigo-600',
    },
    {
      label: 'Tỷ lệ tái kích hoạt tháng cuối',
      value: formatRate(summary.returnRate),
      helper: formatMonth(summary.month),
      accent: 'border-l-orange-500',
      helperTone: 'text-orange-600',
    },
    {
      label: 'Tổng người dùng đủ điều kiện',
      value: formatCount(summary.eligibleSystemUserCount),
      helper: `Đánh giá đến ${formatEvaluationDate(summary.evaluationDate)}`,
      accent: 'border-l-blue-600',
      helperTone: 'text-zinc-500',
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

function KpiCard({ label, value, helper, accent, helperTone }) {
  return (
    <article className={`min-h-28 rounded-2xl border border-zinc-100 border-l-2 bg-white p-4 shadow-sm ${accent}`}>
      <p className="text-[10px] font-bold uppercase tracking-[0.08em] text-zinc-500">{label}</p>
      <p className="mt-3 text-2xl font-bold tracking-tight text-zinc-950">{value}</p>
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
    <section className="flex min-w-0 flex-col rounded-2xl border border-zinc-100 bg-white p-4 shadow-sm sm:p-5">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h2 className="text-base font-bold text-zinc-950">
            Xu hướng người dùng quay lại ({monthly.fromMonth} – {monthly.toMonth})
          </h2>
          <p className="mt-1 text-xs text-zinc-500">Số người quay lại và tỷ lệ tái kích hoạt thực tế theo từng tháng.</p>
        </div>
        <div className="flex flex-wrap gap-4 text-[11px] font-semibold text-zinc-600">
          <Legend color="#0b67d1" label="Người dùng quay lại" />
          <Legend color="#475b91" label="Tỷ lệ quay lại" dashed />
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
              <stop offset="0%" stopColor="#0b67d1" stopOpacity="0.15" />
              <stop offset="100%" stopColor="#0b67d1" stopOpacity="0.01" />
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
          {countPath ? <path d={countPath} fill="none" stroke="#0b67d1" strokeWidth="3" strokeLinejoin="round" strokeLinecap="round" /> : null}
          {ratePath ? <path d={ratePath} fill="none" stroke="#475b91" strokeWidth="2.5" strokeDasharray="6 5" strokeLinejoin="round" strokeLinecap="round" /> : null}

          {countPoints.map((point, index) => (
            <g key={`count-${items[index].month}`}>
              <circle cx={point.x} cy={point.y} r="4" fill="#0b67d1">
                <title>{`${formatMonth(items[index].month)}: ${formatCount(items[index].returningUserCount)} người quay lại`}</title>
              </circle>
              <text x={point.x} y={CHART.height - 16} textAnchor="middle" className="fill-zinc-500 text-[10px]">
                {formatMonthAxis(items[index].month)}
              </text>
            </g>
          ))}

          {ratePoints.map((point, index) => (
            <circle key={`rate-${items[index].month}`} cx={point.x} cy={point.y} r="3.5" fill="#475b91">
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
    { label: 'Tổng số người dùng đủ điều kiện', count: summary.eligibleSystemUserCount },
    { label: 'Tổng người dùng đang hoạt động', count: summary.activeUserCount },
    { label: 'Người dùng mới hoạt động', count: summary.newActiveUserCount },
    { label: 'Người dùng hoạt động thường xuyên', count: summary.regularActiveUserCount },
    { label: 'Người dùng quay lại', count: summary.returningUserCount },
    { label: 'Người dùng mới ngừng hoạt động', count: summary.recentlyInactiveUserCount },
    { label: `Đủ ngưỡng > ${inactiveDays} ngày, chưa quay lại`, count: summary.eligibleInactiveNotReturnedUserCount, highlighted: true },
    { label: 'Người dùng chưa từng hoạt động', count: summary.neverActiveUserCount },
    { label: 'Tổng người dùng không hoạt động', count: summary.inactiveUserCount },
  ];

  return (
    <section className="overflow-hidden rounded-2xl border border-zinc-100 bg-white shadow-sm">
      <div className="flex flex-col gap-2 border-b border-zinc-100 p-4">
        <div>
          <h2 className="text-base font-bold text-zinc-950">Bảng dữ liệu tháng {formatMonthCompact(summary.month)}</h2>
          <p className="mt-1 flex items-center gap-1.5 text-xs text-zinc-500">
            <CalendarDays size={13} aria-hidden="true" /> Đánh giá đến {formatEvaluationDate(summary.evaluationDate)} theo UTC
          </p>
        </div>
        <span className="rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700">Ngưỡng {inactiveDays} ngày</span>
      </div>

      <div className="min-w-0">
        <table className="w-full table-fixed text-left text-sm">
          <colgroup>
            <col className="w-[72%]" />
            <col className="w-[28%]" />
          </colgroup>
          <thead className="bg-zinc-50 text-[10px] uppercase tracking-[0.1em] text-zinc-500">
            <tr>
              <th className="px-4 py-3 font-bold">Chỉ số API</th>
              <th className="px-4 py-3 text-right font-bold">Số lượng</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100">
            {rows.map((row) => (
              <tr key={row.label} className={row.highlighted ? 'bg-red-50/50' : 'hover:bg-zinc-50/70'}>
                <td className={`break-words px-4 py-3 font-medium ${row.highlighted ? 'text-red-700' : 'text-zinc-800'}`}>{row.label}</td>
                <td className={`px-4 py-3 text-right font-bold ${row.highlighted ? 'text-red-700' : 'text-zinc-950'}`}>{formatCount(row.count)}</td>
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
