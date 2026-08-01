import { useEffect, useState } from 'react';
import { Ban } from 'lucide-react';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import Pagination from '../../../components/common/Pagination.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import { socialApi } from '../../../api/index.js';
import { isRequestCanceled } from '../../../api/apiError.js';
import { useApp } from '../../../contexts/AppContext.jsx';

/** Trang cài đặt chỉ hiển thị danh sách Block của tài khoản đang đăng nhập. */
export default function BlockedUsersPage() {
  const { invalidateUserRelationshipData, showToast } = useApp();
  const [page, setPage] = useState(0);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [target, setTarget] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    socialApi.getBlockedUsers({ page, size: 20 }, controller.signal)
      .then((response) => {
        setResult(response);
        setError('');
      })
      .catch((requestError) => {
        if (!isRequestCanceled(requestError)) setError(requestError.message);
      });
    return () => controller.abort();
  }, [page]);

  async function confirmUnblock() {
    if (!target || submitting) return;
    setSubmitting(true);
    try {
      await socialApi.unblockUser(target.userId);
      // Unblock không khôi phục Follow; chỉ làm mới các danh sách có thể nhìn thấy lại dữ liệu.
      invalidateUserRelationshipData();
      setResult((current) => ({
        ...current,
        content: current.content.filter((item) => item.userId !== target.userId),
        totalElements: Math.max(0, current.totalElements - 1),
      }));
      showToast(`Đã bỏ chặn ${target.displayName}.`);
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
          <h1 className="text-xl font-extrabold tracking-[-0.015em] text-[var(--app-text)]">Tài khoản đã chặn</h1>
          <p className="mt-1.5 max-w-xl text-sm leading-6 text-[var(--app-muted)]">
            Tài khoản đã chặn không thể xem hồ sơ hay tương tác với bạn.
          </p>
        </header>
        <div>
          {!result && !error ? <LoadingState message="Đang tải danh sách..." /> : null}
          {error ? <EmptyState title="Không thể tải danh sách" description={error} /> : null}
          {result && result.content.length === 0 ? <EmptyState title="Chưa chặn tài khoản nào" description="Các tài khoản bạn chặn sẽ xuất hiện tại đây." /> : null}
          <div>
            {result?.content.map((user) => (
              <div key={user.userId} className="flex items-center gap-3.5 border-b border-[var(--app-border)] py-4 last:border-b-0">
                <Avatar src={user.avatarUrl} name={user.displayName} size="md" />
                <div className="min-w-0 flex-1">
                  <p className="truncate text-[15px] font-bold text-[var(--app-text)]">{user.displayName}</p>
                  <p className="mt-0.5 flex items-center gap-1.5 text-xs text-[var(--app-muted)]">
                    <Ban size={13} strokeWidth={2} aria-hidden="true" />
                    Đang bị chặn
                  </p>
                </div>
                <Button variant="secondary" size="sm" disabled={submitting} onClick={() => setTarget(user)}>Bỏ chặn</Button>
              </div>
            ))}
          </div>
          {result?.totalPages > 1 ? <Pagination
            currentPage={result.page + 1}
            totalPages={result.totalPages}
            totalItems={result.totalElements}
            pageSize={result.size}
            onPageChange={(nextPage) => setPage(nextPage - 1)}
          /> : null}
        </div>
      </section>
      <Modal
        open={Boolean(target)}
        title="Bỏ chặn người dùng?"
        onClose={() => !submitting && setTarget(null)}
        footer={<>
          <Button variant="secondary" disabled={submitting} onClick={() => setTarget(null)}>Hủy</Button>
          <Button disabled={submitting} onClick={confirmUnblock}>{submitting ? 'Đang xử lý...' : 'Bỏ chặn'}</Button>
        </>}
      >
        Bạn sẽ có thể xem và tương tác với tài khoản này, nhưng hệ thống không tự động theo dõi lại.
      </Modal>
    </>
  );
}
