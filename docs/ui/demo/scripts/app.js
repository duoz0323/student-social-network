import { startRouter } from "./router.js";

const root = document.querySelector("#app");

// Khởi động demo sau khi DOM sẵn sàng vì toàn bộ UI được render bằng JavaScript thuần.
if (root) {
  startRouter(root);
}
