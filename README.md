<img width="3365" alt="image" src="./documentation/image/banner.svg"/>

---

[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://choosealicense.com/licenses/mit/)
![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84.svg?logo=android-studio&logoColor=white)
[![Kotlin](https://img.shields.io/badge/Kotlin-%237F52FF.svg?logo=kotlin&logoColor=white)](#)
[![Java](https://img.shields.io/badge/Java-%23ED8B00.svg?logo=openjdk&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=fff)](#)
[![Supabase](https://img.shields.io/badge/Supabase-3FCF8E?logo=supabase&logoColor=fff)](#)
[![Git](https://img.shields.io/badge/Git-F05032?logo=git&logoColor=fff)](#)
[![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?logo=github-actions&logoColor=white)](#)
[![Render](https://img.shields.io/badge/Render-46E3B7?logo=render&logoColor=000)](#)

---

<div align="center">
  <p><b>Đồ án môn học:</b> SE114 (Nhập môn ứng dụng di động)</p>
  <p><b>Giảng viên hướng dẫn:</b> ThS. Nguyễn Tấn Toàn</p>
  <p><i>University of Information Technology - VNUHCM</i></p>
</div>

<div align="center">

---

| [**📖 Introduction**](#-introduction) | [**✨ Key Features**](#-key-features) | [**🛠 Tech Stack**](#-tech-stack) | [**📁 Project Structure**](#-project-structure) |
|---|---|---|---|
| [**🚀 Installation Guide**](#-installation-guide) | [**📸 Screenshots & Demo**](#-screenshots--demo) | [**👥 Team Members**](#-team-members) | [**📜 License**](#-license) |

</div>


---

## 📖 Introduction

Nhận thấy các nền tảng đặt đồ ăn truyền thống đang bị giới hạn bởi mô hình "danh mục menu tĩnh", **URMYFOOD** không đơn thuần là một ứng dụng giao đồ ăn thông thường. Đây là một giải pháp công nghệ hướng tới việc kiến tạo một hệ sinh thái bán lẻ thức ăn nhanh mang tính xã hội hóa cao (**Social Commerce**).

Đồ án được phát triển với **mục tiêu cốt lõi**: Thu hẹp khoảng cách tương tác giữa người bán (chủ quán nhỏ lẻ) và người mua bằng cách tích hợp trực tiếp luồng Bảng tin (Newsfeed) thời gian thực vào hành trình mua sắm. 

Thay vì bắt buộc người dùng duyệt qua những danh sách khô khan, **URMYFOOD** phá vỡ lối mòn thông qua các điểm nhấn thiết kế:

* 📱 **Cơ chế xuất bản dạng Bài đăng (Feed-based Listing):** Thay vì quản lý menu cứng nhắc, chủ quán phân phối sản phẩm dưới dạng các "status" tương tác cao, kết hợp đa phương tiện (hình ảnh, video) và nội dung mô tả sinh động.
* ⏳ **Chiến lược tạo nhu cầu khẩn cấp (Scarcity & Urgency):** Ứng dụng cơ chế giới hạn số lượng tồn kho trên từng bài đăng. Hệ thống tự động đóng băng giao dịch khi đạt ngưỡng, tạo động lực tâm lý thúc đẩy người dùng ra quyết định nhanh chóng.
* ⚡ **Tối ưu hóa luồng chuyển đổi (Seamless Conversion):** Xóa bỏ rào cản thao tác bằng cách tích hợp hành vi xã hội (Like, Comment, Share) với hành vi giao dịch (Đặt hàng) ngay trên một điểm chạm (Single Interface), giảm thiểu tối đa *friction* cho người dùng.

Về mặt kiến trúc hệ thống, URMYFOOD được xây dựng dựa trên cơ chế phân quyền nghiêm ngặt (**Role-Based Access Control - RBAC**), phục vụ liền mạch 3 lớp đối tượng:
1.  **Người dùng (User):** Trải nghiệm mua sắm kết hợp mạng xã hội.
2.  **Chủ quán (Shop Owner):** Đăng bài, quản lý đơn hàng và tương tác trực tiếp.
3.  **Quản trị viên (Admin):** Vận hành, kiểm duyệt nội dung và quản lý hệ thống.

---

## ✨ Key Features
Hệ thống **URMYFOOD** được phân rã thành các module chức năng chuyên biệt, đáp ứng kịch bản sử dụng (Use-case) cụ thể của 3 nhóm quyền hạn (Roles) trong hệ sinh thái:

### 👤 1. Người dùng (User) - Trải nghiệm mua sắm liền mạch
* **Newsfeed-Driven Discovery (Khám phá qua Bảng tin):** Lướt xem các món ăn thông qua giao diện Bảng tin (Feed) cập nhật theo thời gian thực thay vì giao diện danh mục truyền thống.
* **One-Touch Ordering (Đặt hàng một chạm):** Hiện thực hóa "Seamless Conversion" bằng cách cho phép người dùng thêm vào giỏ hàng hoặc thanh toán ngay tại bài đăng (Post) đang tương tác mà không cần chuyển hướng trang.
* **Social Interactions (Tương tác xã hội):** Tích hợp các hành vi quen thuộc như Thả tim (Like), Bình luận (Comment) và Chia sẻ (Share) trên từng bài đăng, tạo không gian trao đổi trực tiếp với chủ quán và cộng đồng.
* **Real-time Order Tracking (Theo dõi đơn hàng):** Giám sát trạng thái đơn hàng (Chờ duyệt, Đang giao, Hoàn thành) và quản lý lịch sử giao dịch cá nhân.

### 🏪 2. Chủ quán (Shop Owner) - Quản trị nội dung & Vận hành
* **Feed-Based Listing Management (Quản lý phân phối nội dung):** Cho phép xuất bản món ăn dưới dạng các bài đăng (Status) đa phương tiện (Hình ảnh, Tiêu đề, Mô tả chi tiết).
* **Scarcity & Inventory Control (Kiểm soát mức độ khan hiếm):** Thiết lập số lượng (Stock) giới hạn cho từng bài đăng cụ thể. Hệ thống tự động kích hoạt trạng thái "Sold Out" (Vô hiệu hóa nút đặt hàng) khi đạt ngưỡng tồn kho, tối ưu hóa chiến lược FOMO (Fear Of Missing Out) đối với người mua.
* **Order Fulfillment (Xử lý đơn hàng):** Giao diện tiếp nhận, duyệt hoặc từ chối đơn hàng từ người dùng một cách nhanh chóng.
* **Direct Customer Engagement (Tương tác khách hàng):** Quản lý phản hồi, trực tiếp giải đáp thắc mắc của khách hàng ngay trong phần bình luận của bài đăng, giúp tăng độ tin cậy và tỷ lệ chuyển đổi.

### 🛡️ 3. Quản trị viên (System Admin) - Điều phối & Kiểm soát
* **RBAC & Identity Management (Quản lý định danh & Phân quyền):** Phê duyệt, cấp quyền hoặc thu hồi quyền truy cập của các tài khoản Chủ quán (Shop Owner). Xử lý các tài khoản vi phạm.
* **Content Moderation (Kiểm duyệt nội dung):** Giám sát luồng dữ liệu trên Newsfeed, kiểm duyệt bài đăng và bình luận để đảm bảo tính an toàn, tuân thủ tiêu chuẩn cộng đồng của nền tảng.
* **Global Dashboard (Bảng điều khiển tổng quan):** Cung cấp các số liệu thống kê (Metrics) về hiệu suất hoạt động của toàn hệ thống (Tổng số người dùng, Lượng giao dịch, Tần suất tương tác).

---

## 🛠 Tech Stack

Kiến trúc hệ thống của URMYFOOD được thiết kế phân lớp rõ ràng, kết hợp giữa nền tảng Native Mobile và Backend mạnh mẽ. Dưới đây là các công nghệ lõi được áp dụng để giải quyết bài toán hiệu năng và trải nghiệm người dùng:

### 📱 1. Client-side: Mobile Application
Ứng dụng hướng tới trải nghiệm mượt mà, tối ưu phần cứng thiết bị thông qua việc sử dụng công nghệ Native:
* **Ngôn ngữ lập trình:** Kotlin.
* **Nền tảng cốt lõi:** Android SDK (Native).
* **Giao diện người dùng (UI):** Xây dựng hoàn toàn bằng Jetpack Compose, tuân thủ nghiêm ngặt hệ thống ngôn ngữ thiết kế Material Design 3.

### ⚙️ 2. Server-side: Backend & Database
Hệ thống xử lý nghiệp vụ được xây dựng với mục tiêu chịu tải cao và tính toàn vẹn dữ liệu:
* **Ngôn ngữ & Framework:** Nền tảng Java kết hợp với Spring Boot.
* **Hệ quản trị Cơ sở dữ liệu:** Supabase (vận hành trên lõi PostgreSQL).
* **Kiến trúc Giao tiếp:** Các service trao đổi dữ liệu qua chuẩn RESTful API, định dạng JSON.
* **Bảo mật & Xử lý đồng thời (Concurrency):** Mật khẩu được băm (hash) bằng thuật toán bcrypt, xác thực người dùng qua JWT (Access/Refresh Token). Hệ thống ứng dụng Redis để tối ưu hóa hiệu năng (caching) và giải quyết bài toán *race condition* khi cập nhật số lượng tồn kho (Inventory) theo thời gian thực.

### 🧠 3. Hệ thống Thuật toán nâng cao (Advanced Modules)
Để hiện thực hóa mô hình Social Commerce thông minh, hệ thống tích hợp các luồng xử lý dữ liệu phức tạp:
* **Hệ thống Gợi ý (Recommendation System):** Ứng dụng mô hình Linear Regression để phân tích hành vi người dùng, từ đó đề xuất món ăn cá nhân hóa trực tiếp trên Newsfeed.
* **Tìm kiếm ngữ nghĩa (Semantic Search):** Xử lý truy vấn đa dạng, cho phép người dùng tìm kiếm món ăn bằng ngôn ngữ tự nhiên.

### 🚀 4. Triển khai & Quản lý Chất lượng (DevOps & QA)
Quy trình phát triển tuân thủ các nguyên tắc CI/CD và kiểm soát chất lượng mã nguồn tự động:
* **Môi trường Triển khai (Hosting):** Hệ thống được deploy trên nền tảng Render.
* **Tích hợp liên tục (Continuous Integration):** Tự động hóa quy trình kiểm thử và build thông qua GitHub Actions.
* **Code Quality Assurance:** Đảm bảo source code đồng nhất và sạch (clean code) bằng việc áp dụng các bộ linter khắt khe: Checkstyle (dành cho Java), Detekt (dành cho Kotlin) và Husky (cho các pre-commit hooks).

---

## 📁 Project Structure

```
📁URMYFOOD
└── 📁.github/workflows
└── 📁.husky
└── 📁backend
└── 📁documentation
└── 📁frontend
    └── 📁app-admin
    └── 📁app-shop
    └── 📁app-user
├── .gitignore
├── LICENSE
└── README.md
```

---

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
   - Nội dung file `.env` mẫu để chạy dự án (bạn có thể copy trực tiếp đoạn này để chạy ngay với database của nhóm):
     ```env
     # Database Configuration
     DB_URL=jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:5432/postgres
     DB_USERNAME=postgres.yfdhsoilpawzhxbftlbe
     DB_PASSWORD=urmyfood.uit.vnuhcm

     # Security Configuration
     JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
     JWT_EXPIRATION=86400000
     JWT_REFRESH_EXPIRATION=604800000

     # Social Login & Gmail API Configuration
     GOOGLE_CLIENT_ID=your_google_client_id_here
     GOOGLE_CLIENT_SECRET=your_google_client_secret_here
     GOOGLE_REFRESH_TOKEN=your_google_refresh_token_here

     SUPABASE_URL=https://yfdhsoilpawzhxbftlbe.supabase.co
     SUPABASE_ANON_KEY=sb_publishable_JmmDh_osEt0VgNJSjU2EJA_cR5GMS8I
     SUPABASE_STORAGE_BUCKET=urmyfood-bucket
     SUPABASE_PROFILE_IMAGE_MAX_SIZE=1048576
     SUPABASE_PROFILE_IMAGE_ALLOWED_TYPES=image/jpeg,image/png,image/gif
     SUPABASE_SERVICE_ROLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlmZGhzb2lscGF3emh4YmZ0bGJlIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3NzcwOTYyOSwiZXhwIjoyMDkzMjg1NjI5fQ.MireNkDJnja6KmKh3Eh2597U8258HcI0p5wpdve5JaM

     PAYOS_CLIENT_ID=8c50ef7d-bd86-4a7f-827b-2f32cc865133
     PAYOS_API_KEY=8f8108e8-9b77-4b96-9085-8dc73b11b2ca
     PAYOS_CHECKSUM_KEY=2fe6e17887634e26da5cbe83842e5d22b6244e5a70c8b9c7abc3efe9584de359
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

## 📸 Screenshots & Demo

Dưới đây là hình ảnh giao diện màn hình chính (Home Screen) của 3 ứng dụng trong hệ sinh thái **URMYFOOD**:

<div align="center">
  <table>
    <tr>
      <td align="center" valign="top" width="50%">
        <b>📱 Ứng dụng Khách hàng (User App)</b><br/><br/>
        <img src="./documentation/image/user_home.png" width="280" alt="User App Home Screen"/>
      </td>
      <td align="center" valign="top" width="50%">
        <b>🏪 Ứng dụng Chủ quán (Shop App)</b><br/><br/>
        <img src="./documentation/image/shop_home.png" width="280" alt="Shop App Home Screen"/>
      </td>
    </tr>
    <tr>
      <td align="center" valign="top" colspan="2">
        <br/>
        <b>💻 Ứng dụng Quản trị viên (Admin App)</b><br/><br/>
        <img src="./documentation/image/admin_home.png" width="700" alt="Admin App Dashboard"/>
      </td>
    </tr>
  </table>
</div>

---

## 👥 Team Members

| No. | Full Name | Student ID | Role |
| --- | --- | --- | --- |
|1| Nguyễn Đại Hưng       | 24520601 | Team Leader, Documentation, Backend |
|2| Trần Nhật Duy         | 24520403 | UI/UX Designer, Frontend |
|3| Tô Công Hữu Nhân      | 24521238 | Backend, QA/Tester |
|4| Nguyễn Cao Xuân Trung | 24521885 | Backend, QA/Tester |

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).