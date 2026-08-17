import { useEffect } from 'react';
import {
  BookOpen,
  Bookmark,
  CalendarDays,
  Folder,
  Globe,
  Heart,
  Lightbulb,
  MessageCircle,
  UsersRound,
} from 'lucide-react';
import { Link } from 'react-router-dom';
import logo from '../../../assets/brand/logo-dark.jpg';
import studySpaceImg from '../../../assets/images/study_space.jpg';
import avatarMinhAnh from '../../../assets/images/avatar_minh_anh.jpg';
import avatarTraMy from '../../../assets/images/avatar_tra_my.jpg';

const topics = [
  { label: 'Học tập', icon: BookOpen},
  { label: 'Dự án', icon: Folder },
  { label: 'Kinh nghiệm', icon: Lightbulb },
  { label: 'Sự kiện', icon: CalendarDays },
];

/** Composition bài viết mô phỏng sinh động truyền cảm hứng học tập theo mẫu thiết kế chuẩn. */
function SocialPreview() {
  return (
    <div className="relative mt-5 h-[325px] w-full max-w-[600px] xl:h-[350px] 2xl:h-[375px]" aria-hidden="true">
      <svg className="absolute inset-0 h-full w-full text-zinc-300" viewBox="0 0 600 345">
        <path className="auth-preview-path" d="M18 62C86 111 97 42 165 91S272 146 322 75S445 50 540 113" fill="none" stroke="currentColor" strokeWidth="1.5" strokeDasharray="4 6" />
        <path className="auth-preview-path auth-preview-path--reverse" d="M5 244C60 191 105 210 157 250S287 305 354 258S467 204 583 248" fill="none" stroke="currentColor" strokeWidth="1.5" strokeDasharray="4 6" />
        <circle cx="19" cy="62" r="4" fill="currentColor" />
        <circle cx="583" cy="248" r="4" fill="currentColor" />
      </svg>

      {/* Bài viết chính – Minh Anh */}
      <div className="auth-preview-card absolute left-6 top-6 w-[64%] rounded-[20px] border p-4 xl:left-8" style={{ '--auth-card-rotation': '-1deg', background: 'var(--auth-card-bg)', borderColor: 'var(--auth-card-border)', boxShadow: 'var(--auth-card-shadow)' }}>
        <div className="flex items-center gap-3">
          <img src={avatarMinhAnh} alt="" className="h-9 w-9 rounded-full object-cover shadow-xs" />
          <div className="min-w-0 flex-1">
            <p className="text-[12px] font-bold text-zinc-900">Minh Anh</p>
            <p className="flex items-center gap-1 text-[10px] text-zinc-400">
              2 giờ · <Globe size={11} className="inline text-zinc-400" />
            </p>
          </div>
        </div>
        <p className="mt-2.5 text-[12px] leading-5 text-zinc-700">Góc học tập chill cuối tuần 📚</p>
        <p className="mb-2.5 text-[11px] font-semibold text-zinc-950">#studywithme</p>
        <img src={studySpaceImg} alt="" className="h-[135px] w-full rounded-xl object-cover shadow-xs xl:h-[145px]" />
        <div className="mt-3 flex items-center gap-5 text-[11px] font-medium text-zinc-400">
          <span className="inline-flex items-center gap-1.5"><Heart size={15} />128</span>
          <span className="inline-flex items-center gap-1.5"><MessageCircle size={15} />23 bình luận</span>
          <Bookmark className="ml-auto text-zinc-950" size={16} />
        </div>
      </div>


      {/* Bài viết phụ 2 – Trà My */}
      <div className="auth-preview-card auth-preview-card--delay-two absolute right-0 top-[175px] w-[34%] rounded-[16px] border p-3.5" style={{ '--auth-card-rotation': '1deg', background: 'var(--auth-card-bg)', borderColor: 'var(--auth-card-border)', boxShadow: 'var(--auth-card-shadow)' }}>
        <div className="mb-2 flex items-center gap-2">
          <img src={avatarTraMy} alt="" className="h-7 w-7 rounded-full object-cover" />
          <div>
            <p className="text-[11px] font-bold text-zinc-800">Trà My</p>
            <p className="text-[9px] text-zinc-400">1 ngày</p>
          </div>
        </div>
        <p className="text-[10px] font-medium leading-4 text-zinc-700">Quản lý ghi chú bằng cách hiệu quả hơn?</p>
        <p className="mt-1 text-[10px] font-semibold text-zinc-950">#studyhack</p>
        <div className="mt-3 flex gap-3 text-[10px] text-zinc-400"><span className="flex items-center gap-1"><Heart size={13} />42</span><span className="flex items-center gap-1"><MessageCircle size={13} />12</span></div>
      </div>

      {/* Badges trang trí nổi */}
      <div className="auth-preview-badge absolute right-[26%] top-0 grid h-11 w-11 place-items-center rounded-full bg-zinc-950 text-white shadow-md">
        <UsersRound size={19} />
      </div>
      <div className="auth-preview-badge auth-preview-badge--heart absolute right-[-2px] top-[265px] grid h-10 w-10 place-items-center rounded-full border border-zinc-200 text-pink-500 shadow-md xl:top-[275px]" style={{ background: 'var(--auth-card-bg)' }}>
        <Heart fill="currentColor" size={17} />
      </div>
    </div>
  );
}

