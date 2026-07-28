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
      <ContentShell header={<div className="flex h-[var(--header-height)] items-center px-6"><h1 className="text-lg font-bold">Tài khoản đã hạn chế</h1></div>}>
        <div className="p-6">
          {!result && !error ? <LoadingState message="Đang tải danh sách..." /> : null}
          {error ? <EmptyState title="Không thể tải danh sách" description={error} /> : null}
          {result && result.content.length === 0 ? <EmptyState title="Chưa hạn chế tài khoản nào" description="Các tài khoản bạn hạn chế sẽ xuất hiện tại đây." /> : null}
          <div className="space-y-2">
            {result?.content.map((user) => (
              <div key={user.userId} className="flex items-center gap-3 rounded-xl border border-[var(--app-border)] p-3">
                <Avatar src={user.avatarUrl} name={user.displayName} size="sm" />
                <span className="min-w-0 flex-1 truncate font-semibold">{user.displayName}</span>
                <Button variant="secondary" disabled={submitting} onClick={() => setTarget(user)}>Bỏ hạn chế</Button>
              </div>
            ))}
          </div>
          {result?.totalPages > 1 ? <Pagination currentPage={result.page + 1} totalPages={result.totalPages}
            totalItems={result.totalElements} pageSize={result.size} onPageChange={(next) => setPage(next - 1)} /> : null}
        </div>
      </ContentShell>
      <Modal open={Boolean(target)} title="Bỏ hạn chế tài khoản này?"
        onClose={() => !submitting && setTarget(null)}
        footer={<><Button variant="secondary" onClick={() => setTarget(null)}>Hủy</Button>
          <Button disabled={submitting} onClick={confirmUnrestrict}>{submitting ? 'Đang xử lý...' : 'Bỏ hạn chế'}</Button></>}>
        Bạn sẽ tiếp tục nhận các thông báo tương tác mới từ tài khoản này.
      </Modal>
    </>
  );
}
