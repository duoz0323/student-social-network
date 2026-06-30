import { isAuthenticated } from "./auth.js";
import { renderFeedPage, renderLoginPage } from "./renderers.js";

const routes = {
  "#/login": {
    requiresAuth: false,
    render: renderLoginPage,
  },
  "#/feed/for-you": {
    requiresAuth: true,
    render: renderFeedPage,
  },
};

export function startRouter(root) {
  window.addEventListener("hashchange", () => renderRoute(root));

  // Route mặc định phụ thuộc vào session để reload trang vẫn giữ đúng luồng.
  if (!window.location.hash) {
    window.location.hash = isAuthenticated() ? "#/feed/for-you" : "#/login";
    return;
  }

  renderRoute(root);
}

function renderRoute(root) {
  const route = routes[window.location.hash] ?? routes["#/login"];

  // Route guard tối thiểu: feed yêu cầu session còn hạn.
  if (route.requiresAuth && !isAuthenticated()) {
    window.location.hash = "#/login";
    return;
  }

  // Người đã đăng nhập không cần quay lại login khi reload/hash đổi.
  if (window.location.hash === "#/login" && isAuthenticated()) {
    window.location.hash = "#/feed/for-you";
    return;
  }

  route.render(root);
}
