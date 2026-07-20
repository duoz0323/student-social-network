// Client ID là cấu hình công khai của Google Identity Services, không phải secret.
export const authConfig = Object.freeze({
  googleClientId: import.meta.env.VITE_GOOGLE_CLIENT_ID?.trim() ?? '',
});
