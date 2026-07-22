package com.stu.edu.vn.backend.common.util;

/**
 * Chuẩn hóa ký tự đặc biệt trước khi đưa từ khóa vào biểu thức LIKE có ESCAPE '='.
 */
public final class LikePatternEscaper {

    private static final String ESCAPE_CHARACTER = "=";

    private LikePatternEscaper() {
        // Không cho khởi tạo utility chỉ chứa hàm dùng chung.
    }

    public static String escape(String keyword) {
        // Escape chính ký tự escape trước để không làm thay đổi các ký tự vừa được bổ sung sau đó.
        return keyword.replace(ESCAPE_CHARACTER, ESCAPE_CHARACTER + ESCAPE_CHARACTER)
                .replace("%", ESCAPE_CHARACTER + "%")
                .replace("_", ESCAPE_CHARACTER + "_");
    }
}
