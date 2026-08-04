import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronRight, Search, Sparkles, X } from 'lucide-react';
import { socialApi } from '../../../api/index.js';
import Avatar from '../../../components/common/Avatar.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import InfinitePostList from '../../post/components/InfinitePostList.jsx';
import { useInfinitePosts } from '../../post/hooks/useInfinitePosts.js';
import { invalidatePostListCaches } from '../../post/hooks/postListCache.js';
import { toPostView } from '../../post/utils/postViewModel.js';
import { moveSearchSuggestionIndex } from '../utils/searchSuggestionNavigation.js';

// Dữ liệu mẫu thịnh hành sinh viên theo phong cách Threads
const TOPIC_CHIPS = ['Lập trình Web', 'Đồ án tốt nghiệp', 'cntt_stu', 'Tuyển dụng thực tập', 'Tài liệu ôn thi', 'CLB Sinh Viên'];

const TRENDING_TOPICS = [
  {
    id: 1,
    title: 'Đăng ký đồ án môn học học kỳ mới',
    summary: 'Khoa CNTT mở cổng đăng ký đề tài đồ án môn học và phân công giảng viên hướng dẫn.',
    postCount: '12.5K bài viết',
  },
  {
    id: 2,
    title: 'Hội thảo Công nghệ & Cơ hội Thực tập 2026',
    summary: 'Giao lưu trực tiếp với đại diện doanh nghiệp phần mềm và nhận tư vấn CV sinh viên.',
    postCount: '8.4K bài viết',
  },
  {
    id: 3,
    title: 'Chia sẻ tài liệu ôn thi giữa kỳ môn Cấu trúc dữ liệu',
    summary: 'Tổng hợp bộ đề thi mẫu kèm đáp án chi tiết môn Cấu trúc dữ liệu và Giải thuật.',
    postCount: '5.1K bài viết',
  },
  {
    id: 4,
    title: 'Giải bóng đá Sinh viên STU 2026 chính thức khởi tranh',
    summary: 'Lịch thi đấu các trận vòng bảng bóng đá nam và nữ diễn ra vào cuối tuần này.',
    postCount: '3.2K bài viết',
  },
];

/** Mỗi từ khóa có một vòng đời cursor riêng để request cũ không ghi đè kết quả truy vấn mới. */
function SearchPostResults({ query, hasUserResults }) {
  const searchType = query.startsWith('#') ? 'HASHTAG' : 'CONTENT';
  const searchPostState = useInfinitePosts({
    cacheKey: `search:${searchType}:${encodeURIComponent(query)}`,
    request: ({ limit, cursor }) => socialApi.searchPosts({
      q: query,
      type: searchType,
      limit,
      cursor,
    }),
    normalizePost: toPostView,
  });

  return (
    <InfinitePostList
      {...searchPostState}
      errorTitle="Không thể tải kết quả bài viết"
      emptyTitle={hasUserResults ? 'Không có bài viết' : 'Không có kết quả'}
      emptyDescription={`Không tìm thấy bài viết nào phù hợp cho "${query}".`}
    />
  );
}

