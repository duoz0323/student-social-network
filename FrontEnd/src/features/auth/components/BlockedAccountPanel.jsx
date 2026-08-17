import { ArrowLeft, ArrowUpRight, LockKeyhole } from 'lucide-react';
import { Link } from 'react-router-dom';

// Chỉ ánh xạ reason code public-safe; tuyệt đối không render ghi chú nội bộ hoặc danh tính Admin.
const REASON_LABELS = Object.freeze({
  REPEATED_VIOLATION: 'Tài khoản đã đạt 3 lần vi phạm được xác nhận.',
  SPAM: 'Hoạt động spam.',
  HARASSMENT: 'Quấy rối hoặc xúc phạm người khác.',
  HARMFUL_CONTENT: 'Nội dung có khả năng gây hại.',
  FAKE_ACCOUNT: 'Tài khoản có dấu hiệu giả mạo.',
  PROFILE_VIOLATION: 'Thông tin hồ sơ vi phạm Tiêu chuẩn cộng đồng.',
  OTHER: 'Vi phạm Tiêu chuẩn cộng đồng.',
});

export default function BlockedAccountPanel({ details, onBack }) {
  const reason = REASON_LABELS[details?.reasonCode] || 'Tài khoản vi phạm Tiêu chuẩn cộng đồng.';

  return (
    <section role="alert" aria-labelledby="blocked-account-title" aria-describedby="blocked-account-description">
      {/* Trình bày như một trạng thái của luồng đăng nhập, không mô phỏng hộp thoại cảnh báo. */}
      <div className="flex items-center gap-2 text-sm font-semibold text-red-700">
        <span className="grid h-8 w-8 place-items-center rounded-full bg-red-50" aria-hidden="true">
          <LockKeyhole size={17} strokeWidth={2} />
        </span>
        <span>Truy cập bị hạn chế</span>
      </div>

      <h2
        id="blocked-account-title"
        className="mt-5 text-[1.75rem] font-semibold leading-tight tracking-[-0.02em] text-zinc-950 sm:text-[2rem]"
      >
        Tài khoản của bạn đã bị khóa
      </h2>
      <p id="blocked-account-description" className="mt-3 max-w-md text-sm leading-6 text-zinc-600">
        Bạn chưa thể đăng nhập vào UniShare bằng tài khoản này.
      </p>

      <div className="mt-6 border-l-2 border-red-500 py-1 pl-4">
        <p className="text-xs font-medium text-zinc-500">Lý do</p>
        <p className="mt-1.5 text-sm font-medium leading-6 text-zinc-900">{reason}</p>
      </div>

      <div className="mt-8 border-t border-zinc-200 pt-6">
        <button
          type="button"
          className="inline-flex h-12 w-full items-center justify-center gap-2 rounded-[10px] bg-zinc-950 px-5 text-sm font-semibold text-white transition-colors hover:bg-zinc-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-950 focus-visible:ring-offset-2"
          onClick={onBack}
        >
          <ArrowLeft size={17} aria-hidden="true" />
          Dùng tài khoản khác
        </button>

        <Link
          to="/policies/community-standards"
          className="group mt-4 flex items-center justify-center gap-1.5 text-sm font-medium text-zinc-600 transition-colors hover:text-zinc-950 focus-visible:rounded-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-950 focus-visible:ring-offset-2"
        >
          Xem Tiêu chuẩn cộng đồng
          <ArrowUpRight className="transition-transform group-hover:-translate-y-0.5 group-hover:translate-x-0.5" size={16} aria-hidden="true" />
        </Link>
      </div>
    </section>
  );
}
