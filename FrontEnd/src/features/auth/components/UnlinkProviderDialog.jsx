import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { AUTH_PROVIDER_META } from '../constants/authProviderConstants.js';

export default function UnlinkProviderDialog({ method, open, onClose, onConfirm }) {
  if (!method) return null;
  return (
    <Modal open={open} title="Xác nhận gỡ phương thức" onClose={onClose} footer={(
      <><Button variant="ghost" onClick={onClose}>Hủy</Button><Button variant="danger" onClick={onConfirm}>Tiếp tục xác thực</Button></>
    )}>
      <p className="text-sm leading-6 text-[var(--app-muted)]">Bạn sắp gỡ <strong className="text-[var(--app-text)]">{AUTH_PROVIDER_META[method.type].label}</strong>. Backend sẽ kiểm tra lại đây có phải phương thức đăng nhập cuối cùng hay không.</p>
    </Modal>
  );
}
