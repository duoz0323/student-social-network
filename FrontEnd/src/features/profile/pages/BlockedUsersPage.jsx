import { useEffect, useState } from 'react';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import Pagination from '../../../components/common/Pagination.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';
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
      <ContentShell header={<div className="flex h-[var(--header-height)] items-center px-6"><h1 className="text-lg font-bold">Tài khoản đã chặn</h1></div>}>
        <div className="p-6">
          {!result && !error ? <LoadingState message="Đang tải danh sách..." /> : null}
          {error ? <EmptyState title="Không thể tải danh sách" description={error} /> : null}
          {result && result.content.length === 0 ? <EmptyState title="Chưa chặn tài khoản nào" description="Các tài khoản bạn chặn sẽ xuất hiện tại đây." /> : null}
          <div className="space-y-2">
            {result?.content.map((user) => (
              <div key={user.userId} className="flex items-center gap-3 rounded-xl border border-[var(--app-border)] p-3">
                <Avatar src={user.avatarUrl} name={user.displayName} size="sm" />
                <span className="min-w-0 flex-1 truncate font-semibold">{user.displayName}</span>
                <Button variant="secondary" disabled={submitting} onClick={() => setTarget(user)}>Bỏ chặn</Button>
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
      </ContentShell>
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
