// Component vỏ chứa nội dung chuẩn (có nắp bo tròn dính ở trên) giống phong cách Threads.
// Sử dụng để bọc nội dung của các trang như Feed, Profile, Search.
export default function ContentShell({ header, children, wide = false }) {
  return (
    <>
      {/* Phần Sticky Header: Gồm Tabs và Mặt nạ bo tròn */}
      <div className={`sticky top-0 z-20 w-full ${wide ? 'max-w-[1120px]' : 'max-w-[var(--feed-width)]'}`}>
        
        {/* Vùng xám bao phủ Tabs. Khi bài viết lướt qua mặt nạ, nó sẽ chui vào dưới vùng xám này và bị che đi hoàn toàn. */}
        <div className="bg-[var(--app-bg)] pt-2 lg:pt-6">
          {/* Header phải nằm trên nắp bo tròn để các dropdown nổi không bị đường viền xuyên qua. */}
          {header && <div className="relative z-20 mb-0">{header}</div>}
        </div>

        {/* Nắp bo tròn ảo (Corner Mask). Vùng giữa HOÀN TOÀN TRONG SUỐT để bài viết hiển thị khi lướt qua. */}
        <div className="hidden lg:block relative h-6 w-full pointer-events-none z-10">
          <div className="absolute top-0 left-6 right-6 h-[1px] bg-[var(--app-border-strong)]"></div>
          
          <svg className="absolute top-0 left-0 w-6 h-6 overflow-visible" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M 0 24 L 0 0 L 24 0 A 24 24 0 0 0 0 24 Z" fill="var(--app-bg)" />
            <path d="M 0 24 A 24 24 0 0 1 24 0" stroke="var(--app-border-strong)" strokeWidth="1" />
          </svg>

          <svg className="absolute top-0 right-0 w-6 h-6 overflow-visible" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M 24 24 L 24 0 L 0 0 A 24 24 0 0 1 24 24 Z" fill="var(--app-bg)" />
            <path d="M 0 0 A 24 24 0 0 1 24 24" stroke="var(--app-border-strong)" strokeWidth="1" />
          </svg>
        </div>
      </div>

      {/* Vỏ chứa bài viết. Bị kéo lên 24px để lót dưới mặt nạ. Nền trắng nguyên khối. */}
      <section className={`content-shell ${wide ? 'content-shell--wide' : ''} bg-[var(--app-surface)] lg:-mt-6 relative z-0`}>
        {children}
      </section>
    </>
  );
}
