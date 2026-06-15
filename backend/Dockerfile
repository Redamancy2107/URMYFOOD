# Stage 1: Build file JAR trong GitHub Actions/Docker
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
# Cache dependencies trước để tránh download lại ở các build sau
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime (Sử dụng JRE để tối ưu dung lượng dưới 200MB)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Tạo user non-root để chạy app bảo mật
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Các tham số JVM tối ưu cho môi trường RAM thấp (512MB)
# -XX:MaxRAMPercentage=70.0: Giới hạn heap tối đa 70% RAM của container (chừa 30% cho stack, metaspace, OS)
# -XX:+UseSerialGC: GC tuần tự giúp tiết kiệm bộ nhớ tối đa cho container nhỏ
# -Xss512k: Giảm thread stack size xuống 512KB để tiết kiệm RAM
# -XX:+TieredCompilation -XX:TieredStopAtLevel=1: Giảm tải JIT compiler để khởi động nhanh hơn và tốn ít RAM hơn
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70.0 -XX:+UseSerialGC -Xss512k -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
