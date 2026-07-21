package com.stu.edu.vn.backend.auth.repository;

import java.nio.file.Files;
import java.nio.file.Path;

/** Tìm thư mục database ổn định khi Maven được chạy từ root hoặc từ BackEnd. */
final class DatabaseSourcePaths {
    private DatabaseSourcePaths() { }

    static Path resolve(String first, String... more) {
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        Path databaseDirectory = workingDirectory.resolve("database");
        if (!Files.isDirectory(databaseDirectory)) {
            databaseDirectory = workingDirectory.resolve("..").resolve("database").normalize();
        }
        return databaseDirectory.resolve(Path.of(first, more));
    }
}
