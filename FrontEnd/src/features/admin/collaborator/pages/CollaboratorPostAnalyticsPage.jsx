import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { EmptyState, LoadingState } from '../../../../components/common/StateBlock.jsx';
import { collaboratorApi } from '../services/collaboratorApi.js';

export default function CollaboratorPostAnalyticsPage() {
  const { postId } = useParams(); const [range, setRange] = useState('7D'); const [state, setState] = useState({ data: null, error: '' });
  useEffect(() => { const controller = new AbortController(); collaboratorApi.getAnalytics(postId, range, controller.signal).then((data) => setState({ data, error: '' })).catch((e) => !controller.signal.aborted && setState({ data: null, error: e.message })); return () => controller.abort(); }, [postId, range]);
  if (!state.data && !state.error) return <LoadingState />; if (state.error) return <EmptyState title="Không thể tải thống kê" description={state.error} />;
  return <section className="space-y-6"><header className="flex justify-between gap-4"><div><h1 className="text-3xl font-bold">Thống kê bài viết</h1><p className="mt-2 text-zinc-500">{state.data.post.contentPreview || `Bài #${postId}`}</p></div><select value={range} onChange={(e) => setRange(e.target.value)} className="h-11 rounded-xl border px-4"><option value="24H">24 giờ</option><option value="7D">7 ngày</option><option value="30D">30 ngày</option></select></header>
    <div className="grid gap-4 sm:grid-cols-4">{[['Lượt thích', state.data.likeCount], ['Bình luận', state.data.commentCount], ['Đăng lại', state.data.repostCount], ['Tổng tương tác', state.data.totalInteractions]].map(([label, value]) => <div key={label} className="rounded-2xl border p-5"><p className="text-sm text-zinc-500">{label}</p><p className="mt-2 text-3xl font-bold">{value}</p></div>)}</div>
    <div className="rounded-2xl border p-5"><h2 className="font-bold">Xu hướng chính xác theo ngày</h2>{state.data.interactionTrend.length ? <div className="mt-4 space-y-2">{state.data.interactionTrend.map((p) => <div key={p.date} className="flex justify-between border-b py-2 text-sm"><span>{p.date}</span><span>{p.likes} thích · {p.comments} bình luận · {p.reposts} đăng lại</span></div>)}</div> : <p className="mt-5 text-zinc-500">Chưa có tương tác trong khoảng thời gian này.</p>}</div>
  </section>;
}
