import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, User, Calendar, MessageSquare, AlertTriangle, ShieldX, CheckCircle, EyeOff, FileText } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import { EmptyState } from '../../../components/common/StateBlock.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import { formatDateTime } from '../../../utils/formatters.js';
import useKeyboardShortcut from '../../../hooks/useKeyboardShortcut.js';

export default function AdminReportDetailPage() {
  const { reportId } = useParams();
  const navigate = useNavigate();
  const { data, getPostById, getUserById, setReportStatus, setPostStatus } = useApp();
  const report = data.reports.find((item) => item.id === reportId);
  const post = report ? getPostById(report.postId, true) : null;
  const reporter = report ? getUserById(report.reporterId) : null;
  const author = post ? getUserById(post.authorId) : null;

  // Keyboard shortcuts
  useKeyboardShortcut('Escape', () => navigate('/admin/reports'));
  useKeyboardShortcut('r', () => report && setReportStatus(report.id, 'REJECTED'));
  useKeyboardShortcut('a', () => report && setReportStatus(report.id, 'RESOLVED'));
  useKeyboardShortcut('h', () => {
    if (post && post.status !== 'HIDDEN') {
      setPostStatus(post.id, 'HIDDEN');
    }
  });

  if (!report) return <EmptyState title="Không tìm thấy báo cáo" description="Báo cáo không tồn tại hoặc đã bị xóa." actionLabel="Quay lại danh sách" onAction={() => navigate('/admin/reports')} />;

  return (
    <section className="max-w-4xl mx-auto animate-slide-up">
      {/* Back button */}
      <Button variant="ghost" onClick={() => navigate('/admin/reports')} className="mb-6 -ml-3" title="Phím tắt: Esc">
        <ArrowLeft size={16} /> Quay lại danh sách
      </Button>

      <div className="rounded-xl border border-gray-100 bg-white shadow-sm overflow-hidden">
        {/* Header */}
        <div className="border-b border-gray-100 p-6 sm:p-8 bg-gray-50/50">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
                Chi tiết báo cáo
              </h1>
              <p className="text-sm font-medium text-gray-500 mt-1 uppercase tracking-wider">ID: {report.id}</p>
            </div>
            <span className={`inline-flex items-center gap-1.5 text-xs font-medium px-3 py-1.5 rounded-lg ${report.status === 'PENDING' ? 'bg-orange-50 text-orange-700 border border-orange-100' : report.status === 'RESOLVED' ? 'bg-green-50 text-green-700 border border-green-100' : 'bg-gray-100 text-gray-700'}`}>
              <span className={`w-1.5 h-1.5 rounded-full ${report.status === 'PENDING' ? 'bg-orange-500' : report.status === 'RESOLVED' ? 'bg-green-500' : 'bg-gray-400'}`}></span>
              {report.status === 'PENDING' ? 'Chờ xử lý' : report.status === 'RESOLVED' ? 'Đã duyệt' : 'Từ chối'}
            </span>
          </div>
        </div>

        <div className="p-6 sm:p-8">
          {/* Info Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
            <div className="flex items-start gap-3 p-4 rounded-xl border border-gray-100 bg-gray-50/50">
              <div className="p-2 bg-white rounded-lg text-gray-500 shadow-sm"><User size={18} /></div>
              <div>
                <p className="text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">Người báo cáo</p>
                <p className="font-semibold text-gray-900 text-sm">{reporter?.displayName}</p>
              </div>
            </div>
            
            <div className="flex items-start gap-3 p-4 rounded-xl border border-gray-100 bg-gray-50/50">
              <div className="p-2 bg-white rounded-lg text-gray-500 shadow-sm"><Calendar size={18} /></div>
              <div>
                <p className="text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">Thời gian</p>
                <p className="font-semibold text-gray-900 text-sm">{formatDateTime(report.createdAt)}</p>
              </div>
            </div>

            <div className="flex items-start gap-3 p-4 rounded-xl border border-orange-100 bg-orange-50/50">
              <div className="p-2 bg-white rounded-lg text-orange-600 shadow-sm"><AlertTriangle size={18} /></div>
              <div>
                <p className="text-[11px] font-semibold text-orange-700 uppercase tracking-wider mb-1">Lý do báo cáo</p>
                <p className="font-bold text-gray-900">{report.reason}</p>
              </div>
            </div>

            <div className="flex items-start gap-3 p-4 rounded-xl border border-gray-100 bg-gray-50/50">
              <div className="p-2 bg-white rounded-lg text-gray-500 shadow-sm"><User size={18} /></div>
              <div>
                <p className="text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">Tác giả bài viết</p>
                <p className="font-semibold text-gray-900 text-sm">{author?.displayName ?? 'Không rõ'}</p>
              </div>
            </div>
          </div>

          {/* Report Description */}
          <div className="mb-8">
            <h3 className="text-xs uppercase tracking-wider font-semibold text-gray-900 flex items-center gap-2 mb-3">
              <MessageSquare size={14} className="text-gray-500" /> Mô tả bổ sung
            </h3>
            <div className="rounded-xl bg-gray-50/50 border border-gray-100 p-4 text-sm text-gray-700 leading-relaxed">
              {report.description || <span className="italic text-gray-400">Không có mô tả bổ sung.</span>}
            </div>
          </div>

          {/* Post Preview */}
          {post && (
            <div className="animate-slide-up-delayed-1">
              <h3 className="text-xs uppercase tracking-wider font-semibold text-gray-900 flex items-center gap-2 mb-3">
                <FileText size={14} className="text-gray-500" /> Nội dung bài viết bị báo cáo
              </h3>
              <div className="rounded-xl border-l-4 border-l-gray-300 bg-white border border-gray-100 p-5 shadow-sm relative">
                {post.status === 'HIDDEN' && (
                  <div className="absolute top-3 right-3 text-[10px] font-semibold uppercase tracking-wider bg-gray-100 text-gray-600 px-2 py-0.5 rounded">
                    Đã ẩn
                  </div>
                )}
                <p className="text-gray-800 leading-relaxed whitespace-pre-wrap">{post.content}</p>
              </div>
            </div>
          )}
        </div>

        {/* Action Buttons */}
        <div className="border-t border-gray-100 p-6 sm:p-8 bg-gray-50/50 flex flex-wrap gap-3 items-center justify-end animate-slide-up-delayed-2">
          <Button variant="secondary" onClick={() => setReportStatus(report.id, 'REJECTED')} title="Phím tắt: R">
            <ShieldX size={16} /> Từ chối
          </Button>
          <Button onClick={() => setReportStatus(report.id, 'RESOLVED')} title="Phím tắt: A">
            <CheckCircle size={16} /> Xác nhận hợp lệ
          </Button>
          {post && post.status !== 'HIDDEN' && (
            <div className="w-full sm:w-auto mt-4 sm:mt-0 sm:pl-4 sm:border-l sm:border-gray-200">
              <Button variant="dangerSoft" onClick={() => setPostStatus(post.id, 'HIDDEN')} className="w-full" title="Phím tắt: H">
                <EyeOff size={16} /> Ẩn bài viết
              </Button>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
