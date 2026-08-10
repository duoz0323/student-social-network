import { useCallback, useEffect, useState } from 'react';
import { ArrowLeft, CheckCircle2, CircleX } from 'lucide-react';
import { useNavigate, useParams } from 'react-router-dom';
import { adminApi } from '../../../api/index.js';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../utils/formatters.js';
import { getProfileReportReasonLabel } from '../../profile/constants/profileReportReasons.js';
import { useAdminToast } from '../hooks/useAdminToast.js';

const STATUS_LABELS = {
  PENDING: 'Chờ xử lý',
  RESOLVED: 'Đã xác nhận vi phạm',
  REJECTED: 'Không vi phạm',
};

export default function AdminProfileReportDetailPage() {
  const { caseId } = useParams();
  const navigate = useNavigate();
  const { showToast } = useAdminToast();
  const [report, setReport] = useState(null);
  const [profile, setProfile] = useState(null);
  const [posts, setPosts] = useState([]);
  const [postPage, setPostPage] = useState(0);
  const [hasMorePosts, setHasMorePosts] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async (signal) => {
    setLoading(true);
    try {
      const detail = await adminApi.getProfileReport(caseId, signal);
      const [currentProfile, postResult] = await Promise.all([
        adminApi.getUser(detail.reportedUserId, signal),
        adminApi.getPosts({ authorId: detail.reportedUserId, page: 0, size: 10 }, signal),
      ]);
      setReport(detail);
      setProfile(currentProfile);
      setPosts(postResult.content || []);
      setPostPage(0);
      setHasMorePosts(!postResult.last);
      setError('');
    } catch (requestError) {
      if (requestError.code !== 'ERR_CANCELED') setError(requestError.message || 'Không thể tải báo cáo trang cá nhân.');
    } finally {
      if (!signal?.aborted) setLoading(false);
    }
  }, [caseId]);

  useEffect(() => {
    const controller = new AbortController();
    const timer = window.setTimeout(() => load(controller.signal), 0);
    return () => {
      window.clearTimeout(timer);
      controller.abort();
    };
  }, [load]);

  async function loadMorePosts() {
    if (!profile || loadingMore || !hasMorePosts) return;
    setLoadingMore(true);
    try {
      const nextPage = postPage + 1;
      const result = await adminApi.getPosts({ authorId: profile.userId, page: nextPage, size: 10 });
      setPosts((current) => [...current, ...(result.content || [])]);
      setPostPage(nextPage);
      setHasMorePosts(!result.last);
    } catch (requestError) {
      setError(requestError.message || 'Không thể tải thêm bài viết.');
    } finally {
      setLoadingMore(false);
    }
  }

  async function processReport(violationConfirmed, blockUser = false) {
    if (submitting) return;
    setSubmitting(true);
    try {
      if (violationConfirmed) {
        await adminApi.resolveProfileReport(
          caseId,
          blockUser
            ? 'Đã xác nhận trang cá nhân vi phạm và khóa tài khoản ngay lập tức.'
            : 'Đã xác nhận trang cá nhân có nội dung vi phạm.',
          blockUser,
        );
      } else {
        await adminApi.rejectProfileReport(caseId, 'Không phát hiện vi phạm sau khi xem xét hồ sơ và bài viết.');
      }
      await load();
      showToast(blockUser
        ? 'Đã xác nhận vi phạm và khóa tài khoản.'
        : violationConfirmed
          ? 'Đã xác nhận trang cá nhân vi phạm.'
          : 'Đã kết luận trang cá nhân không vi phạm.');
    } catch (requestError) {
      setError(requestError.message || 'Không thể xử lý báo cáo.');
      showToast(requestError.message || 'Không thể xử lý báo cáo.', { type: 'error' });
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <LoadingState />;
  if (!report || !profile) return <EmptyState title="Không tìm thấy báo cáo trang cá nhân" description={error || 'Báo cáo không tồn tại.'} />;

  return (
    <section className="mx-auto flex h-[calc(100vh-4rem)] min-h-0 max-w-6xl flex-col overflow-hidden lg:h-[calc(100vh-6rem)]">
      <div className="shrink-0"><Button variant="ghost" onClick={() => navigate('/admin/reports')}><ArrowLeft size={16} /> Quay lại</Button></div>
      {error ? <p className="my-3 rounded-xl bg-red-50 p-3 text-sm text-red-700">{error}</p> : null}
      <div className="mt-3 flex min-h-0 flex-1 flex-col overflow-hidden rounded-[22px] border border-zinc-200 bg-white p-5 shadow-sm">
        <div className="flex shrink-0 flex-wrap items-start justify-between gap-3 border-b pb-4">
          <div>
            <h1 className="text-2xl font-bold">Vụ việc trang cá nhân #{report.caseId}</h1>
            <p className="mt-1 text-sm text-zinc-500">{report.reportCount} người báo cáo · Gần nhất {formatDateTime(report.latestReportedAt)}</p>
          </div>
          <span className="rounded-full bg-zinc-100 px-3 py-1 text-xs font-bold">{STATUS_LABELS[report.status] || report.status}</span>
        </div>

        <div className="grid min-h-0 flex-1 items-start gap-6 overflow-y-auto py-5 lg:grid-cols-[minmax(300px,.75fr)_minmax(0,1.25fr)]">
          <aside>
            <div className="rounded-2xl border p-4">
              <div className="flex items-center gap-3">
                <Avatar src={profile.avatarUrl} name={profile.displayName} size="lg" />
                <div><h2 className="font-bold">{profile.displayName}</h2><p className="text-sm text-zinc-500">User #{profile.userId} · {profile.status}</p></div>
              </div>
              <p className="mt-4 whitespace-pre-wrap text-sm">{profile.bio || 'Chưa có giới thiệu.'}</p>
              <p className="mt-3 text-xs text-zinc-500">Ngày sinh: {profile.dateOfBirth || 'Không có dữ liệu'}</p>
            </div>

            <div className="mt-4 rounded-2xl border border-amber-200 bg-amber-50 p-4">
              <h3 className="font-bold text-amber-900">Snapshot lúc bị báo cáo</h3>
              <p className="mt-2 text-sm font-semibold">{report.snapshot.displayName}</p>
              <p className="mt-1 whitespace-pre-wrap text-sm text-amber-900/80">{report.snapshot.bio || 'Chưa có giới thiệu.'}</p>
              <p className="mt-2 text-xs text-amber-800">Ngày sinh: {report.snapshot.dateOfBirth || 'Không có dữ liệu'}</p>
            </div>

            <div className="mt-4 rounded-2xl border p-4 text-sm">
              <h3 className="font-bold">Người đã báo cáo ({report.reports.length})</h3>
              <div className="mt-3 max-h-56 space-y-3 overflow-y-auto pr-1">
                {report.reports.map((item) => (
                  <div key={item.reportId} className="rounded-xl bg-zinc-50 p-3">
                    <p className="font-semibold">{item.reporterDisplayName} (#{item.reporterId})</p>
                    <p className="mt-1 text-zinc-600">{getProfileReportReasonLabel(item.reason)}</p>
                    <p className="mt-1 text-xs text-zinc-400">{formatDateTime(item.createdAt)}</p>
                  </div>
                ))}
              </div>
              {report.resolutionNote ? <p className="mt-2"><strong>Kết luận:</strong> {report.resolutionNote}</p> : null}
            </div>
          </aside>

          <div>
            <h2 className="pb-3 text-lg font-bold">Bài viết của tài khoản ({posts.length}{hasMorePosts ? '+' : ''})</h2>
            <div className="space-y-3">
              {posts.map((post) => (
                <article key={post.postId} className="cursor-pointer rounded-2xl border p-4 hover:bg-zinc-50" onClick={() => navigate(`/admin/posts/${post.postId}`)}>
                  <div className="flex justify-between gap-3 text-xs text-zinc-500"><span>{post.status}</span><span>{formatDateTime(post.createdAt)}</span></div>
                  <p className="mt-2 whitespace-pre-wrap text-sm">{post.contentPreview || 'Bài viết không có nội dung chữ.'}</p>
                  {post.thumbnailUrl ? <img src={post.thumbnailUrl} alt="Media bài viết" className="mt-3 max-h-64 w-full rounded-xl object-cover" /> : null}
                  <p className="mt-2 text-xs text-zinc-500">{post.likeCount} lượt thích · {post.commentCount} bình luận · {post.pendingReportCount} báo cáo chờ</p>
                </article>
              ))}
              {!posts.length ? <EmptyState title="Tài khoản chưa có bài viết" /> : null}
              {hasMorePosts ? <Button variant="secondary" className="w-full" disabled={loadingMore} onClick={loadMorePosts}>{loadingMore ? 'Đang tải...' : 'Xem thêm bài viết'}</Button> : null}
            </div>
          </div>
        </div>

        {report.status === 'PENDING' ? (
          <div className="flex shrink-0 flex-wrap justify-end gap-3 border-t pt-4">
            <Button variant="secondary" disabled={submitting} onClick={() => processReport(false)}><CircleX size={16} /> Không vi phạm</Button>
            <Button disabled={submitting} onClick={() => processReport(true)}><CheckCircle2 size={16} /> Xác nhận vi phạm</Button>
            <Button className="bg-red-600 text-white hover:bg-red-700" disabled={submitting} onClick={() => processReport(true, true)}><CheckCircle2 size={16} /> Vi phạm & khóa tài khoản</Button>
          </div>
        ) : null}
      </div>
    </section>
  );
}
