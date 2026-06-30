import { getCurrentUser, login, logout } from "./auth.js";
import { demoData } from "./mock-data.js";
import { createElement, formatRelativeTime, getInitials, setDocumentTitle } from "./ui.js";

export function renderLoginPage(root) {
  setDocumentTitle("Đăng nhập");
  root.innerHTML = "";

  const page = createElement("main", { className: "auth-page" });
  const card = createElement("section", {
    className: "auth-card",
    attributes: { "aria-labelledby": "login-title" },
  });

  card.innerHTML = `
    <div class="brand-lockup auth-brand">
      <img class="brand-logo" src="./assets/brand/logo.png" alt="UniShare" />
      <span>UniShare</span>
    </div>
    <h1 id="login-title" class="auth-title">Đăng nhập</h1>
    <p class="auth-copy">Dùng tài khoản demo bằng email hoặc số điện thoại để vào Feed For You.</p>
    <form class="form-stack" id="login-form" novalidate>
      <div class="field-group">
        <label class="field-label" for="identifier">Email hoặc số điện thoại</label>
        <input class="text-input" id="identifier" name="identifier" autocomplete="username" required />
      </div>
      <div class="field-group">
        <label class="field-label" for="password">Mật khẩu</label>
        <input class="text-input" id="password" name="password" type="password" autocomplete="current-password" required />
      </div>
      <p class="form-message" id="login-message" role="alert"></p>
      <button class="primary-button" type="submit">Đăng nhập</button>
    </form>
    <div class="demo-note">
      Demo giai đoạn 1 không có đăng ký, OAuth hoặc quên mật khẩu. Tài khoản bị khóa được dùng để kiểm tra từ chối đăng nhập.
    </div>
  `;

  page.append(card);
  root.append(page);

  const form = card.querySelector("#login-form");
  const message = card.querySelector("#login-message");

  form.addEventListener("submit", (event) => {
    event.preventDefault();

    const formData = new FormData(form);
    const identifier = String(formData.get("identifier") ?? "");
    const password = String(formData.get("password") ?? "");

    // Validation phía UI giúp người dùng biết thiếu dữ liệu trước khi login giả.
    if (!identifier.trim() || !password) {
      message.textContent = "Vui lòng nhập email/số điện thoại và mật khẩu.";
      return;
    }

    const result = login(identifier, password);

    if (!result.ok) {
      message.textContent = result.message;
      return;
    }

    window.location.hash = "#/feed/for-you";
  });
}

export function renderFeedPage(root) {
  setDocumentTitle("Feed For You");
  root.innerHTML = "";

  const currentUser = getCurrentUser();
  const shell = createElement("div", { className: "user-shell" });

  shell.append(renderUserSidebar(currentUser), renderFeedMain(currentUser));
  root.append(shell);
}

function renderUserSidebar(currentUser) {
  const aside = createElement("aside", { className: "user-sidebar" });

  aside.innerHTML = `
    <a class="brand-lockup" href="#/feed/for-you" aria-label="UniShare Feed">
      <img class="brand-logo" src="./assets/brand/logo.png" alt="" />
      <span>UniShare</span>
    </a>
    <nav class="nav-list" aria-label="Điều hướng người dùng">
      <a class="nav-item is-active" href="#/feed/for-you">
        <span class="nav-icon">⌂</span>
        <span>Dành cho bạn</span>
      </a>
      <span class="nav-item" data-hidden-mobile="true" aria-disabled="true">
        <span class="nav-icon">＋</span>
        <span>Tạo bài viết</span>
      </span>
      <span class="nav-item" aria-disabled="true">
        <span class="nav-icon">⌕</span>
        <span>Tìm kiếm</span>
      </span>
      <span class="nav-item" aria-disabled="true">
        <span class="nav-icon">♡</span>
        <span>Hoạt động</span>
      </span>
      <span class="nav-item" aria-disabled="true">
        <span class="nav-icon">♙</span>
        <span>Trang cá nhân</span>
      </span>
      <span class="nav-item" data-hidden-mobile="true" aria-disabled="true">
        <span class="nav-icon">▱</span>
        <span>Bài viết đã lưu</span>
      </span>
    </nav>
  `;

  const footer = createElement("div", { className: "sidebar-footer" });
  const userBox = createElement("div", { className: "session-user" });

  // Chỉ hiển thị displayName và role, không hiển thị email/số điện thoại.
  userBox.innerHTML = `
    <strong>${currentUser.displayName}</strong>
    <span>${currentUser.role}</span>
  `;

  const logoutButton = createElement("button", {
    className: "secondary-button",
    text: "Đăng xuất",
    attributes: { type: "button" },
  });

  logoutButton.addEventListener("click", () => {
    logout();
    window.location.hash = "#/login";
  });

  footer.append(userBox, logoutButton);
  aside.append(footer);
  return aside;
}

