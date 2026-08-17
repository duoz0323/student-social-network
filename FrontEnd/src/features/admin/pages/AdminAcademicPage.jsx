import { useEffect, useMemo, useState } from 'react';
import { BookOpen, ChevronRight, GraduationCap, Pencil, Plus, Power, Search } from 'lucide-react';
import { adminApi } from '../../../api/index.js';
import Button from '../../../components/common/Button.jsx';
import DataTable from '../../../components/common/DataTable.jsx';
import { LoadingState } from '../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../utils/formatters.js';
import AdminAcademicFormDialog from '../components/AdminAcademicFormDialog.jsx';
import AdminAcademicStatusDialog from '../components/AdminAcademicStatusDialog.jsx';
import AdminPageHeader from '../components/AdminPageHeader.jsx';
import { useAdminToast } from '../hooks/useAdminToast.js';

const EMPTY_RESULT = Object.freeze({ content: [], totalElements: 0, totalPages: 0 });
const KINDS = Object.freeze({
  school: { key: 'school', label: 'Trường', plural: 'trường' },
  faculty: { key: 'faculty', label: 'Khoa', plural: 'khoa' },
  major: { key: 'major', label: 'Ngành', plural: 'ngành' },
  interest: { key: 'interest', label: 'Sở thích', plural: 'sở thích' },
});

