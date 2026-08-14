import { useCallback, useEffect, useState } from 'react';
import { Users } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { recommendationApi } from '../../../api/index.js';
import Avatar from '../../../components/common/Avatar.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import {
  recommendationReasonTexts,
  removeStudentRecommendation,
} from '../utils/studentRecommendation.js';

const INITIAL_VISIBLE_RECOMMENDATIONS = 4;
const RECOMMENDATION_REVEAL_STEP = 3;

export default function StudentRecommendationRail() {
  const navigate = useNavigate();
  const {
    toggleFollow,
    showToast,
    userRelationshipRevision,
  } = useApp();
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [pendingUserId, setPendingUserId] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);
  const [visibleCount, setVisibleCount] = useState(INITIAL_VISIBLE_RECOMMENDATIONS);

  const retry = useCallback(() => setReloadKey((value) => value + 1), []);

  useEffect(() => {
    const controller = new AbortController();

    async function loadRecommendations() {
      setLoading(true);
      setError('');
      setVisibleCount(INITIAL_VISIBLE_RECOMMENDATIONS);
      try {
        const page = await recommendationApi.getStudents({ page: 0, size: 10 }, controller.signal);
        setRecommendations(page.content ?? []);
      } catch (requestError) {
        if (requestError.code !== 'ERR_CANCELED') {
          setError(requestError.message || 'Không thể tải gợi ý sinh viên.');
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    }

    void loadRecommendations();
    return () => controller.abort();
  }, [reloadKey, userRelationshipRevision]);

  async function followRecommendation(event, recommendation) {
    event.stopPropagation();
    if (pendingUserId != null) return;
    setPendingUserId(recommendation.userId);
    try {
      await toggleFollow(recommendation.userId, false);
      setRecommendations((current) => removeStudentRecommendation(current, recommendation.userId));
      showToast(`Đã theo dõi ${recommendation.displayName}.`);
    } catch (requestError) {
      showToast(requestError.message || 'Không thể theo dõi. Vui lòng thử lại.', 'error');
    } finally {
      setPendingUserId(null);
    }
  }

  function openProfile(userId) {
    navigate(`/profile/${userId}`);
  }

  // Chỉ mở thêm nội dung trong vùng cuộn; chiều cao card phải luôn ổn định khi người dùng bấm Xem thêm.
  const visibleRecommendations = recommendations.slice(0, visibleCount);
  const isExpanded = visibleCount > INITIAL_VISIBLE_RECOMMENDATIONS;
  const canToggleRecommendations = recommendations.length > INITIAL_VISIBLE_RECOMMENDATIONS;

  function toggleRecommendations() {
    setVisibleCount((current) => current > INITIAL_VISIBLE_RECOMMENDATIONS
      ? INITIAL_VISIBLE_RECOMMENDATIONS
      : Math.min(INITIAL_VISIBLE_RECOMMENDATIONS + RECOMMENDATION_REVEAL_STEP, recommendations.length));
  }

  return (
    <aside
      className="fixed right-10 top-[calc(var(--header-height)+1.5rem)] z-10 hidden w-[280px] xl:block 2xl:w-[var(--right-panel-width)]"
      aria-labelledby="student-recommendation-rail-title"
    >
      <section className="flex h-[390px] flex-col overflow-hidden rounded-[var(--radius-modal)] border border-[var(--app-border)] bg-[var(--app-surface)] p-4 shadow-sm">
        <div className="mb-2 flex items-center justify-between gap-3">
          <h2 id="student-recommendation-rail-title" className="flex min-w-0 items-center gap-2 text-[16px] font-bold text-[var(--app-text)]">
            <Users size={18} className="shrink-0 text-[var(--app-muted)]" aria-hidden="true" />
            Có thể bạn biết
          </h2>
          {error ? (
            <button type="button" onClick={retry} className="shrink-0 text-[12px] font-bold text-[var(--app-brand)] hover:underline">
              Thử lại
            </button>
          ) : null}
        </div>

        {loading ? (
          <div className="grid flex-1 gap-2" role="status" aria-label="Đang tải gợi ý sinh viên">
            {[0, 1, 2, 3].map((item) => <div key={item} className="h-16 animate-pulse rounded-xl bg-[var(--app-surface-soft)]" />)}
          </div>
        ) : error ? (
          <p className="grid flex-1 place-items-center rounded-xl bg-[var(--app-surface-soft)] px-3 py-5 text-center text-[13px] text-[var(--app-muted)]">{error}</p>
        ) : recommendations.length === 0 ? (
          <p className="grid flex-1 place-items-center rounded-xl bg-[var(--app-surface-soft)] px-3 py-5 text-center text-[13px] text-[var(--app-muted)]">
            Chưa có sinh viên nào có điểm chung phù hợp.
          </p>
        ) : (
          <div className="flex min-h-0 flex-1 flex-col">
            <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain pr-1 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden" data-testid="student-recommendation-list">
              {visibleRecommendations.map((recommendation) => {
                const reasons = recommendationReasonTexts(recommendation);
                const pending = String(pendingUserId) === String(recommendation.userId);
                return (
                  <article
                    key={recommendation.userId}
                    className="group flex min-w-0 cursor-pointer items-center gap-2.5 rounded-xl px-1.5 py-2.5 transition hover:bg-[var(--app-surface-soft)]"
                    onClick={() => openProfile(recommendation.userId)}
                  >
                    <button type="button" className="shrink-0 rounded-full" onClick={() => openProfile(recommendation.userId)} aria-label={`Mở hồ sơ ${recommendation.displayName}`}>
                      <Avatar src={recommendation.avatarUrl} name={recommendation.displayName} size="md" className="!h-10 !w-10" />
                    </button>
                    <button type="button" className="min-w-0 flex-1 text-left" onClick={() => openProfile(recommendation.userId)}>
                      <span className="block truncate text-[13px] font-bold text-[var(--app-text)]">{recommendation.displayName}</span>
                      <span className="block truncate text-[11px] text-[var(--app-muted)]">{reasons[0] ?? `@${recommendation.username}`}</span>
                    </button>
                    <button
                      type="button"
                      disabled={pending}
                      onClick={(event) => followRecommendation(event, recommendation)}
                      className="min-w-[76px] shrink-0 whitespace-nowrap rounded-full bg-[var(--app-active)] px-3 py-1.5 text-[11px] font-bold text-[var(--app-active-contrast)] transition hover:opacity-90 disabled:cursor-wait disabled:opacity-60"
                    >
                      {pending ? '...' : 'Theo dõi'}
                    </button>
                  </article>
                );
              })}
            </div>
            {canToggleRecommendations ? (
              <button
                type="button"
                onClick={toggleRecommendations}
                className="mt-2 shrink-0 border-t border-[var(--app-border)] pt-3 text-left text-[13px] font-bold text-[var(--app-brand)] transition hover:opacity-80"
              >
                {isExpanded ? 'Thu gọn' : 'Xem thêm'}
              </button>
            ) : null}
          </div>
        )}
      </section>
    </aside>
  );
}
