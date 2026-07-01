# CURRENT TASK

## Trang thai

Hoan thanh cap nhat Frontend React, mock data va tai lieu UI/Data cho luong dang ky va onboarding moi.

## Pham vi da thuc hien

- Dang ky MVP dung mot truong `identifier`, `password`, `confirmPassword`.
- Sau dang ky tao mock user, mock profile rong va session React hop le.
- Session React dung localStorage key rieng `unishare.react.session`.
- Session chi luu `userId`, `role`, `expiresAt`.
- Mock data React co key rieng `unishare.react.mock-data` de giu user vua dang ky sau reload.
- Them onboarding tai `/onboarding/profile` voi ba buoc noi bo.
- Them man hinh thanh cong tai `/onboarding/success`.
- Cap nhat route guard cho ba trang thai: guest, da dang nhap chua hoan tat ho so, da dang nhap da hoan tat ho so.
- Google/Facebook chi hien thi tren UI va bao "Tinh nang dang duoc phat trien.".
- Khong trien khai OAuth, khong tao session khi bam Google/Facebook.
- Khong sua Backend, SQL hoac DBML.

## File da tao

- `FrontEnd/src/features/auth/components/SocialAuthButtons.jsx`
- `FrontEnd/src/features/auth/pages/OnboardingProfilePage.jsx`
- `FrontEnd/src/features/auth/pages/OnboardingSuccessPage.jsx`
- `docs/ai/CURRENT_TASK.md`

## File da sua

- `FrontEnd/src/contexts/AppContext.jsx`
- `FrontEnd/src/data/mockData.js`
- `FrontEnd/src/features/auth/pages/LoginPage.jsx`
- `FrontEnd/src/features/auth/pages/RegisterPage.jsx`
- `FrontEnd/src/router/index.jsx`
- `docs/data/DEMO-DATA.md`
- `docs/data/demo-data.json`
- `docs/ui/COMPONENTS.md`
- `docs/ui/SCREEN-LIST.md`
- `docs/ui/UI-FIX-LIST.md`
- `docs/ui/UI-FLOW.md`

## Kiem tra da chay

- `npm.cmd run lint`: pass.
- `npm.cmd run build`: pass.

Repo FrontEnd khong co test script rieng trong `package.json`, nen chua co lenh test tu dong de chay them.

## Luu y ky thuat

- `passwordHash` bang SHA-256 chi la mo phong Frontend cho mock data. Day khong phai co che bao mat that; Backend thuc te phai dung BCrypt.
- Route `/register/success` khong con la luong chinh va dang redirect ve `/onboarding/profile`.
- File `RegisterSuccessPage.jsx` cu chua bi xoa vi khong con duoc import trong router; co the don dep sau neu muon giam file legacy.

## Chua thuc hien

- Chua trien khai OAuth Google/Facebook.
- Chua trien khai luong quen/doi mat khau P2.
- Chua sua Backend, SQL, DBML.
- Chua commit hoac push.
