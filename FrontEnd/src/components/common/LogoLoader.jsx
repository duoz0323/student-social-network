import useThemeLogo from '../../hooks/useThemeLogo.js';

const SIZES = {
  sm: { frame: 'h-5 w-5', logo: 'h-3 w-3', ring: 'border' },
  md: { frame: 'h-12 w-12', logo: 'h-8 w-8', ring: 'border-2' },
  lg: { frame: 'h-24 w-24', logo: 'h-14 w-14', ring: 'border-2' },
};

/** Loading dùng logo chính thức để mọi màn hình giữ cùng nhận diện UniShare. */
export default function LogoLoader({ message = 'Đang tải...', size = 'md', fullScreen = false, className = '' }) {
  const logo = useThemeLogo();
  const styles = SIZES[size] ?? SIZES.md;
  const loader = (
    <div className={`flex items-center justify-center ${size === 'sm' ? 'gap-2' : 'flex-col gap-3'} ${className}`} role="status" aria-live="polite">
      <span className={`relative flex shrink-0 items-center justify-center ${styles.frame}`} aria-hidden="true">
        <span className={`absolute inset-0 animate-spin rounded-full ${styles.ring} border-transparent border-t-[var(--app-brand)] border-r-violet-500`} />
        <span className="absolute inset-[12%] animate-pulse rounded-full bg-violet-500/10" />
        <span className="absolute inset-[18%] flex items-center justify-center rounded-full overflow-hidden shadow-sm">
          <img src={logo} alt="" className={`brand-logo ${styles.logo} object-contain`} />
        </span>
      </span>
      {message ? <span className={`${size === 'sm' ? 'text-inherit' : 'text-sm font-medium text-[var(--app-muted)]'}`}>{message}</span> : null}
    </div>
  );

  if (!fullScreen) return loader;
  return <main className="flex min-h-screen items-center justify-center bg-[var(--app-bg)] px-6" aria-busy="true">{loader}</main>;
}
