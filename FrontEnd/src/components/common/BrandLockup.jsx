import logo from '../../assets/brand/logo-transparent.png';

/**
 * Cụm nhận diện sử dụng logo gốc đã loại khoảng trắng thừa, không biến đổi hình học hoặc màu sắc.
 */
export default function BrandLockup({ compact = false, className = '' }) {
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
