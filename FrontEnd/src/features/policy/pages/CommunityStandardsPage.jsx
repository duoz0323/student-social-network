import { ArrowLeft, ShieldCheck } from 'lucide-react';
import { Link } from 'react-router-dom';

// Nội dung chính sách sử dụng nội bộ, không được diễn giải thành điều khoản pháp lý đầy đủ.
const standards = [
  'Spam và hành vi gây nhiễu cộng đồng',
  'Quấy rối, xúc phạm hoặc đe dọa người khác',
  'Nội dung không phù hợp với môi trường sinh viên',
  'Giả mạo cá nhân hoặc tổ chức',
  'Lừa đảo, gian lận hoặc thao túng người dùng',
  'Thông tin sai sự thật có khả năng gây hại',
  'Bạo lực hoặc tổ chức nguy hiểm',
  'Các hành vi khác vi phạm tiêu chuẩn của hệ thống',
];

export default function CommunityStandardsPage() {
  return (
    <main className="min-h-dvh bg-[var(--app-bg)] px-4 py-8 text-[var(--app-text)] sm:px-6 sm:py-12">
      <article className="mx-auto max-w-3xl rounded-3xl border border-[var(--app-border)] bg-[var(--app-surface)] p-6 shadow-sm sm:p-10">
        <Link to="/" className="inline-flex items-center gap-2 text-sm font-semibold text-[var(--app-muted)] hover:text-[var(--app-text)]">
          <ArrowLeft size={17} /> Quay lại UniShare
        </Link>
        <header className="mt-7 flex items-start gap-4">
          <span className="rounded-2xl bg-[var(--app-brand)]/10 p-3 text-[var(--app-brand)]"><ShieldCheck size={30} /></span>
          <div>
            <h1 className="text-3xl font-black tracking-tight">Tiêu chuẩn cộng đồng</h1>
            <p className="mt-2 leading-7 text-[var(--app-muted)]">
              Đây là chính sách sử dụng nội bộ của Student Social Network, giúp cộng đồng sinh viên chia sẻ và kết nối an toàn. Nội dung này không phải bộ điều khoản pháp lý đầy đủ.
            </p>
          </div>
        </header>

        <section className="mt-9">
          <h2 className="text-xl font-extrabold">Nội dung và hành vi có thể bị xử lý</h2>
          <ul className="mt-4 grid gap-3 sm:grid-cols-2">
            {standards.map((item) => <li key={item} className="rounded-xl bg-[var(--app-surface-soft)] p-4 text-sm leading-6">{item}</li>)}
          </ul>
        </section>

        <section className="mt-9">
          <h2 className="text-xl font-extrabold">Cách hệ thống xử lý vi phạm</h2>
          <div className="mt-4 grid gap-3 sm:grid-cols-3">
            <div className="rounded-xl border border-[var(--app-border)] p-4"><strong>Lần 1</strong><p className="mt-2 text-sm text-[var(--app-muted)]">Cảnh báo người dùng.</p></div>
            <div className="rounded-xl border border-amber-500/30 p-4"><strong>Lần 2</strong><p className="mt-2 text-sm text-[var(--app-muted)]">Cảnh báo nghiêm trọng, lần cuối.</p></div>
            <div className="rounded-xl border border-red-500/30 p-4"><strong>Lần 3</strong><p className="mt-2 text-sm text-[var(--app-muted)]">Tự động khóa vì vi phạm lặp lại.</p></div>
          </div>
          <p className="mt-5 rounded-xl bg-[var(--app-surface-soft)] p-4 text-sm leading-6">
            Một bài bị nhiều người báo cáo không đồng nghĩa có nhiều lần vi phạm. Chỉ khi quản trị viên xác nhận một Moderation Case có vi phạm thì mới được tính một lần.
          </p>
        </section>
      </article>
    </main>
  );
}
