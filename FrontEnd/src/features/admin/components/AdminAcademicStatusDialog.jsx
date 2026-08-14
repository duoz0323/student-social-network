import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';

/** Xác nhận đổi trạng thái; thao tác chỉ đổi target và không cascade xuống dữ liệu con. */
export default function AdminAcademicStatusDialog({ target, kind, submitting, error, onClose, onConfirm }) {
  const nextStatus = target?.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
  const deactivating = nextStatus === 'INACTIVE';
  return (
    <Modal
      open={Boolean(target)}
      title={deactivating ? `Ngừng sử dụng ${kind.label.toLowerCase()}` : `Kích hoạt ${kind.label.toLowerCase()}`}
      size="sm"
      onClose={() => { if (!submitting) onClose(); }}
      footer={(
        <>
          <Button variant="secondary" disabled={submitting} onClick={onClose}>Hủy</Button>
          <Button
            variant={deactivating ? 'danger' : 'primary'}
            loading={submitting}
            loadingLabel="Đang cập nhật..."
            onClick={() => onConfirm(nextStatus)}
          >
            {deactivating ? 'Chuyển INACTIVE' : 'Chuyển ACTIVE'}
          </Button>
        </>
      )}
    >
      <p className="text-sm leading-6 text-zinc-700">
        Xác nhận chuyển <strong>{target?.name}</strong> sang <strong>{nextStatus}</strong>.
        {deactivating
          ? ' Dữ liệu đã được người dùng chọn vẫn được bảo toàn; các lựa chọn mới sẽ tuân theo active hierarchy.'
          : ' Bản ghi sẽ xuất hiện trở lại khi chính nó và toàn bộ parent đều ACTIVE.'}
      </p>
      {error ? <p className="mt-3 rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p> : null}
    </Modal>
  );
}
