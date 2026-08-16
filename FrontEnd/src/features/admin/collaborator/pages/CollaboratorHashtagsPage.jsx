import { useEffect, useState } from 'react';
import { Search } from 'lucide-react';
import { EmptyState, LoadingState } from '../../../../components/common/StateBlock.jsx';
import { collaboratorApi } from '../services/collaboratorApi.js';

export default function CollaboratorHashtagsPage() {
  const [keyword, setKeyword] = useState('');
  const [state, setState] = useState({ data: null, error: '' });

  useEffect(() => {
    const controller = new AbortController();
    collaboratorApi.getHashtags({ keyword, page: 0, size: 50 }, controller.signal)
      .then((data) => setState({ data, error: '' }))
      .catch((error) => !controller.signal.aborted && setState({ data: null, error: error.message }));
    return () => controller.abort();
  }, [keyword]);

  return (
    <section className="flex h-[calc(100vh-4rem)] min-h-0 flex-col gap-5 lg:h-[calc(100vh-6rem)]">
      <header className="shrink-0">
        <h1 className="text-3xl font-bold">Hashtag</h1>
        <p className="mt-2 text-zinc-500">Chỉ xem và tìm kiếm; không có quyền sửa hoặc xóa.</p>
      </header>

      <label className="relative block shrink-0">
        <span className="sr-only">Tìm hashtag</span>
        <Search size={18} className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-zinc-400" />
        <input
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          placeholder="Tìm hashtag"
          className="w-full rounded-xl border border-zinc-300 py-3 pl-11 pr-4 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
        />
      </label>

      {/* Chỉ vùng kết quả nhận scroll; tiêu đề và ô tìm kiếm luôn đứng yên trong viewport. */}
      <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain rounded-2xl border border-zinc-200 bg-zinc-50/50 p-3 pr-2 [scrollbar-gutter:stable]">
        {!state.data && !state.error ? (
          <LoadingState />
        ) : state.error ? (
          <EmptyState title="Không thể tải hashtag" description={state.error} />
        ) : state.data.content.length === 0 ? (
          <EmptyState title="Không tìm thấy hashtag" description="Hãy thử một từ khóa khác." />
        ) : (
          <div className="grid gap-3 pr-1 sm:grid-cols-2">
            {state.data.content.map((hashtag) => (
              <article key={hashtag.hashtagId} className="rounded-xl border border-zinc-200 bg-white p-4 shadow-sm">
                <p className="truncate text-base font-bold text-zinc-950">#{hashtag.name}</p>
                <p className="mt-1.5 text-sm text-zinc-500">
                  {hashtag.postCount} bài viết · dùng gần nhất {formatLastUsedDate(hashtag.lastUsedAt)}
                </p>
              </article>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

function formatLastUsedDate(value) {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleDateString('vi-VN');
}
