# Sử dụng image JRE thu gọn nhất của Java 21 để tiết kiệm dung lượng
FROM eclipse-temurin:21-jre-alpine

# Thư mục làm việc trong container
WORKDIR /app

# Khai báo port ứng dụng sẽ chạy
EXPOSE 8080

# Copy file jar đã được build từ GitHub Actions vào container
COPY target/*.jar app.jar

# Lệnh khởi động ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
