import { useEffect, useState } from 'react';
import { ArrowLeft, CheckCircle, EyeOff, ShieldX } from 'lucide-react';
import { useNavigate, useParams } from 'react-router-dom';
import { adminApi } from '../../../api/index.js';
import Button from '../../../components/common/Button.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../utils/formatters.js';

export default function AdminReportDetailPage() {
  const { reportId } = useParams();
  const navigate = useNavigate();
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function load(signal) {
    setLoading(true);
    try { setReport(await adminApi.getReport(reportId, signal)); setError(''); }
    catch (requestError) { if (requestError.code !== 'ERR_CANCELED') setError(requestError.message); }
    finally { setLoading(false); }
  }

  useEffect(() => {
    const controller = new AbortController();
    adminApi.getReport(reportId, controller.signal)
      .then((response) => { setReport(response); setError(''); })
      .catch((requestError) => { if (requestError.code !== 'ERR_CANCELED') setError(requestError.message); })
      .finally(() => setLoading(false));
    return () => controller.abort();
  }, [reportId]);

  async function reject() {
    try { await adminApi.rejectReport(reportId, 'Báo cáo không đủ căn cứ.'); await load(); }
    catch (requestError) { setError(requestError.message); }
  }

  async function resolve(hidePost) {
    try {
      await adminApi.resolveReport(reportId, { resolutionNote: 'Báo cáo hợp lệ.', hidePost, ...(hidePost && { hideReasonCode: 'OTHER' }) });
      await load();
    } catch (requestError) { setError(requestError.message); }
  }

  if (loading) return <LoadingState />;
  if (!report) return <EmptyState title="Không tìm thấy báo cáo" description={error || 'Báo cáo không tồn tại.'} />;
  const post = report.reportedPost;

  return (
    <section className="mx-auto max-w-4xl">
      <Button variant="ghost" onClick={() => navigate('/admin/reports')}><ArrowLeft size={16} /> Quay lại</Button>
      {error && <p className="my-4 rounded-xl bg-red-50 p-3 text-red-700">{error}</p>}
      <div className="mt-5 rounded-xl border bg-white p-6 shadow-sm">
        <div className="flex justify-between"><h1 className="text-2xl font-bold">Chi tiết báo cáo #{report.reportId}</h1><strong>{report.status}</strong></div>
        <dl className="mt-6 grid gap-4 sm:grid-cols-2">
          <div><dt className="text-sm text-gray-500">Người báo cáo</dt><dd>{report.reporter?.displayName}</dd></div>
          <div><dt className="text-sm text-gray-500">Thời gian</dt><dd>{formatDateTime(report.createdAt)}</dd></div>
          <div><dt className="text-sm text-gray-500">Lý do</dt><dd>{report.reason}</dd></div>
          <div><dt className="text-sm text-gray-500">Tác giả</dt><dd>{post?.author?.displayName}</dd></div>
        </dl>
        <h2 className="mt-6 font-bold">Mô tả</h2><p className="mt-2 rounded-lg bg-gray-50 p-4">{report.description || 'Không có mô tả.'}</p>
        <h2 className="mt-6 font-bold">Snapshot nội dung</h2><p className="mt-2 whitespace-pre-wrap rounded-lg border p-4">{report.evidence?.contentSnapshot || post?.currentContent}</p>
        {report.status === 'PENDING' && <div className="mt-6 flex flex-wrap justify-end gap-3 border-t pt-5">
          <Button variant="secondary" onClick={reject}><ShieldX size={16} /> Từ chối</Button>
          <Button variant="secondary" onClick={() => resolve(false)}><CheckCircle size={16} /> Xác nhận</Button>
          <Button onClick={() => resolve(true)}><EyeOff size={16} /> Xác nhận và ẩn bài</Button>
        </div>}
      </div>
    </section>
  );
}