function renderFeedMain(currentUser) {
  const mainArea = createElement("main", { className: "main-area" });
  const feedColumn = createElement("section", {
    className: "feed-column",
    attributes: { "aria-labelledby": "feed-title" },
  });

  feedColumn.innerHTML = `
    <div class="feed-title-row">
      <h1 id="feed-title">Dành cho bạn</h1>
      <p class="feed-muted">Feed For You từ mock data.</p>
    </div>
    <div class="feed-tabs" role="tablist" aria-label="Feed">
      <button class="feed-tab" type="button" role="tab" aria-selected="true">Dành cho bạn</button>
    </div>
  `;

  feedColumn.append(renderComposerPreview(currentUser), renderForYouPosts());
  mainArea.append(feedColumn);
  return mainArea;
}

function renderComposerPreview(currentUser) {
  const composer = createElement("div", { className: "composer-preview" });
  composer.append(
    renderAvatar(currentUser),
    createElement("span", { className: "feed-muted", text: "Có gì mới?" }),
    createElement("button", {
      className: "primary-button",
      text: "Đăng",
      attributes: { type: "button", disabled: "disabled", title: "Chưa triển khai ở Giai đoạn 1" },
    }),
  );
  return composer;
}

function renderForYouPosts() {
  const list = createElement("div", { attributes: { "aria-label": "Danh sách bài viết For You" } });

  const publishedPosts = demoData.feeds.forYou
    .map((postId) => demoData.posts.find((post) => post.id === postId))
    .filter((post) => post && post.status === "PUBLISHED");

  if (publishedPosts.length === 0) {
    list.append(
      createElement("div", {
        className: "empty-state",
        text: "Chưa có bài viết phù hợp trong Feed For You.",
      }),
    );
    return list;
  }

  publishedPosts.forEach((post) => {
    list.append(renderPostCard(post));
  });

  return list;
}

function renderPostCard(post) {
  const author = demoData.users.find((user) => user.id === post.authorId);
  const card = createElement("article", { className: "post-card" });

  card.append(renderAvatar(author));

  const body = createElement("div");
  const header = createElement("div", { className: "post-header" });
  const meta = createElement("div", { className: "post-meta" });
  const menu = createElement("button", {
    className: "icon-button",
    text: "⋯",
    attributes: { type: "button", title: "Menu bài viết chưa triển khai" },
  });

  meta.innerHTML = `
    <span class="post-author">${author.displayName}</span>
    <span> · ${formatRelativeTime(post.createdAt)} trước${post.edited ? " · đã chỉnh sửa" : ""}</span>
  `;

  header.append(meta, menu);
  body.append(header);
  body.append(createElement("p", { className: "post-content", text: post.content }));

  if (post.hashtags.length > 0) {
    const hashtags = createElement("div", { className: "hashtag-list" });
    post.hashtags.forEach((tag) => {
      hashtags.append(createElement("span", { className: "hashtag", text: `#${tag}` }));
    });
    body.append(hashtags);
  }

  post.imageUrls.forEach((imageUrl) => {
    const imageWrap = createElement("div", { className: "post-image" });
    imageWrap.innerHTML = `<img src="${imageUrl}" alt="Ảnh minh họa bài viết" />`;
    body.append(imageWrap);
  });

  const actions = createElement("div", { className: "post-actions" });
  actions.innerHTML = `
    <span class="action-chip">♡ ${post.likeCount}</span>
    <span class="action-chip">▢ ${post.commentCount}</span>
    <span class="action-chip">▱</span>
  `;
  body.append(actions);

  card.append(body);
  return card;
}

function renderAvatar(user) {
  const avatar = createElement("div", { className: "avatar", attributes: { "aria-hidden": "true" } });

  if (!user?.avatarUrl) {
    avatar.textContent = "?";
    return avatar;
  }

  avatar.innerHTML = `<img src="${user.avatarUrl}" alt="" />`;

  // Nếu asset ảnh lỗi, fallback dùng chữ cái đầu từ displayName.
  avatar.querySelector("img").addEventListener("error", () => {
    avatar.textContent = getInitials(user.displayName);
  });

  return avatar;
}
