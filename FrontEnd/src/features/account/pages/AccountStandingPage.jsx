import { useEffect, useState } from 'react';
import { CheckCircle2, RefreshCw, ShieldAlert } from 'lucide-react';
import { Link } from 'react-router-dom';
import Button from '../../../components/common/Button.jsx';
import { accountApi } from '../services/accountApi.js';

// Màn hình chỉ trình bày số liệu authoritative do Account Standing API trả về.
export default function AccountStandingPage() {
  const [standing, setStanding] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    accountApi.getStanding(controller.signal)
      .then((data) => { setStanding(data); setError(''); })
      .catch((requestError) => {
        if (requestError.code !== 'ERR_CANCELED') setError(requestError.message);
      })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [revision]);

  if (loading) return <div className="p-6 text-sm text-[var(--app-muted)]">Đang tải trạng thái tài khoản...</div>;
  if (error) return (
    <div className="p-6" role="alert">
      <p className="text-sm text-red-600">{error}</p>
      <Button className="mt-4" variant="secondary" onClick={() => { setLoading(true); setRevision((value) => value + 1); }}>
        <RefreshCw size={16} /> Thử lại
      </Button>
    </div>
  );

  const count = standing?.confirmedViolationCount ?? 0;
  const threshold = standing?.violationThreshold ?? 3;
  const clean = count === 0;
  return (
    <section className="p-5 sm:p-7" aria-labelledby="account-standing-title">
      <h2 id="account-standing-title" className="text-xl font-extrabold text-[var(--app-text)]">Trạng thái tài khoản</h2>
      <div className={`mt-5 rounded-2xl border p-5 ${clean ? 'border-emerald-500/20 bg-emerald-500/[0.06]' : 'border-amber-500/25 bg-amber-500/[0.07]'}`}>
        <div className="flex items-center gap-3">
          {clean ? <CheckCircle2 className="text-emerald-600" /> : <ShieldAlert className="text-amber-600" />}
          <p className="font-bold text-[var(--app-text)]">
            {clean ? 'Tài khoản đang hoạt động tốt' : 'Tài khoản đang có vi phạm đã xác nhận'}
          </p>
        </div>
        <div className="mt-6">
          <p className="text-sm font-semibold text-[var(--app-muted)]">Vi phạm đã xác nhận</p>
          <p className="mt-1 text-4xl font-black tracking-tight text-[var(--app-text)]">{count} / {threshold}</p>
          <div className="mt-3 h-2 overflow-hidden rounded-full bg-[var(--app-border)]">
            <div className="h-full rounded-full bg-[var(--app-brand)]" style={{ width: `${Math.min(100, (count / threshold) * 100)}%` }} />
          </div>
          <p className="mt-4 max-w-lg text-sm leading-6 text-[var(--app-muted)]">
            {standing?.remainingBeforeBlock > 0
              ? `Bạn còn ${standing.remainingBeforeBlock} lần vi phạm được xác nhận trước khi tài khoản bị khóa tự động.`
              : 'Tài khoản đã đạt ngưỡng khóa tự động. Việc mở khóa không xóa lịch sử vi phạm.'}
          </p>
        </div>
      </div>
      <Link to="/policies/community-standards" className="mt-5 inline-flex font-semibold text-[var(--app-brand)] hover:underline">
        Xem Tiêu chuẩn cộng đồng
      </Link>
    </section>
  );
}
