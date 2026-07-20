package com.stu.edu.vn.backend.auth.google;

/** Port xác minh Google ID Token để nghiệp vụ Auth không phụ thuộc trực tiếp Google SDK. */
public interface GoogleIdentityVerifier {

    VerifiedGoogleIdentity verify(String rawIdToken);
}