/** Trang quản trị hierarchy School → Faculty → Major và tab Interest Category độc lập. */
export default function AdminAcademicPage() {
  const { showToast } = useAdminToast();
  const [tab, setTab] = useState('academic');
  const [selectedSchool, setSelectedSchool] = useState(null);
  const [selectedFaculty, setSelectedFaculty] = useState(null);
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [result, setResult] = useState(EMPTY_RESULT);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [refreshKey, setRefreshKey] = useState(0);
  const [formOpen, setFormOpen] = useState(false);
  const [editTarget, setEditTarget] = useState(null);
  const [statusTarget, setStatusTarget] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [actionError, setActionError] = useState('');

  const kind = tab === 'interests'
    ? KINDS.interest
    : selectedFaculty ? KINDS.major : selectedSchool ? KINDS.faculty : KINDS.school;

  useEffect(() => {
    const controller = new AbortController();
    const timer = setTimeout(() => {
      setLoading(true);
      const params = { keyword: query.trim(), page: page - 1, size: pageSize };
      let request;
      if (tab === 'interests') request = adminApi.getAcademicInterests(params, controller.signal);
      else if (selectedFaculty) request = adminApi.getAcademicMajors(selectedFaculty.id, params, controller.signal);
      else if (selectedSchool) request = adminApi.getAcademicFaculties(selectedSchool.id, params, controller.signal);
      else request = adminApi.getAcademicSchools(params, controller.signal);
      request
        .then((response) => { setResult(response); setError(''); })
        .catch((requestError) => {
          if (requestError.code !== 'ERR_CANCELED') setError(requestError.message);
        })
        .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    }, 250);
    return () => { clearTimeout(timer); controller.abort(); };
  }, [tab, selectedSchool, selectedFaculty, query, page, pageSize, refreshKey]);

  function resetListState() {
    setQuery('');
    setPage(1);
    setResult(EMPTY_RESULT);
    setError('');
  }

  function selectTab(nextTab) {
    setTab(nextTab);
    setSelectedSchool(null);
    setSelectedFaculty(null);
    resetListState();
  }

  function openCreate() {
    setEditTarget(null);
    setActionError('');
    setFormOpen(true);
  }

  function openEdit(item) {
    setEditTarget(item);
    setActionError('');
    setFormOpen(true);
  }

  async function submitForm(payload) {
    if (submitting) return;
    setSubmitting(true);
    setActionError('');
    try {
      if (kind.key === 'school') {
        if (editTarget) await adminApi.updateAcademicSchool(editTarget.id, payload);
        else await adminApi.createAcademicSchool(payload);
      } else if (kind.key === 'faculty') {
        if (editTarget) await adminApi.updateAcademicFaculty(editTarget.id, payload.name);
        else await adminApi.createAcademicFaculty(selectedSchool.id, payload.name);
      } else if (kind.key === 'major') {
        if (editTarget) await adminApi.updateAcademicMajor(editTarget.id, payload.name);
        else await adminApi.createAcademicMajor(selectedFaculty.id, payload.name);
      } else if (editTarget) await adminApi.updateAcademicInterest(editTarget.id, payload.name);
      else await adminApi.createAcademicInterest(payload.name);
      setFormOpen(false);
      setEditTarget(null);
      setRefreshKey((value) => value + 1);
      showToast(`${editTarget ? 'Cập nhật' : 'Tạo'} ${kind.label.toLowerCase()} thành công.`);
    } catch (requestError) {
      setActionError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function changeStatus(nextStatus) {
    if (!statusTarget || submitting) return;
    setSubmitting(true);
    setActionError('');
    try {
      if (kind.key === 'school') await adminApi.changeAcademicSchoolStatus(statusTarget.id, nextStatus);
      else if (kind.key === 'faculty') await adminApi.changeAcademicFacultyStatus(statusTarget.id, nextStatus);
      else if (kind.key === 'major') await adminApi.changeAcademicMajorStatus(statusTarget.id, nextStatus);
      else await adminApi.changeAcademicInterestStatus(statusTarget.id, nextStatus);
      setStatusTarget(null);
      setRefreshKey((value) => value + 1);
      showToast(`Đã chuyển ${kind.label.toLowerCase()} sang ${nextStatus}.`);
    } catch (requestError) {
      setActionError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  const columns = useMemo(() => {
    const values = [
      {
        key: 'name', label: `Tên ${kind.label.toLowerCase()}`,
        render: (row) => <span className="font-semibold text-zinc-950">{row.name}</span>,
      },
    ];
    if (kind.key === 'school') {
      values.push({ key: 'shortName', label: 'Tên viết tắt', render: (row) => row.shortName || '—' });
    }
    values.push(
      {
        key: 'status', label: 'Trạng thái',
        render: (row) => (
          <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${
            row.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700' : 'bg-zinc-100 text-zinc-600'
          }`}>{row.status}</span>
        ),
      },
      { key: 'updatedAt', label: 'Cập nhật', render: (row) => formatDateTime(row.updatedAt) },
      {
        key: 'actions', label: 'Thao tác', className: 'w-44 text-right',
        render: (row) => (
          <div className="flex justify-end gap-1">
            {kind.key === 'school' || kind.key === 'faculty' ? (
              <button
                type="button"
                onClick={() => {
                  if (kind.key === 'school') { setSelectedSchool(row); setSelectedFaculty(null); }
                  else setSelectedFaculty(row);
                  resetListState();
                }}
                className="inline-flex h-9 items-center gap-1 rounded-lg px-2 text-sm font-medium text-zinc-700 hover:bg-zinc-100"
              >
                Xem {kind.key === 'school' ? 'khoa' : 'ngành'} <ChevronRight size={15} />
              </button>
            ) : null}
            <button type="button" onClick={() => openEdit(row)} aria-label={`Sửa ${row.name}`} title="Chỉnh sửa" className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-zinc-600 hover:bg-zinc-100">
              <Pencil size={16} />
            </button>
            <button type="button" onClick={() => { setActionError(''); setStatusTarget(row); }} aria-label={`Đổi trạng thái ${row.name}`} title="Đổi trạng thái" className={`inline-flex h-9 w-9 items-center justify-center rounded-lg ${row.status === 'ACTIVE' ? 'text-red-600 hover:bg-red-50' : 'text-emerald-700 hover:bg-emerald-50'}`}>
              <Power size={16} />
            </button>
          </div>
        ),
      },
    );
    return values;
  }, [kind]);

  return (
    <section className="flex h-[calc(100vh-4rem)] min-h-0 flex-col lg:h-[calc(100vh-6rem)]">
      <AdminPageHeader
        className="mb-5"
        icon={GraduationCap}
        title="Dữ liệu học thuật"
        description="Quản lý trường, khoa, ngành và danh mục sở thích."
        actions={<Button onClick={openCreate}><Plus size={17} /> Tạo {kind.label.toLowerCase()}</Button>}
      />

      <div className="mb-4 flex shrink-0 gap-2 border-b border-zinc-200">
        {[['academic', 'Trường / Khoa / Ngành', GraduationCap], ['interests', 'Sở thích', BookOpen]].map(([value, label, Icon]) => (
          <button key={value} type="button" onClick={() => selectTab(value)} className={`flex items-center gap-2 border-b-2 px-4 py-3 text-sm font-semibold ${tab === value ? 'border-zinc-950 text-zinc-950' : 'border-transparent text-zinc-500 hover:text-zinc-800'}`}>
            <Icon size={16} /> {label}
          </button>
        ))}
      </div>

      {tab === 'academic' ? (
        <nav className="mb-4 flex shrink-0 items-center gap-2 text-sm" aria-label="Phân cấp dữ liệu học thuật">
          <button type="button" onClick={() => { setSelectedSchool(null); setSelectedFaculty(null); resetListState(); }} className="font-semibold text-zinc-700 hover:text-zinc-950">Trường</button>
          {selectedSchool ? <><ChevronRight size={15} className="text-zinc-400" /><button type="button" onClick={() => { setSelectedFaculty(null); resetListState(); }} className="font-semibold text-zinc-700 hover:text-zinc-950">{selectedSchool.name}</button></> : null}
          {selectedFaculty ? <><ChevronRight size={15} className="text-zinc-400" /><span className="font-semibold text-zinc-950">{selectedFaculty.name}</span></> : null}
        </nav>
      ) : null}

      <div className="mb-4 flex shrink-0 items-center gap-3 rounded-xl border border-zinc-200 bg-white p-3">
        <Search size={17} className="text-zinc-500" />
        <input value={query} onChange={(event) => { setQuery(event.target.value); setPage(1); }} maxLength={100} placeholder={`Tìm ${kind.plural}...`} aria-label={`Tìm ${kind.plural}`} className="min-w-0 flex-1 bg-transparent outline-none" />
        <span className="whitespace-nowrap text-sm text-zinc-500">Tổng: {result.totalElements}</span>
      </div>

      {error ? <div className="mb-4 flex shrink-0 items-center justify-between rounded-xl bg-red-50 p-3 text-sm text-red-700"><span>{error}</span><button type="button" className="font-semibold underline" onClick={() => setRefreshKey((value) => value + 1)}>Thử lại</button></div> : null}
      <div className="min-h-0 flex-1 [&>div]:h-full [&>div]:max-h-none">
        {loading ? <LoadingState /> : (
          <DataTable rows={result.content} emptyText={`Không có ${kind.plural} phù hợp`} columns={columns} pagination={{
            currentPage: page, totalPages: result.totalPages, onPageChange: setPage,
            totalItems: result.totalElements, pageSize,
            onPageSizeChange: (size) => { setPageSize(size); setPage(1); },
          }} />
        )}
      </div>

      {formOpen ? (
        <AdminAcademicFormDialog
          key={`${kind.key}-${editTarget?.id || 'new'}`}
          open
          kind={kind}
          item={editTarget}
          submitting={submitting}
          error={actionError}
          onClose={() => { setFormOpen(false); setEditTarget(null); setActionError(''); }}
          onSubmit={submitForm}
        />
      ) : null}
      <AdminAcademicStatusDialog target={statusTarget} kind={kind} submitting={submitting} error={actionError} onClose={() => { setStatusTarget(null); setActionError(''); }} onConfirm={changeStatus} />
    </section>
  );
}
