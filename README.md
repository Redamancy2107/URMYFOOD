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
*Sẽ bổ sung sau khi hoàn thành xong version đầu tiên của dự án*

---

## 📸 Screenshots & Demo
*Sẽ bổ sung sau khi hoàn thành xong version đầu tiên của dự án*

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