/** Layout Auth duy nhất, dùng hero đầy đủ trên desktop và ưu tiên form trên mobile. */
export default function AuthEntryLayout({ children, title = 'Đăng nhập' }) {
  useEffect(() => {
    document.title = `${title} • UniShare`;
  }, [title]);

  return (
    <main className="auth-theme relative min-h-dvh overflow-x-hidden font-sans text-zinc-950 lg:h-dvh lg:overflow-hidden" style={{ background: 'var(--auth-bg)' }}>
      {/* Lớp nền uốn cong duy nhất trải dài toàn bộ trang – không có bất kỳ ranh giới cột nào cắt ngang */}
      <div className="pointer-events-none absolute inset-0 hidden overflow-hidden lg:block" aria-hidden="true">
        <div className="auth-hero-oval auth-hero-oval--outer absolute -left-[14%] -top-[20%] h-[140%] w-[68%] rounded-[50%] blur-[2px]" style={{ background: 'var(--auth-hero-oval)' }} />
        <div className="auth-hero-oval absolute -left-[18%] -top-[16%] h-[132%] w-[64%] rounded-[50%]" style={{ background: 'var(--auth-hero-gradient)', boxShadow: 'var(--auth-hero-shadow)' }} />
        <div className="absolute -left-16 bottom-[-90px] h-64 w-64 rounded-full [background-size:14px_14px]" style={{ background: 'radial-gradient(circle, var(--auth-dot-color) 1.2px, transparent 1.4px)', opacity: 'var(--auth-dot-opacity)' }} />
      </div>

      <div className="relative z-10 min-h-dvh lg:grid lg:h-dvh lg:grid-cols-[minmax(0,54fr)_minmax(450px,46fr)]">
        {/* Khối Hero thông điệp chính bên trái */}
        <section className="relative hidden h-dvh bg-transparent px-10 py-7 lg:flex lg:flex-col xl:px-12 xl:py-8 2xl:px-16">
          {/* Logo ứng dụng */}
          <Link to="/" className="relative z-10 inline-flex w-fit items-center gap-3.5" aria-label="UniShare - Trang chủ">
            <img src={logo} alt="" className="h-11 w-11 rounded-xl object-contain shadow-xs xl:h-12 xl:w-12" />
            <span className="text-2xl font-bold tracking-[-0.03em] text-zinc-950 xl:text-[26px]">UniShare</span>
          </Link>

          {/* Khối Hero thông điệp chính */}
          <div className="auth-hero-copy relative z-10 mx-auto mt-6 w-full max-w-[650px] pr-10 xl:mt-8 xl:pr-6 2xl:mt-10">
            <h1 className="max-w-[620px] text-[2.3rem] font-semibold leading-[1.15] tracking-[-0.02em] text-zinc-950 xl:text-[2.85rem] 2xl:text-[3.15rem]">
              Kết nối, học hỏi.<br />
              Phát triển <span className="italic font-bold text-zinc-950 tracking-tight">cùng nhau.</span>
            </h1>
            <p className="mt-3.5 max-w-[520px] text-[14px] leading-6 text-zinc-500 xl:text-[15px]">
              UniShare là mạng xã hội dành riêng cho sinh viên.<br className="hidden 2xl:block" /> Nơi bạn học hỏi, chia sẻ và kết nối mỗi ngày.
            </p>

            {/* Thẻ chủ đề */}
            <div className="mt-4 flex flex-wrap gap-2.5">
              {topics.map(({ label, icon: Icon, active }) => (
                <span
                  key={label}
                  className={`auth-topic-chip inline-flex items-center gap-2 rounded-full border px-4 py-2 text-xs font-medium ${active ? 'border-zinc-900 text-zinc-950 font-semibold' : 'border-zinc-200 text-zinc-600'}`}
                  style={{ background: 'var(--auth-topic-bg)', boxShadow: 'var(--auth-topic-shadow)' }}
                >
                  <Icon size={15} />
                  {label}
                </span>
              ))}
            </div>

            {/* Thẻ xem trước tương tác mạng xã hội */}
            <SocialPreview />
          </div>
        </section>

        {/* Khối form nhập liệu bên phải */}
        <section className="relative z-10 flex min-h-dvh items-center bg-transparent px-5 py-6 sm:px-8 sm:py-8 lg:h-dvh lg:min-h-0 lg:overflow-y-auto lg:px-10 lg:py-8 xl:px-14 2xl:px-20">
          <div className="auth-form-shell mx-auto w-full max-w-[500px]">
            <Link to="/" className="mb-4 inline-flex items-center gap-3 sm:mb-6 lg:hidden" aria-label="UniShare - Trang chủ">
              <img src={logo} alt="" className="h-10 w-10 rounded-xl object-contain shadow-xs" />
              <span className="text-2xl font-bold tracking-[-0.03em] text-zinc-950">UniShare</span>
            </Link>
            {children}
          </div>
        </section>
      </div>
    </main>
  );
}

