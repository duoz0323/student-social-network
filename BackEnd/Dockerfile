# =========================
# Giai đoạn 1: Build project
# =========================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Sao chép pom.xml trước để tận dụng Docker cache
COPY pom.xml .

# Tải dependency
RUN mvn dependency:go-offline -B

# Sao chép source code
COPY src ./src

# Build file JAR, bỏ qua test khi deploy
RUN mvn clean package -DskipTests

# Lấy file JAR thực thi, loại trừ file plain.jar nếu có
RUN JAR_FILE=$(find target -maxdepth 1 -type f \
    -name "*.jar" \
    ! -name "*-plain.jar" \
    ! -name "*.original" \
    | head -n 1) \
    && test -n "$JAR_FILE" \
    && cp "$JAR_FILE" /app/app.jar


# ===========================
# Giai đoạn 2: Chạy ứng dụng
# ===========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Tạo user riêng, không chạy ứng dụng bằng root
RUN groupadd --system spring \
    && useradd --system --gid spring spring

COPY --from=build /app/app.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]