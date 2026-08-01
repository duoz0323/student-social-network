import { useEffect, useState } from 'react';
import { Shield } from 'lucide-react';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import Pagination from '../../../components/common/Pagination.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import { socialApi } from '../../../api/index.js';
import { isRequestCanceled } from '../../../api/apiError.js';
import { useApp } from '../../../contexts/AppContext.jsx';

/** Trang quyền riêng tư chỉ hiển thị các Restrict do current user thiết lập. */
export default function RestrictedUsersPage() {
  const { showToast } = useApp();
  const [page, setPage] = useState(0);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [target, setTarget] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    socialApi.getRestrictedUsers({ page, size: 20 }, controller.signal)
      .then((response) => {
        setResult(response);
        setError('');
      })
      .catch((requestError) => {
        if (!isRequestCanceled(requestError)) setError(requestError.message);
      });
    return () => controller.abort();
  }, [page]);

  async function confirmUnrestrict() {
    if (!target || submitting) return;
    setSubmitting(true);
    try {
      await socialApi.unrestrictUser(target.userId);
      setResult((current) => ({
        ...current,
        content: current.content.filter((item) => item.userId !== target.userId),
        totalElements: Math.max(0, current.totalElements - 1),
      }));
      showToast(`Đã bỏ hạn chế ${target.displayName}.`);
      setTarget(null);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <section className="px-5 pb-8 pt-6 sm:px-7 sm:pt-7">
        <header className="border-b border-[var(--app-border)] pb-5">
          <h1 className="text-xl font-extrabold tracking-[-0.015em] text-[var(--app-text)]">Tài khoản đã hạn chế</h1>
          <p className="mt-1.5 max-w-xl text-sm leading-6 text-[var(--app-muted)]">
            Bạn sẽ không nhận thông báo khi các tài khoản này tương tác.
          </p>
        </header>
        <div>
          {!result && !error ? <LoadingState message="Đang tải danh sách..." /> : null}
          {error ? <EmptyState title="Không thể tải danh sách" description={error} /> : null}
          {result && result.content.length === 0 ? <EmptyState title="Chưa hạn chế tài khoản nào" description="Các tài khoản bạn hạn chế sẽ xuất hiện tại đây." /> : null}
          <div>
            {result?.content.map((user) => (
              <div key={user.userId} className="flex items-center gap-3.5 border-b border-[var(--app-border)] py-4 last:border-b-0">
                <Avatar src={user.avatarUrl} name={user.displayName} size="md" />
                <div className="min-w-0 flex-1">
                  <p className="truncate text-[15px] font-bold text-[var(--app-text)]">{user.displayName}</p>
                  <p className="mt-0.5 flex items-center gap-1.5 text-xs text-[var(--app-muted)]">
                    <Shield size={13} strokeWidth={2} aria-hidden="true" />
                    Đang bị hạn chế
                  </p>
                </div>
                <Button
                  variant="secondary"
                  size="sm"
                  disabled={submitting}
                  onClick={() => setTarget(user)}
                >
                  Bỏ hạn chế
                </Button>
              </div>
            ))}
          </div>
          {result?.totalPages > 1 ? <Pagination currentPage={result.page + 1} totalPages={result.totalPages}
            totalItems={result.totalElements} pageSize={result.size} onPageChange={(next) => setPage(next - 1)} /> : null}
        </div>
      </section>
      <Modal open={Boolean(target)} title="Bỏ hạn chế tài khoản này?"
        onClose={() => !submitting && setTarget(null)}
        footer={<><Button variant="secondary" onClick={() => setTarget(null)}>Hủy</Button>
          <Button className="gap-2" disabled={submitting} onClick={confirmUnrestrict}>
            {submitting ? 'Đang xử lý...' : 'Bỏ hạn chế'}
            {!submitting ? <Shield size={16} strokeWidth={2} aria-hidden="true" /> : null}
          </Button></>}>
        Bạn sẽ tiếp tục nhận các thông báo tương tác mới từ tài khoản này.
      </Modal>
    </>
  );
}
