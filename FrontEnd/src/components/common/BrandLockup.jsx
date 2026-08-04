import useThemeLogo from '../../hooks/useThemeLogo.js';

/**
 * Cụm nhận diện thương hiệu UniShare – tự động chuyển logo theo theme sáng/tối.
 */
export default function BrandLockup({ compact = false, className = '' }) {
  const logo = useThemeLogo();
  return (
    <span className={`brand-lockup ${compact ? 'brand-lockup--compact' : ''} ${className}`}>
      <span className="brand-mark" aria-hidden="true">
        <img src={logo} alt="" className="brand-mark__image" />
      </span>

      <span className="min-w-0">
        <span className="brand-wordmark">UniShare</span>
      </span>
    </span>
  );
}
