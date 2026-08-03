import { useMemo, useState } from 'react';
import {
  Activity,
  CalendarRange,
  RefreshCw,
  RotateCcw,
  TrendingUp,
  UserCheck,
  UserRoundPlus,
  UsersRound,
} from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import { useUserEngagementAnalytics } from '../hooks/useUserEngagementAnalytics.js';
import {
  USER_GROUPS,
  createDefaultAnalyticsFilters,
  formatCount,
  formatMonth,
  formatRate,
  hasEligibleUsers,
  validateAnalyticsFilters,
} from '../utils/userEngagementAnalytics.js';

/**
 * Trang Analytics độc lập của Admin; không nhúng dữ liệu engagement vào Dashboard tổng quan.
 */
export default function AdminUserAnalyticsPage() {
  const defaults = useMemo(() => createDefaultAnalyticsFilters(), []);
  const [draftFilters, setDraftFilters] = useState(defaults);
  const [appliedFilters, setAppliedFilters] = useState(defaults);
  const [formError, setFormError] = useState('');
  const { monthly, summary, loading, error, retry } = useUserEngagementAnalytics(appliedFilters);
  const currentMonth = defaults.toMonth;

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
    <section className="space-y-6">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <div className="flex items-center gap-2 text-sm font-semibold text-blue-700">
            <Activity size={17} aria-hidden="true" /> Analytics người dùng
          </div>
          <h1 className="mt-2 text-3xl font-bold tracking-tight text-zinc-950">Thống kê hoạt động người dùng</h1>
          <p className="mt-2 max-w-3xl text-sm leading-6 text-zinc-500">
            Theo dõi mức độ sử dụng, người dùng mới, hoạt động thường xuyên và khả năng quay trở lại theo từng tháng.
          </p>
        </div>
        <span className="inline-flex w-fit items-center gap-2 rounded-full border border-zinc-200 bg-zinc-50 px-3 py-1.5 text-xs font-semibold text-zinc-600">
          <CalendarRange size={14} aria-hidden="true" /> Múi giờ đánh giá: UTC
        </span>
      </header>

      <AnalyticsFilterForm
        filters={draftFilters}
        currentMonth={currentMonth}
        loading={loading}
        error={formError}
        onChange={updateFilter}
        onSubmit={submitFilters}
        onReset={resetFilters}
      />

      {error ? <AnalyticsError message={error} onRetry={retry} /> : null}
      {loading ? <LoadingState message="Đang tổng hợp hoạt động người dùng..." /> : null}

      {!loading && !error && monthly && summary ? (
        hasEligibleUsers(monthly.items) ? (
          <AnalyticsContent monthly={monthly} summary={summary} />
        ) : (
          <EmptyState
            title="Chưa có tài khoản đủ điều kiện thống kê"
            description="Khoảng tháng đã chọn chưa có USER đang hoạt động và hoàn tất hồ sơ tại ngày đánh giá."
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
    <form onSubmit={onSubmit} className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
      <div className="grid gap-4 md:grid-cols-[1fr_1fr_1fr_auto] md:items-end">
        <FilterField label="Từ tháng">
          <input
            type="month"
            required
            max={currentMonth}
            value={filters.fromMonth}
            onChange={(event) => onChange('fromMonth', event.target.value)}
            className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
          />
        </FilterField>
        <FilterField label="Đến tháng">
          <input
            type="month"
            required
            max={currentMonth}
            value={filters.toMonth}
            onChange={(event) => onChange('toMonth', event.target.value)}
            className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
          />
        </FilterField>
        <FilterField label="Ngưỡng không hoạt động">
          <div className="relative">
            <input
              type="number"
              min="1"
              max="365"
              step="1"
              required
              value={filters.inactiveDays}
              onChange={(event) => onChange('inactiveDays', event.target.value)}
              className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 pr-14 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            />
            <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-xs text-zinc-400">ngày</span>
          </div>
        </FilterField>
        <div className="flex gap-2">
          <Button type="submit" disabled={loading} className="min-w-28 gap-2">
            <TrendingUp size={16} aria-hidden="true" /> Áp dụng
          </Button>
          <Button type="button" variant="secondary" disabled={loading} onClick={onReset} className="gap-2 px-3">
            <RotateCcw size={16} aria-hidden="true" />
            <span className="sr-only">Đặt lại bộ lọc</span>
          </Button>
        </div>
      </div>
      <div className="mt-3 flex flex-wrap items-center justify-between gap-2 text-xs text-zinc-500">
        <span>Tối đa 24 tháng. Đúng ngưỡng vẫn là hoạt động thường xuyên; phải lớn hơn ngưỡng mới tính quay lại.</span>
        {error ? <span className="font-semibold text-red-600">{error}</span> : null}
      </div>
    </form>
  );
}

function FilterField({ label, children }) {
  return (
    <label className="space-y-2 text-sm font-semibold text-zinc-700">
      <span>{label}</span>
      {children}
    </label>
  );
}

function AnalyticsError({ message, onRetry }) {
  return (
    <div className="flex flex-col gap-3 rounded-2xl border border-red-200 bg-red-50 p-5 sm:flex-row sm:items-center">
      <div className="min-w-0 flex-1">
        <p className="font-semibold text-red-800">Không thể tải thống kê</p>
        <p className="mt-1 text-sm text-red-700">{message}</p>
      </div>
      <Button variant="secondary" onClick={onRetry} className="gap-2 border-red-200 text-red-700">
        <RefreshCw size={15} aria-hidden="true" /> Thử lại
      </Button>
    </div>
  );
}

function AnalyticsContent({ monthly, summary }) {
  const summaryCards = [
    { label: 'USER hợp lệ', value: formatCount(summary.eligibleSystemUserCount), helper: formatMonth(summary.month), icon: UsersRound, tone: 'blue' },
    { label: 'Có hoạt động', value: formatCount(summary.activeUserCount), helper: formatRate(summary.activeUserRate), icon: UserCheck, tone: 'green' },
    { label: 'Mới hoạt động', value: formatCount(summary.newActiveUserCount), helper: 'Lần đầu sử dụng hệ thống', icon: UserRoundPlus, tone: 'violet' },
    { label: 'Quay trở lại', value: formatCount(summary.returningUserCount), helper: `Tỷ lệ tái kích hoạt: ${formatRate(summary.returnRate)}`, icon: RefreshCw, tone: 'amber' },
  ];

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {summaryCards.map((card) => <SummaryCard key={card.label} {...card} />)}
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <PeakCard
          eyebrow="Số người quay lại cao nhất"
          month={monthly.peakReturningMonth}
          value={`${formatCount(monthly.peakReturningUserCount)} người`}
        />
        <PeakCard
          eyebrow="Tỷ lệ tái kích hoạt cao nhất"
          month={monthly.peakReturnRateMonth}
          value={formatRate(monthly.peakReturnRate)}
        />
      </div>

      <MonthlyClassificationChart items={monthly.items} />
      <MonthlyMetricsTable items={monthly.items} />
      <MetricDefinitions inactiveDays={monthly.inactiveDays} />
    </div>
  );
}

function SummaryCard({ label, value, helper, icon: Icon, tone }) {
  const tones = {
    blue: 'bg-blue-50 text-blue-700',
    green: 'bg-emerald-50 text-emerald-700',
    violet: 'bg-violet-50 text-violet-700',
    amber: 'bg-amber-50 text-amber-700',
  };
  return (
    <article className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
      <div className={`flex h-10 w-10 items-center justify-center rounded-xl ${tones[tone]}`}>
        <Icon size={19} aria-hidden="true" />
      </div>
      <p className="mt-4 text-sm font-medium text-zinc-500">{label}</p>
      <p className="mt-1 text-3xl font-bold tracking-tight text-zinc-950">{value}</p>
      <p className="mt-2 text-xs leading-5 text-zinc-500">{helper}</p>
    </article>
  );
}

function PeakCard({ eyebrow, month, value }) {
  return (
    <article className="flex items-center gap-4 rounded-2xl border border-zinc-200 bg-zinc-950 p-5 text-white shadow-sm">
      <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-white/10">
        <TrendingUp size={20} aria-hidden="true" />
      </div>
      <div className="min-w-0">
        <p className="text-xs font-semibold uppercase tracking-wide text-zinc-400">{eyebrow}</p>
        <p className="mt-1 text-xl font-bold">{value}</p>
        <p className="mt-1 text-sm text-zinc-300">{month ? formatMonth(month) : 'Chưa đủ dữ liệu đánh giá'}</p>
      </div>
    </article>
  );
}

function MonthlyClassificationChart({ items }) {
  const maximum = Math.max(...items.map((item) => item.eligibleSystemUserCount), 1);
  return (
    <section className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wide text-blue-700">Cơ cấu tài khoản</p>
          <h2 className="mt-1 text-xl font-bold text-zinc-950">Sáu nhóm người dùng theo tháng</h2>
          <p className="mt-1 text-sm text-zinc-500">Mỗi USER hợp lệ chỉ xuất hiện trong đúng một nhóm tại ngày đánh giá.</p>
        </div>
        <div className="flex max-w-xl flex-wrap gap-x-4 gap-y-2">
          {USER_GROUPS.map((group) => (
            <span key={group.key} className="inline-flex items-center gap-1.5 text-xs text-zinc-600">
              <span className="h-2.5 w-2.5 rounded-sm" style={{ backgroundColor: group.color }} /> {group.shortLabel}
            </span>
          ))}
        </div>
      </div>

      <div className="mt-6 overflow-x-auto pb-2">
        <div className="flex min-w-max items-end gap-4 border-b border-zinc-200 px-2" style={{ width: `${Math.max(items.length * 78, 720)}px` }}>
          {items.map((item) => (
            <div key={item.month} className="flex w-16 shrink-0 flex-col items-center">
              <span className="mb-2 text-xs font-bold text-zinc-700">{formatCount(item.eligibleSystemUserCount)}</span>
              <div className="flex h-56 w-10 flex-col-reverse overflow-hidden rounded-t-lg bg-zinc-100" role="img" aria-label={`Cơ cấu ${formatMonth(item.month)}`}>
                {USER_GROUPS.map((group) => {
                  const count = item[group.key];
                  return count > 0 ? (
                    <div
                      key={group.key}
                      title={`${group.label}: ${formatCount(count)}`}
                      style={{ height: `${(count / maximum) * 100}%`, backgroundColor: group.color }}
                    />
                  ) : null;
                })}
              </div>
              <span className="my-2 text-[11px] font-semibold text-zinc-500">{item.month.slice(5)}/{item.month.slice(0, 4)}</span>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function MonthlyMetricsTable({ items }) {
  return (
    <section className="overflow-hidden rounded-2xl border border-zinc-200 bg-white shadow-sm">
      <div className="border-b border-zinc-200 p-5">
        <p className="text-xs font-semibold uppercase tracking-wide text-blue-700">Chi tiết</p>
        <h2 className="mt-1 text-xl font-bold text-zinc-950">Chỉ số theo từng tháng</h2>
      </div>
      <div className="overflow-x-auto">
        <table className="min-w-[1180px] w-full text-left text-sm">
          <thead className="bg-zinc-50 text-xs uppercase tracking-wide text-zinc-500">
            <tr>
              <HeaderCell>Tháng</HeaderCell>
              <HeaderCell>USER hợp lệ</HeaderCell>
              <HeaderCell>Hoạt động</HeaderCell>
              <HeaderCell>Mới</HeaderCell>
              <HeaderCell>Thường xuyên</HeaderCell>
              <HeaderCell>Quay lại</HeaderCell>
              <HeaderCell>Không hoạt động</HeaderCell>
              <HeaderCell>Chưa từng dùng</HeaderCell>
              <HeaderCell>Tỷ lệ hoạt động</HeaderCell>
              <HeaderCell>Tỷ lệ tái kích hoạt</HeaderCell>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100">
            {items.map((item) => (
              <tr key={item.month} className="hover:bg-zinc-50/70">
                <DataCell><strong className="text-zinc-950">{formatMonth(item.month)}</strong><small className="mt-1 block text-zinc-400">Đến {item.evaluationDate}</small></DataCell>
                <DataCell>{formatCount(item.eligibleSystemUserCount)}</DataCell>
                <DataCell>{formatCount(item.activeUserCount)}</DataCell>
                <DataCell>{formatCount(item.newActiveUserCount)}</DataCell>
                <DataCell>{formatCount(item.regularActiveUserCount)}<small className="mt-1 block text-zinc-400">{formatRate(item.regularActiveRate)}</small></DataCell>
                <DataCell>{formatCount(item.returningUserCount)}</DataCell>
                <DataCell>{formatCount(item.inactiveUserCount)}</DataCell>
                <DataCell>{formatCount(item.neverActiveUserCount)}<small className="mt-1 block text-zinc-400">{formatRate(item.neverActiveRate)}</small></DataCell>
                <DataCell><RateBadge value={item.activeUserRate} /></DataCell>
                <DataCell><RateBadge value={item.returnRate} /></DataCell>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function HeaderCell({ children }) {
  return <th className="whitespace-nowrap px-4 py-3 font-semibold">{children}</th>;
}

function DataCell({ children }) {
  return <td className="px-4 py-4 text-zinc-700">{children}</td>;
}

function RateBadge({ value }) {
  const unavailable = value === null || value === undefined;
  return (
    <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${unavailable ? 'bg-zinc-100 text-zinc-500' : 'bg-blue-50 text-blue-700'}`}>
      {formatRate(value)}
    </span>
  );
}

function MetricDefinitions({ inactiveDays }) {
  return (
    <details className="rounded-2xl border border-zinc-200 bg-zinc-50 p-5 text-sm text-zinc-600">
      <summary className="cursor-pointer font-semibold text-zinc-900">Cách đọc các nhóm và tỷ lệ</summary>
      <div className="mt-4 grid gap-4 leading-6 md:grid-cols-2">
        <p><strong>NEW:</strong> hoạt động lần đầu trong tháng. <strong>REGULAR:</strong> khoảng cách tới hoạt động trước không quá {inactiveDays} ngày.</p>
        <p><strong>RETURNING:</strong> quay lại sau hơn {inactiveDays} ngày. Tỷ lệ tái kích hoạt chỉ dùng người đã đủ ngưỡng ngay tại đầu tháng.</p>
        <p><strong>RECENTLY_INACTIVE:</strong> không hoạt động trong tháng nhưng chưa vượt ngưỡng tại đầu tháng.</p>
        <p><strong>NEVER_ACTIVE:</strong> tài khoản đủ điều kiện nhưng chưa từng phát sinh hoạt động hợp lệ.</p>
      </div>
    </details>
  );
}