export default function SearchPage() {
  const navigate = useNavigate();
  const { currentUserId, toggleFollow, showToast } = useApp();

  // inputText: Nội dung gõ trên thanh tìm kiếm
  const [inputText, setInputText] = useState('');

  // submittedQuery: Từ khóa đã xác nhận (chỉ cập nhật khi bấm Enter hoặc chọn gợi ý)
  const [submittedQuery, setSubmittedQuery] = useState('');

  const [users, setUsers] = useState([]);
  const [dropdownUsers, setDropdownUsers] = useState([]);
  const [activeTab, setActiveTab] = useState('relevant'); // 'relevant' | 'latest' | 'profiles'
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [activeSuggestionIndex, setActiveSuggestionIndex] = useState(-1);
  const [pendingFollowId, setPendingFollowId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const searchContainerRef = useRef(null);

  // Đóng dropdown tìm kiếm nhanh khi click ra ngoài
  useEffect(() => {
    function handleClickOutside(e) {
      if (searchContainerRef.current && !searchContainerRef.current.contains(e.target)) {
        setDropdownOpen(false);
      }
    }
    document.addEventListener('pointerdown', handleClickOutside);
    return () => document.removeEventListener('pointerdown', handleClickOutside);
  }, []);

  // Fetch kết quả gợi ý nhanh cho Dropdown khi người dùng đang gõ (inputText)
  useEffect(() => {
    const keyword = inputText.trim();
    if (!keyword) {
      return;
    }

    // Từ khóa đã xác nhận phải hiển thị kết quả ngay, không tự mở lại dropdown.
    if (keyword === submittedQuery.trim()) {
      return;
    }

    const controller = new AbortController();
    const timer = setTimeout(async () => {
      try {
        const userPage = await socialApi.searchUsers({ q: keyword, page: 0, size: 5 }, controller.signal);
        const fetchedUsers = (userPage.content ?? []).map((user) => ({
          ...user,
          id: user.userId ?? user.id,
          followedByCurrentUser: Boolean(user.followedByCurrentUser),
        }));
        setDropdownUsers(fetchedUsers);
      } catch {
        // Bỏ qua lỗi canceled request khi gõ nhanh
      }
    }, 250);

    return () => {
      clearTimeout(timer);
      controller.abort();
    };
  }, [inputText, submittedQuery]);

  // Fetch dữ liệu tìm kiếm chính CHỈ KHI người dùng bấm ENTER hoặc chọn từ khóa (submittedQuery)
  useEffect(() => {
    const keyword = submittedQuery.trim();
    if (!keyword) {
      return;
    }

    const controller = new AbortController();

    async function fetchMainResults() {
      try {
        const userPage = await socialApi.searchUsers({ q: keyword, page: 0, size: 20 }, controller.signal);

        const fetchedUsers = (userPage.content ?? []).map((user) => ({
          ...user,
          id: user.userId ?? user.id,
          followedByCurrentUser: Boolean(user.followedByCurrentUser),
        }));

        setUsers(fetchedUsers);
      } catch (requestError) {
        if (requestError.code !== 'ERR_CANCELED') setError(requestError.message);
      } finally {
        setLoading(false);
      }
    }

    fetchMainResults();

    return () => {
      controller.abort();
    };
  }, [submittedQuery]);

  // Xử lý xác nhận tìm kiếm (Ấn Enter hoặc Click từ khóa)
  function handleConfirmSearch(keywordToSearch) {
    const target = (keywordToSearch !== undefined ? keywordToSearch : inputText).trim();
    if (target && target !== submittedQuery.trim()) {
      // Chỉ giữ cache của truy vấn hiện tại để lịch sử gõ dài không làm cache tăng vô hạn.
      invalidatePostListCaches(['search:']);
      setLoading(true);
    }
    setInputText(target);
    setSubmittedQuery(target);
    setDropdownUsers([]);
    setDropdownOpen(false);
    setActiveSuggestionIndex(-1);
    setError('');
  }

  // Xử lý xóa từ khóa
  function handleClearSearch() {
    setInputText('');
    setSubmittedQuery('');
    setDropdownUsers([]);
    setDropdownOpen(false);
    setActiveSuggestionIndex(-1);
    setUsers([]);
    setLoading(false);
    setError('');
  }

  // Xử lý theo dõi / bỏ theo dõi người dùng và cập nhật state chính xác
  async function handleToggleFollow(event, targetUser) {
    event.stopPropagation();
    if (pendingFollowId) return;
    setPendingFollowId(targetUser.id);
    try {
      const isFollowing = Boolean(targetUser.followedByCurrentUser);
      const response = await toggleFollow(targetUser.id, isFollowing);

      const updateList = (current) =>
        current.map((item) =>
          String(item.id) === String(targetUser.id)
            ? {
                ...item,
                followedByCurrentUser: response.followedByCurrentUser,
                followerCount: response.followedByCurrentUser
                  ? (item.followerCount || 0) + 1
                  : Math.max(0, (item.followerCount || 1) - 1),
              }
            : item
        );

      setUsers(updateList);
      setDropdownUsers(updateList);
    } catch (err) {
      showToast(err.message || 'Không thể thực hiện. Vui lòng thử lại.');
    } finally {
      setPendingFollowId(null);
    }
  }

  function navigateToProfile(targetId) {
    setDropdownOpen(false);
    setActiveSuggestionIndex(-1);
    navigate(String(targetId) === String(currentUserId) ? '/profile/me' : `/profile/${targetId}`);
  }

  // Danh sách từ khóa gợi ý bổ sung kiểu Threads
  const keywordVariations = inputText.trim()
    ? [
        inputText.trim(),
        `${inputText.trim()} STU`,
        `${inputText.trim()} CNTT`,
        `${inputText.trim()} 2026`,
      ].filter((v, idx, arr) => arr.indexOf(v) === idx)
    : [];

  const suggestionCount = keywordVariations.length + dropdownUsers.length;

  // Kích hoạt đúng hành vi của dòng đang chọn: tìm từ khóa hoặc mở trang cá nhân.
  function activateSuggestion(index) {
    if (index < 0 || index >= suggestionCount) return false;

    if (index < keywordVariations.length) {
      handleConfirmSearch(keywordVariations[index]);
    } else {
      const selectedUser = dropdownUsers[index - keywordVariations.length];
      if (!selectedUser) return false;
      navigateToProfile(selectedUser.id);
    }

    return true;
  }

  function handleSearchKeyDown(event) {
    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault();
      if (!inputText.trim()) return;

      setDropdownOpen(true);
      setActiveSuggestionIndex((currentIndex) =>
        moveSearchSuggestionIndex(currentIndex, suggestionCount, event.key === 'ArrowUp' ? 'up' : 'down')
      );
      return;
    }

    if (event.key === 'Escape') {
      setDropdownOpen(false);
      setActiveSuggestionIndex(-1);
      return;
    }

    if (event.key === 'Enter') {
      event.preventDefault();
      if (!dropdownOpen || !activateSuggestion(activeSuggestionIndex)) {
        handleConfirmSearch();
      }
    }
  }

  function handleSearchInputChange(event) {
    const nextValue = event.target.value;
    setInputText(nextValue);
    setDropdownOpen(Boolean(nextValue.trim()));
    setActiveSuggestionIndex(-1);
    if (!nextValue.trim()) setDropdownUsers([]);
  }

  // Header tìm kiếm tối giản
  const header = (
    <div ref={searchContainerRef} className={`relative px-4 pt-3 sm:px-6 ${submittedQuery.trim() && !dropdownOpen ? 'pb-0' : 'pb-3'}`}>
      <div className="relative w-full">
        <div className="relative flex w-full items-center">
          <Search size={18} className="absolute left-4 text-[var(--app-muted)] pointer-events-none" />
          <input
            value={inputText}
            role="combobox"
            aria-autocomplete="list"
            aria-expanded={dropdownOpen}
            aria-controls="search-suggestion-list"
            aria-activedescendant={activeSuggestionIndex >= 0 ? `search-suggestion-${activeSuggestionIndex}` : undefined}
            onFocus={() => {
              if (inputText.trim() && inputText.trim() !== submittedQuery.trim()) setDropdownOpen(true);
            }}
            onKeyDown={handleSearchKeyDown}
            onChange={handleSearchInputChange}
            placeholder="Tìm kiếm"
            className="h-12 w-full rounded-2xl border border-transparent bg-[var(--app-surface-soft)] pl-11 pr-10 text-[15px] font-medium text-[var(--app-text)] placeholder:text-[var(--app-muted)] outline-none transition focus:border-[var(--app-border-strong)] focus:bg-[var(--app-surface)]"
          />
          {inputText ? (
            <button
              type="button"
              onClick={handleClearSearch}
              aria-label="Xóa từ khóa"
              className="absolute right-3.5 grid h-6 w-6 place-items-center rounded-full text-[var(--app-muted)] transition hover:bg-[var(--app-surface-soft)] hover:text-[var(--app-text)]"
            >
              <X size={16} />
            </button>
          ) : null}
        </div>

        {/* DROPDOWN NỔI ĐÈ CHE HOÀN TOÀN KHUNG FORM BÊN DƯỚI */}
        {dropdownOpen && inputText.trim() && (
          <div
            id="search-suggestion-list"
            role="listbox"
            className="absolute inset-x-0 top-full z-50 mt-1 overflow-hidden rounded-[22px] border border-[var(--app-border-strong)] bg-[var(--app-bg)] shadow-2xl"
          >
            {/* DANH SÁCH GỢI Ý TỪ KHÓA */}
            {keywordVariations.map((kw, index) => (
              <div
                key={kw}
                id={`search-suggestion-${index}`}
                role="option"
                aria-selected={activeSuggestionIndex === index}
                className={`group flex cursor-pointer items-stretch gap-4 px-5 transition hover:bg-[var(--app-surface-soft)] ${
                  activeSuggestionIndex === index ? 'bg-[var(--app-surface-soft)]' : ''
                }`}
                onMouseEnter={() => setActiveSuggestionIndex(index)}
                onClick={() => handleConfirmSearch(kw)}
              >
                <Search size={19} className="my-auto shrink-0 text-[var(--app-muted)]" />
                <div
                  className={`flex min-w-0 flex-1 items-center justify-between py-4 ${
                    index < keywordVariations.length - 1 || dropdownUsers.length > 0
                      ? 'border-b border-[var(--app-border)]'
                      : ''
                  }`}
                >
                  <span className="text-[15px] font-bold text-[var(--app-text)]">{kw}</span>
                  <ChevronRight size={18} className="shrink-0 text-[var(--app-muted)] transition group-hover:translate-x-0.5" />
                </div>
              </div>
            ))}

            {/* DANH SÁCH GỢI Ý NGƯỜI DÙNG KÈM NÚT THEO DÕI NỔI */}
            {dropdownUsers.map((user, index) => {
              const isSelf = String(user.id) === String(currentUserId);
              const isFollowing = Boolean(user.followedByCurrentUser);
              const isPending = pendingFollowId === user.id;
              const suggestionIndex = keywordVariations.length + index;

              return (
                <div
                  key={user.id}
                  id={`search-suggestion-${suggestionIndex}`}
                  role="option"
                  aria-selected={activeSuggestionIndex === suggestionIndex}
                  className={`flex cursor-pointer items-stretch gap-3.5 px-5 transition hover:bg-[var(--app-surface-soft)] ${
                    activeSuggestionIndex === suggestionIndex ? 'bg-[var(--app-surface-soft)]' : ''
                  }`}
                  onMouseEnter={() => setActiveSuggestionIndex(suggestionIndex)}
                  onClick={() => navigateToProfile(user.id)}
                >
                  <Avatar src={user.avatarUrl} name={user.displayName} size="md" className="my-auto !h-11 !w-11 shrink-0" />
                  <div
                    className={`flex min-w-0 flex-1 items-center gap-4 py-3.5 ${
                      index < dropdownUsers.length - 1 ? 'border-b border-[var(--app-border)]' : ''
                    }`}
                  >
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-[14.5px] font-bold text-[var(--app-text)]">{user.displayName}</p>
                      <p className="truncate text-[12.5px] text-[var(--app-muted)]">@{user.displayName?.toLowerCase().replace(/\s+/g, '')}</p>
                    </div>

                    {!isSelf && (
                      <button
                        type="button"
                        disabled={isPending}
                        onClick={(e) => handleToggleFollow(e, user)}
                        className={`h-9 min-w-[96px] rounded-full px-4 text-[13.5px] font-bold transition shadow-xs ${
                          isFollowing
                            ? 'border border-[var(--app-border-strong)] bg-[var(--app-control-bg)] text-[var(--app-text)] hover:bg-[var(--app-surface-soft)]'
                            : 'border border-transparent bg-[var(--app-active)] text-[var(--app-active-contrast)] hover:opacity-90'
                        }`}
                      >
                        {isPending ? '...' : isFollowing ? 'Hủy theo dõi' : 'Theo dõi'}
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* TABS PHÂN LOẠI KẾT QUẢ KHI ĐÃ SUBMIT TÌM KIẾM (ẤN ENTER) */}
      {submittedQuery.trim() && !dropdownOpen ? (
        <div className="mt-2 flex h-11 justify-center gap-6 sm:gap-10">
          {[
            { key: 'relevant', label: 'Liên quan nhất' },
            { key: 'latest', label: 'Mới đây' },
            { key: 'profiles', label: `Trang cá nhân (${users.length})` },
          ].map((tab) => (
            <button
              key={tab.key}
              type="button"
              onClick={() => setActiveTab(tab.key)}
              className={`relative flex h-full items-center px-3 text-[15px] font-bold transition ${
                activeTab === tab.key
                  ? 'text-[var(--app-text)]'
                  : 'text-[var(--app-muted)] hover:text-[var(--app-text)]'
              }`}
            >
              {tab.label}
              {activeTab === tab.key && (
                <span className="feed-tab-indicator absolute inset-x-0 bottom-0 h-[3px] rounded-full bg-[var(--app-text)]" />
              )}
            </button>
          ))}
        </div>
      ) : null}
    </div>
  );

  const displayUsers = activeTab === 'latest' ? [] : users;

  return (
    <ContentShell header={header}>
      {/* NỘI DUNG NẰM TRỰC TIẾP TRONG CONTENT-SHELL (CHỈ 1 VIỀN FORM DUY NHẤT GIỐNG TRANG FEED) */}
      {!submittedQuery.trim() ? (
        <div className="pb-16">
          {/* CHIPS CHỦ ĐỀ CUỘN NGANG KHÔNG HIỂN THỊ THANH CUỘN BROWSER */}
          <div className="no-scrollbar flex gap-2.5 overflow-x-auto p-4 sm:px-6 sm:py-4 [scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden">
            {TOPIC_CHIPS.map((chip) => (
              <button
                key={chip}
                type="button"
                onClick={() => handleConfirmSearch(chip)}
                className="shrink-0 rounded-full border border-[var(--app-border)] bg-[var(--app-surface-soft)] px-4 py-2 text-[14px] font-semibold text-[var(--app-text)] transition hover:bg-[var(--app-surface)] hover:border-[var(--app-border-strong)]"
              >
                {chip}
              </button>
            ))}
          </div>

          {/* TIÊU ĐỀ ĐANG THỊNH HÀNH VỚI BADGE VÀNG HIGHLIGHT */}
          <div className="p-4 sm:p-6">
            <h2 className="text-[18px] font-bold text-[var(--app-text)]">
              <span className="rounded-md bg-amber-400 px-2 py-0.5 text-[15px] font-bold text-black dark:bg-amber-400 dark:text-black">
                Đang thịnh hành
              </span>
            </h2>
            <p className="mt-1 text-[13px] text-[var(--app-muted)]">
              Những gì mọi người đang bàn luận, do AI tóm tắt
            </p>
          </div>

          {/* DANH SÁCH BÀI VIẾT THỊNH HÀNH */}
          <div className="divide-y divide-[var(--app-border)] border-t border-[var(--app-border)]">
            {TRENDING_TOPICS.map((item) => (
              <article
                key={item.id}
                className="cursor-pointer px-4 py-4 sm:px-6 transition hover:bg-[var(--app-surface-soft)]/50"
                onClick={() => handleConfirmSearch(item.title)}
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="min-w-0 flex-1">
                    <h3 className="text-[15px] font-bold text-[var(--app-text)]">{item.title}</h3>
                    <p className="mt-1 text-[13px] leading-relaxed text-[var(--app-muted)] line-clamp-2">{item.summary}</p>
                    <span className="mt-2 inline-block text-[12px] font-semibold text-[var(--app-muted)]">{item.postCount}</span>
                  </div>
                  <div className="grid h-12 w-12 shrink-0 place-items-center rounded-xl bg-[var(--app-surface-soft)] text-[var(--app-muted)]">
                    <Sparkles size={20} className="text-amber-500" />
                  </div>
                </div>
              </article>
            ))}
          </div>
        </div>
      ) : loading ? (
        <LoadingState message="Đang tìm kiếm..." />
      ) : (
        /* MÀN HÌNH KẾT QUẢ TÌM KIẾM CHÍNH KHI ĐÃ SUBMIT TÌM KIẾM (ẤN ENTER) */
        <div className="pb-16">
          {error && <p className="app-error m-4 rounded-xl p-3 text-sm">{error}</p>}

          {displayUsers.length > 0 && (
            <section className="border-b border-[var(--app-border)]">
              {activeTab === 'relevant' && (
                <div className="px-5 pt-4 pb-2">
                  <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--app-muted)]">Người dùng</h2>
                </div>
              )}
              {displayUsers.map((user) => {
                const isSelf = String(user.id) === String(currentUserId);
                const isFollowing = Boolean(user.followedByCurrentUser);
                const isPending = pendingFollowId === user.id;

                return (
                  <article
                    key={user.id}
                    className="flex items-center gap-3.5 border-b border-[var(--app-border)] px-5 py-4 last:border-b-0 transition hover:bg-[var(--app-surface-soft)]/50"
                  >
                    <button
                      type="button"
                      onClick={() => navigateToProfile(user.id)}
                      className="shrink-0 focus:outline-none"
                    >
                      <Avatar src={user.avatarUrl} name={user.displayName} size="md" className="!h-11 !w-11" />
                    </button>

                    <div
                      className="min-w-0 flex-1 cursor-pointer text-left"
                      onClick={() => navigateToProfile(user.id)}
                    >
                      <h3 className="truncate text-[15px] font-bold text-[var(--app-text)]">{user.displayName}</h3>
                      <p className="truncate text-[13px] text-[var(--app-muted)]">@{user.displayName?.toLowerCase().replace(/\s+/g, '')}</p>
                      {user.bio ? (
                        <p className="mt-1 text-[13px] text-[var(--app-text)] line-clamp-1">{user.bio}</p>
                      ) : null}
                      {Number.isFinite(user.followerCount) && (
                        <p className="mt-1 text-[12px] font-medium text-[var(--app-muted)]">
                          {user.followerCount} người theo dõi
                        </p>
                      )}
                    </div>

                    {!isSelf && (
                      <button
                        type="button"
                        disabled={isPending}
                        onClick={(e) => handleToggleFollow(e, user)}
                        className={`h-9 min-w-[100px] rounded-full px-4 text-[13.5px] font-bold transition shadow-xs ${
                          isFollowing
                            ? 'border border-[var(--app-border-strong)] bg-[var(--app-control-bg)] text-[var(--app-text)] hover:bg-[var(--app-surface-soft)]'
                            : 'border border-transparent bg-[var(--app-active)] text-[var(--app-active-contrast)] hover:opacity-90'
                        }`}
                      >
                        {isPending ? 'Đang xử lý...' : isFollowing ? 'Hủy theo dõi' : 'Theo dõi'}
                      </button>
                    )}
                  </article>
                );
              })}
            </section>
          )}

          {activeTab !== 'profiles' && (
            <section>
              {activeTab === 'relevant' && displayUsers.length > 0 && (
                <div className="px-5 pt-5 pb-2">
                  <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--app-muted)]">Bài viết</h2>
                </div>
              )}
              <SearchPostResults
                key={`${submittedQuery.startsWith('#') ? 'HASHTAG' : 'CONTENT'}:${submittedQuery}`}
                query={submittedQuery.trim()}
                hasUserResults={displayUsers.length > 0}
              />
            </section>
          )}

          {!error && activeTab === 'profiles' && displayUsers.length === 0 && (
            <EmptyState title="Không có kết quả" description={`Không tìm thấy kết quả nào phù hợp cho "${submittedQuery}".`} />
          )}
        </div>
      )}
    </ContentShell>
  );
}
