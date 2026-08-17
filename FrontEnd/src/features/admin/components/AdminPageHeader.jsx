/**
 * Chuẩn hóa phần mở đầu của các trang quản trị để tiêu đề, mô tả và thao tác
 * luôn có cùng bố cục trên mọi vai trò và kích thước màn hình.
 */
export default function AdminPageHeader({ icon: Icon, title, description, actions, className = '' }) {
  return (
    <header className={`shrink-0 ${className}`}>
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
        <div className="flex min-w-0 flex-1 items-center gap-3">
          {Icon ? (
            <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-zinc-100 text-zinc-700">
              <Icon size={22} aria-hidden="true" />
            </span>
          ) : null}
          <div className="min-w-0">
            <h1 className="text-2xl font-bold text-zinc-950">{title}</h1>
            {description ? <p className="mt-1 text-sm text-zinc-500">{description}</p> : null}
          </div>
        </div>
        {actions ? <div className="flex shrink-0 flex-wrap items-center gap-2">{actions}</div> : null}
      </div>
    </header>
  );
}
