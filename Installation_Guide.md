## 🚀 Installation Guide

Tài liệu này hướng dẫn chi tiết các bước cài đặt cấu hình, build và khởi chạy hệ thống **URMYFOOD** (bao gồm Backend Spring Boot và 3 ứng dụng Android Client: User, Shop, Admin).

### 🛠 Yêu Cầu Hệ Thống (Prerequisites)

Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã cài đặt các công cụ sau:
1. **Java Development Kit (JDK)**: Phiên bản **21** hoặc mới hơn.
2. **Android Studio**: Phiên bản mới nhất (Koala trở lên được khuyến nghị).
3. **Android SDK**: API Level 36 (Android 14/15) và API Level 29 (Android 10) là mức tối thiểu.
4. **Git**: Để clone mã nguồn.
5. **Maven** (đã tích hợp sẵn wrapper trong thư mục backend).

---

### ⚙️ Hướng Dẫn Cài Đặt & Cấu Hìnnh

#### Bước 1: Clone Mã Nguồn Dự Án
Mở terminal và chạy lệnh sau để tải project về máy:
```bash
git clone https://github.com/Redamancy2107/URMYFOOD.git
cd URMYFOOD
```

---

#### Bước 2: Thiết Lập & Khởi Chạy Backend (Spring Boot)

Backend của dự án giao tiếp với cơ sở dữ liệu Supabase PostgreSQL thông qua file cấu hình môi trường `.env`.

1. **Cấu hình môi trường Backend**:
   - Tạo file `.env` tại thư mục gốc của dự án `URMYFOOD/` (dựa trên file mẫu `.env.example`).
   - Nội dung file `.env` mẫu để chạy dự án:
     ```env
     # Database Configuration
        DB_URL=jdbc:postgresql://<your_supabase_db_host>:5432/postgres
        DB_USERNAME=<your_database_username>
        DB_PASSWORD=<your_database_password>

        # Security Configuration
        JWT_SECRET=<your_jwt_secret_64_character_hex_string>
        JWT_EXPIRATION=86400000
        JWT_REFRESH_EXPIRATION=604800000

        # Social Login & Gmail API Configuration (Google Console)
        GOOGLE_CLIENT_ID=<your_google_web_client_id>.apps.googleusercontent.com
        GOOGLE_CLIENT_SECRET=<your_google_client_secret>
        GOOGLE_REFRESH_TOKEN=<your_google_refresh_token_for_gmail_api>

        # Supabase Storage Configuration
        SUPABASE_URL=https://<your_supabase_project_ref>.supabase.co
        SUPABASE_ANON_KEY=<your_supabase_anonymous_key>
        SUPABASE_STORAGE_BUCKET=urmyfood-bucket
        SUPABASE_PROFILE_IMAGE_MAX_SIZE=1048576
        SUPABASE_PROFILE_IMAGE_ALLOWED_TYPES=image/jpeg,image/png,image/gif
        SUPABASE_SERVICE_ROLE_KEY=<your_supabase_service_role_key>

        # PayOS Configuration (Payment Gateway)
        PAYOS_CLIENT_ID=<your_payos_client_id>
        PAYOS_API_KEY=<your_payos_api_key>
        PAYOS_CHECKSUM_KEY=<your_payos_checksum_key>

     ```

2. **Chạy Backend**:
   - Di chuyển vào thư mục `backend`:
     ```bash
     cd backend
     ```
   - Build và chạy ứng dụng:
     - Trên Windows:
       ```bash
       mvnw.cmd spring-boot:run
       ```
     - Trên macOS/Linux:
       ```bash
       ./mvnw spring-boot:run
       ```
   - Mặc định backend sẽ khởi chạy tại cổng `http://localhost:8080`.

---

#### Bước 3: Thiết Lập & Khởi Chạy Frontend (Android Client)

Thư mục `frontend` chứa 3 ứng dụng Android dạng Gradle multi-module: `:app-user`, `:app-shop`, và `:app-admin`.

1. **Mở dự án trong Android Studio**:
   - Khởi động Android Studio.
   - Chọn **Open** và dẫn tới thư mục `frontend` trong project `URMYFOOD`.

2. **Cấu hình kết nối API & Google Auth**:
   - Tạo một file tên là `local.properties` tại thư mục root của thư mục `frontend` (nếu chưa có).
   - Thêm các cấu hình sau:
     ```properties
     # Địa chỉ IP của máy chạy backend.
     # Nếu dùng Android Emulator, sử dụng IP mặc định: http://10.0.2.2:8080/
     # Nếu dùng thiết bị thật, sử dụng địa chỉ IP mạng LAN của máy chạy backend (ví dụ: http://192.168.1.5:8080/)
     base_url=http://10.0.2.2:8080/

     # Khóa Google Server Client ID dùng cho đăng nhập Google (nếu có cấu hình)
     GOOGLE_SERVER_CLIENT_ID=your_google_client_id_here
     ```

3. **Sync Gradle**:
   - Bấm vào biểu tượng **Sync Project with Gradle Files** (hình con voi) ở góc trên bên phải Android Studio và chờ quá trình đồng bộ hoàn tất.

4. **Khởi chạy ứng dụng**:
   - Trên thanh công cụ Android Studio, chọn module bạn muốn khởi chạy ở ô chọn Run Configuration:
     - `:app-user` (Ứng dụng cho Khách hàng)
     - `:app-shop` (Ứng dụng cho Chủ quán)
     - `:app-admin` (Ứng dụng cho Quản trị viên)
   - Chọn thiết bị máy ảo (Emulator) hoặc thiết bị thật (qua USB Debugging/Wireless Debugging).
   - Nhấn nút **Run** (hình tam giác xanh) để cài đặt và khởi chạy.

---

### 🔍 Kiểm Tự Kết Nối (Troubleshooting)

- **Lỗi không kết nối được API**: Đảm bảo rằng thiết bị Android của bạn cùng kết nối chung mạng Wi-Fi với máy tính chạy Backend (nếu dùng thiết bị thật) và cấu hình đúng địa chỉ IP trong `local.properties`.

---

## 🔑 Tài khoản đăng nhập:

### Customer:

| Account | Password |
| :--- | :--- |
| `statistics_customer@urmyfood.com` | `Urmyfood@1` |

### Shop:

| Account | Password |
| :--- | :--- |
| `cali_nguyenhue@urmyfood.com` | `Urmyfood@1` |

### Admin:

| Account | Password |
| :--- | :--- |
| `webarebear1000@gmail.com` | `12345678` |
