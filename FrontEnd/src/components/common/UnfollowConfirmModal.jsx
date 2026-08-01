import Modal from './Modal.jsx';
import Avatar from './Avatar.jsx';

export default function UnfollowConfirmModal({
  open, user, busy = false, onClose, onConfirm,
}) {
  if (!user) return null;

  return (
    <Modal
      open={open}
      onClose={onClose}
      size="sm"
      className="!max-w-[320px] !rounded-[16px] !overflow-hidden"
      hideCloseButton={true}
      customHeader={<div className="hidden"></div>}
      footer={
        <div className="flex w-full border-t border-[var(--app-border)]">
          <button
            className="flex-1 py-3.5 text-[15px] text-[var(--app-text)] font-normal border-r border-[var(--app-border)] hover:bg-[var(--app-surface-soft)] transition rounded-bl-[16px]"
            disabled={busy}
            onClick={onClose}
          >
            Hủy
          </button>
          <button
            className="flex-1 py-3.5 text-[15px] font-bold text-red-500 hover:bg-red-500/10 transition rounded-br-[16px]"
            disabled={busy}
            onClick={onConfirm}
          >
            {busy ? 'Đang xử lý...' : 'Bỏ theo dõi'}
          </button>
        </div>
      }
      footerClassName="!border-none !p-0"
      bodyClassName="!p-0"
    >
      <div className="flex flex-col items-center pt-8 pb-6 px-4 text-center">
        <Avatar
          src={user.avatarUrl}
          name={user.displayName}
          size="lg"
          className="!w-20 !h-20 mb-5"
        />
        <h3 className="text-[15px] text-[var(--app-text)] px-2 leading-relaxed font-normal">
          Bỏ theo dõi {user.displayName}?
        </h3>
      </div>
    </Modal>
  );
}
