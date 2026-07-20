import { Link } from 'react-router-dom';
import { socialConflictService } from '../services/socialConflictService.js';

export default function SocialConflictPendingPage() {
  const conflict = socialConflictService.get();
  return (
    <main className="auth-pattern flex min-h-screen items-center justify-center px-4 py-8">
      <section className="stitch-card-shadow w-full max-w-[430px] rounded-[10px] border border-[var(--app-border)] bg-white p-8 text-center">
        <h1 className="text-xl font-black">Cần xác nhận lựa chọn tài khoản</h1>
        <p className="mt-3 text-sm leading-6 text-zinc-600">
          {conflict ? 'Backend yêu cầu bạn lựa chọn cách tiếp tục trước khi hoàn tất đăng nhập Google.' : 'Phiên xử lý xung đột không còn khả dụng.'}
        </p>
        <p className="mt-3 text-xs text-zinc-500">Giao diện lựa chọn sẽ được hoàn thiện ở Giai đoạn 13G; hệ thống chưa tự gộp hoặc hủy đăng ký của bạn.</p>
        <Link to="/register" className="mt-6 inline-block font-black text-zinc-900">Quay lại đăng ký</Link>
      </section>
    </main>
  );
}
