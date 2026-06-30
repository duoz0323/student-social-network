import { useNavigate, useParams } from 'react-router-dom';
import Badge from '../../../components/common/Badge.jsx';
import Button from '../../../components/common/Button.jsx';
import { EmptyState } from '../../../components/common/StateBlock.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import { formatDateTime } from '../../../utils/formatters.js';

export default function AdminReportDetailPage() {
  const { reportId } = useParams();
  const navigate = useNavigate();
  const { data, getPostById, getUserById, setReportStatus, setPostStatus } = useApp();
  const report = data.reports.find((item) => item.id === reportId);

  if (!report) return <EmptyState title="Khong tim thay bao cao" description="Bao cao khong ton tai." actionLabel="Quay lai" onAction={() => navigate('/admin/reports')} />;

  const post = getPostById(report.postId, true);
  const reporter = getUserById(report.reporterId);
  const author = post ? getUserById(post.authorId) : null;

  return (
    <section className="max-w-4xl">
      <Button variant="ghost" onClick={() => navigate('/admin/reports')}>Quay lai</Button>
      <div className="mt-4 rounded-2xl border border-zinc-200 bg-white p-5">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-black">Chi tiet bao cao</h1>
          <Badge tone={report.status === 'PENDING' ? 'warning' : report.status === 'RESOLVED' ? 'success' : 'danger'}>{report.status}</Badge>
        </div>
        <dl className="mt-5 grid grid-cols-2 gap-4 text-sm">
          <div><dt className="text-zinc-500">Nguoi bao cao</dt><dd className="font-bold">{reporter?.displayName}</dd></div>
          <div><dt className="text-zinc-500">Thoi gian</dt><dd className="font-bold">{formatDateTime(report.createdAt)}</dd></div>
          <div><dt className="text-zinc-500">Ly do</dt><dd className="font-bold">{report.reason}</dd></div>
          <div><dt className="text-zinc-500">Tac gia bai viet</dt><dd className="font-bold">{author?.displayName ?? 'Khong ro'}</dd></div>
        </dl>
        <p className="mt-5 rounded-2xl bg-zinc-50 p-4 text-sm text-zinc-700">{report.description || 'Khong co mo ta bo sung.'}</p>
        {post ? <p className="mt-4 rounded-2xl border border-zinc-200 p-4 text-zinc-700">{post.content}</p> : null}
        <div className="mt-5 flex gap-2">
          <Button variant="secondary" onClick={() => setReportStatus(report.id, 'REJECTED')}>Tu choi bao cao</Button>
          <Button onClick={() => setReportStatus(report.id, 'RESOLVED')}>Xac nhan hop le</Button>
          {post ? <Button variant="danger" onClick={() => setPostStatus(post.id, 'HIDDEN')}>An bai viet</Button> : null}
        </div>
      </div>
    </section>
  );
}
