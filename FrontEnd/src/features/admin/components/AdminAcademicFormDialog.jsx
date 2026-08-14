import { useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';

/** Form dùng chung cho create/edit master data; School có thêm shortName tùy chọn. */
export default function AdminAcademicFormDialog({ open, kind, item, submitting, error, onClose, onSubmit }) {
  const [name, setName] = useState(item?.name || '');
  const [shortName, setShortName] = useState(item?.shortName || '');

  function handleSubmit(event) {
    event.preventDefault();
    if (submitting) return;
    onSubmit({ name, ...(kind.key === 'school' ? { shortName } : {}) });
  }

  const formId = `admin-academic-${kind.key}-form`;
  return (
    <Modal
      open={open}
      title={`${item ? 'Chỉnh sửa' : 'Tạo'} ${kind.label.toLowerCase()}`}
      size="sm"
      onClose={() => { if (!submitting) onClose(); }}
      footer={(
        <>
          <Button variant="secondary" disabled={submitting} onClick={onClose}>Hủy</Button>
          <Button type="submit" form={formId} loading={submitting} loadingLabel="Đang lưu...">
            {item ? 'Lưu thay đổi' : `Tạo ${kind.label.toLowerCase()}`}
          </Button>
        </>
      )}
    >
      <form id={formId} onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor={`${formId}-name`} className="mb-2 block text-sm font-semibold text-zinc-800">
            Tên {kind.label.toLowerCase()}
          </label>
          <input
            id={`${formId}-name`}
            value={name}
            onChange={(event) => setName(event.target.value)}
            maxLength={kind.key === 'interest' ? 100 : 255}
            autoFocus
            placeholder={`Nhập tên ${kind.label.toLowerCase()}`}
            className="w-full rounded-xl border border-zinc-300 px-3 py-3 outline-none focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
          />
        </div>
        {kind.key === 'school' ? (
          <div>
            <label htmlFor={`${formId}-short-name`} className="mb-2 block text-sm font-semibold text-zinc-800">
              Tên viết tắt <span className="font-normal text-zinc-500">(tùy chọn)</span>
            </label>
            <input
              id={`${formId}-short-name`}
              value={shortName}
              onChange={(event) => setShortName(event.target.value)}
              maxLength={50}
              placeholder="Ví dụ: STU"
              className="w-full rounded-xl border border-zinc-300 px-3 py-3 outline-none focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
            />
          </div>
        ) : null}
        <p className="text-xs leading-5 text-zinc-500">Khoảng trắng sẽ được chuẩn hóa; tên trùng theo phạm vi nghiệp vụ sẽ bị từ chối.</p>
        {error ? <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p> : null}
      </form>
    </Modal>
  );
}
