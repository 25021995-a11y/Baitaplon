# BidCycle — Ứng Dụng Đấu Giá Trực Tuyến

---

## Mục Lục

- [Mô Tả Bài Toán](#1-mô-tả-bài-toán--phạm-vi-hệ-thống)
- [Công Nghệ & Môi Trường](#2-công-nghệ--môi-trường)
- [Cấu Trúc Thư Mục](#3-cấu-trúc-thư-mục--module-chính)
- [Yêu Cầu Cài Đặt](#4-yêu-cầu-cài-đặt)
- [Hướng Dẫn Chạy Chương Trình](#5-hướng-dẫn-chạy-chương-trình)
- [Tài Khoản Mặc Định](#6-tài-khoản-mặc-định)
- [Danh Sách Chức Năng Đã Hoàn Thành](#7-danh-sách-chức-năng-đã-hoàn-thành)
- [Database Schema](#8-database-schema)
- [Xử Lý Sự Cố Thường Gặp](#9-xử-lý-sự-cố-thường-gặp)
- [Tài Liệu & Demo](#10-tài-liệu--demo)

---

## 1. Mô Tả Bài Toán & Phạm Vi Hệ Thống

**BidCycle** là ứng dụng desktop quản lý đấu giá trực tuyến xây dựng bằng **JavaFX**, cho phép nhiều người dùng đồng thời tham gia đấu giá sản phẩm theo thời gian thực. Toàn bộ dữ liệu được đồng bộ qua cơ sở dữ liệu **MySQL** triển khai sẵn trên cloud (Railway), không yêu cầu cài đặt database cục bộ.

Hệ thống phục vụ ba nhóm người dùng:

| Vai trò | Mô tả |
|---|---|
| **Người bán (Seller)** | Đăng sản phẩm lên đấu giá, quản lý kho hàng và theo dõi doanh thu |
| **Người mua (Bidder)** | Duyệt sản phẩm, đặt giá thủ công hoặc tự động, thanh toán ví ảo |
| **Quản trị viên (Admin)** | Quản lý toàn bộ tài khoản, sản phẩm, giao dịch và cấu hình hệ thống |

Phạm vi bài toán bao gồm: đăng ký / đăng nhập, quản lý hồ sơ cá nhân, tạo và quản lý phiên đấu giá, cơ chế đặt giá thủ công và tự động (Auto-Bid), thanh toán ví ảo, thống kê doanh thu và bảng quản trị toàn hệ thống.

---

## 2. Công Nghệ & Môi Trường

| Thành phần | Phiên bản |
|---|---|
| Java (JDK) | **21** |
| JavaFX | 21.0.6 |
| Maven | 3.9+ |
| MySQL Connector/J | 8.3.0 |
| ControlsFX | 11.2.1 |
| JUnit Jupiter | 5.12.1 |
| Kiến trúc | MVC (Model – View – Controller) |
| Giao diện | FXML + CSS (dark theme) |
| Database | MySQL 8.0 — cloud trên **Railway** (không cần cài local) |

> **Lưu ý quan trọng:** Ứng dụng kết nối trực tiếp tới database MySQL đã được triển khai sẵn trên Railway cloud. **Chỉ cần Java 21 và Maven** là đủ để chạy toàn bộ ứng dụng.

---

## 3. Cấu Trúc Thư Mục & Module Chính

```
BidCycle/
├── src/
│   ├── main/
│   │   ├── java/com/bidcycle/
│   │   │   ├── app/                        # Điểm khởi chạy ứng dụng JavaFX
│   │   │   │   ├── Launcher.java           # ⚠ Class khởi chạy (DÙNG CÁI NÀY)
│   │   │   │   └── MainApp.java            # Ứng dụng JavaFX chính (extends Application)
│   │   │   ├── controller/                 # MVC Controllers — xử lý logic giao diện
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── RegisterController.java
│   │   │   │   ├── BidderHomeController.java
│   │   │   │   ├── SellerController.java
│   │   │   │   ├── ProductDetailController.java
│   │   │   │   ├── InventoryController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── AccountController.java
│   │   │   │   └── AdminController.java
│   │   │   ├── model/                      # Domain models
│   │   │   │   ├── User.java
│   │   │   │   ├── Admin.java
│   │   │   │   ├── Product.java            # Bao gồm AutoBidConfig & BidResult
│   │   │   │   ├── BidRecord.java
│   │   │   │   ├── ProductStorage.java
│   │   │   │   └── UserSession.java        # Quản lý phiên đăng nhập
│   │   │   ├── dao/                        # Data Access Objects — truy cập DB qua JDBC
│   │   │   │   ├── DatabaseConnection.java # Singleton kết nối MySQL
│   │   │   │   ├── UserDAO.java
│   │   │   │   ├── ProductDAO.java
│   │   │   │   └── AdminDAO.java
│   │   │   ├── exception/                  # Custom business exceptions
│   │   │   │   ├── BidCycleException.java
│   │   │   │   ├── AuctionClosedException.java
│   │   │   │   ├── AuthenticationException.java
│   │   │   │   └── InvalidBidException.java
│   │   │   └── util/
│   │   │       └── ViewNavigator.java      # Điều hướng giữa các màn hình (Scene switching)
│   │   └── resources/
│   │       ├── com.bidcycle.view/          # FXML layouts & CSS
│   │       │   ├── login.fxml
│   │       │   ├── register.fxml
│   │       │   ├── bidder-home.fxml
│   │       │   ├── seller-home.fxml
│   │       │   ├── product-detail.fxml
│   │       │   ├── inventory.fxml
│   │       │   ├── payment.fxml
│   │       │   ├── dashboard.fxml
│   │       │   ├── account.fxml
│   │       │   ├── admin-panel.fxml
│   │       │   ├── style.css
│   │       │   ├── db_migration_v2.sql
│   │       │   ├── db_migration_v3_autobid.sql
│   │       │   └── db_migration_v4_multi_autobid.sql
│   │       └── images/
│   │           └── iconn.png
│   └── test/
│       └── java/com/bidcycle/model/
│           └── ProductTest.java
├── pom.xml                                 # Maven build & dependencies
├── mvnw                                    # Maven Wrapper — Linux / macOS
├── mvnw.cmd                                # Maven Wrapper — Windows
└── README.md
```

### Vai trò các module

| Module | Trách nhiệm |
|---|---|
| `app` | Khởi tạo Stage JavaFX, load màn hình đăng nhập đầu tiên |
| `controller` | Toàn bộ logic tương tác người dùng theo pattern MVC |
| `model` | Cấu trúc dữ liệu: `User`, `Product`, `BidRecord`, `UserSession`, v.v. |
| `dao` | Tầng truy cập database qua JDBC (Singleton pattern cho `DatabaseConnection`) |
| `exception` | Xử lý ngoại lệ nghiệp vụ: đấu giá đã đóng, bid không hợp lệ, xác thực thất bại |
| `util` | `ViewNavigator` tập trung logic load FXML và chuyển màn hình |

---

## 4. Yêu Cầu Cài Đặt

### Cài Java JDK 21

```bash
# macOS (Homebrew)
brew install openjdk@21
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Ubuntu / Debian
sudo apt-get update && sudo apt-get install -y openjdk-21-jdk

# Fedora / RHEL
sudo dnf install java-21-openjdk-devel

# Windows — tải installer tại:
# https://www.oracle.com/java/technologies/downloads/#java21
# Sau đó thiết lập biến môi trường JAVA_HOME trỏ đến thư mục cài đặt
```

### Cài Maven 3.9+ (tuỳ chọn — có thể dùng Maven Wrapper thay thế)

```bash
# macOS
brew install maven

# Ubuntu / Debian
sudo apt-get install -y maven

# Fedora
sudo dnf install maven

# Windows (Chocolatey)
choco install maven
```

> **Khuyến nghị:** Dùng **Maven Wrapper** (`mvnw` / `mvnw.cmd`) đi kèm sẵn trong project — không cần cài Maven riêng, Maven Wrapper sẽ tự tải đúng phiên bản.

### Kiểm tra môi trường

```bash
java -version    # Kết quả phải hiển thị openjdk 21...
mvn -version     # Nếu cài Maven riêng: Apache Maven 3.9+
```

---

## 5. Hướng Dẫn Chạy Chương Trình

> **Kiến trúc ứng dụng:** BidCycle là ứng dụng **desktop một tiến trình** — không có server và client chạy riêng lẻ. "Server" ở đây là database MySQL đã được deploy sẵn trên Railway cloud; bạn chỉ cần chạy ứng dụng desktop (client) là dùng được ngay.

---

### Bước 1 — Lấy source code

```bash
# Cách 1: Clone qua Git
git clone <repository-url>
cd BidCycle

# Cách 2: Giải nén file ZIP
unzip BidCycle.zip
cd BidCycle
```

---

### Bước 2 — (Không cần thiết) Kiểm tra kết nối Database

Database đã được triển khai sẵn trên Railway. Ứng dụng sẽ tự kết nối khi khởi động. Chỉ cần đảm bảo máy bạn **có kết nối Internet**.

---

### Bước 3 — Build project

Chạy **một trong các lệnh** phù hợp với hệ điều hành:

```bash
# Linux / macOS — dùng Maven Wrapper (không cần cài Maven riêng)
./mvnw clean package -DskipTests

# Windows — Command Prompt hoặc PowerShell
mvnw.cmd clean package -DskipTests

# Nếu đã cài Maven riêng (mọi hệ điều hành)
mvn clean package -DskipTests
```

---

### Bước 4 — Chạy ứng dụng (Client)

**Cách 1 — Maven JavaFX Plugin (khuyến nghị, chạy được trên mọi OS):**

```bash
# Linux / macOS
./mvnw javafx:run

# Windows
mvnw.cmd javafx:run

# Nếu đã cài Maven riêng
mvn javafx:run
```

**Cách 2 — Chạy từ IDE (IntelliJ IDEA / Eclipse / VS Code):**

1. Mở project, chờ IDE index Maven dependencies xong.
2. Tìm file `src/main/java/com/bidcycle/app/Launcher.java`.
3. Chuột phải → **Run 'Launcher.main()'**.

> ⚠️ **Quan trọng:** Phải chạy qua `Launcher.java`, **không** chạy trực tiếp `MainApp.java`. `Launcher` là class trung gian để tránh lỗi JavaFX module-path khi chạy từ IDE.

**Cách 3 — Chạy từ file JAR :**

Cần cài JavaFX SDK rời và cung cấp `--module-path`. Cách đơn giản nhất vẫn là `mvnw javafx:run`. Nếu muốn chạy JAR trực tiếp:

```bash
# Linux / macOS (thay /path/to/javafx-sdk bằng đường dẫn thực tế)
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/BidCycle-2.0-SNAPSHOT.jar

# Windows (thay đường dẫn tương ứng)
java --module-path C:\javafx-sdk\lib ^
     --add-modules javafx.controls,javafx.fxml ^
     -jar target\BidCycle-2.0-SNAPSHOT.jar
```

---

### Thứ tự khởi động

| Thứ tự | Thành phần | Trạng thái | Hành động |
|:---:|---|---|---|
| 1 | **Database (Railway cloud)** | ✅ Đã chạy sẵn | Không cần làm gì |
| 2 | **Ứng dụng desktop (Client)** | Cần khởi động | Chạy `./mvnw javafx:run` |

Sau khi ứng dụng khởi động thành công, màn hình đăng nhập sẽ hiện ra. Ứng dụng tự động kiểm tra và tạo bảng `system_config` cùng tài khoản Admin mặc định nếu chưa tồn tại.

---

## 6. Tài Khoản Mặc Định

| Loại tài khoản | Tên đăng nhập | Mật khẩu | Quyền |
|---|---|---|---|
| Admin mặc định | `admin` | `admin123` | Quản trị viên toàn hệ thống |
| Tài khoản thường | Đăng ký mới | Tự đặt | User (Seller + Bidder) |

---

## 7. Danh Sách Chức Năng Đã Hoàn Thành

### Quản Lý Tài Khoản
- ✅ Đăng ký tài khoản với email, giới tính, ngày tạo
- ✅ Đăng nhập và phân quyền tự động (Admin / User)
- ✅ Xem và chỉnh sửa hồ sơ cá nhân
- ✅ Đổi mật khẩu
- ✅ Quản lý số dư ví ảo (`vir_money`)

### Người Dùng — Vai Trò Người Bán (Seller)
- ✅ Đăng sản phẩm lên đấu giá (tên, giá khởi điểm, thời gian, mô tả, ảnh URL, danh mục)
- ✅ Chỉnh sửa và xóa sản phẩm chưa có lượt bid
- ✅ Quản lý kho hàng (Inventory)
- ✅ Xem lịch sử và kết quả đấu giá của sản phẩm đã đăng
- ✅ Dashboard thống kê doanh số cá nhân

### Người Dùng — Vai Trò Người Mua (Bidder)
- ✅ Duyệt danh sách sản phẩm đang đấu giá
- ✅ Tìm kiếm theo tên và lọc theo danh mục
- ✅ Sắp xếp theo giá, thời gian còn lại
- ✅ Xem chi tiết sản phẩm và biểu đồ lịch sử giá (LineChart)
- ✅ Đặt giá thủ công (Manual Bid)
- ✅ Đặt giá tự động (Auto-Bid) — hệ thống tự động trả giá theo bước đến mức tối đa đã cấu hình
- ✅ Hỗ trợ nhiều người đồng thời đặt Auto-Bid trên cùng một sản phẩm
- ✅ Xem lịch sử các lần đấu giá của bản thân
- ✅ Thanh toán sản phẩm đã thắng bằng ví ảo

### Hệ Thống Đấu Giá Thời Gian Thực
- ✅ Đồng hồ đếm ngược thời gian thực
- ✅ Cập nhật giá và người dẫn đầu theo thời gian thực (polling)
- ✅ Xác định và thông báo người thắng cuộc khi hết giờ
- ✅ Kiểm tra và xử lý ngoại lệ: bid thấp hơn giá hiện tại, đấu giá đã đóng, số dư không đủ

### Admin Dashboard
- ✅ Thống kê tổng quan: tổng user, user đang hoạt động / bị khóa, tổng sản phẩm, đấu giá đang chạy / đã kết thúc, doanh thu
- ✅ Quản lý người dùng: xem danh sách, khóa tài khoản, cấp quyền Admin
- ✅ Quản lý sản phẩm: xem và xóa sản phẩm
- ✅ Tab xử lý tranh chấp (Disputes)
- ✅ Cấu hình tham số hệ thống (System Config)

### Giao Diện & Trải Nghiệm
- ✅ Giao diện hiện đại với CSS tùy chỉnh (dark theme)
- ✅ Điều hướng mượt mà giữa các màn hình qua `ViewNavigator`
- ✅ Validation form phía client trước khi gửi yêu cầu
- ✅ Biểu đồ lịch sử giá đấu giá (JavaFX LineChart)
- ✅ Hiển thị ảnh sản phẩm qua URL

---

## 8. Database Schema

Database nằm trên **Railway cloud**. Script migration tham khảo tại `src/main/resources/com.bidcycle.view/`.

| Bảng | Mô tả |
|---|---|
| `users` | Tài khoản người dùng: username, password, email, role, money, vir_money |
| `products` | Sản phẩm đấu giá: tên, giá, thời gian, người bán, trạng thái |
| `bid_records` | Lịch sử từng lượt đặt giá |
| `auto_bids` | Cấu hình Auto-Bid của từng user trên từng sản phẩm |
| `system_config` | Tham số cấu hình hệ thống của Admin |

---

## 9. Xử Lý Sự Cố Thường Gặp

**Lỗi `UnsupportedClassVersionError`**

Java đang chạy thấp hơn phiên bản 21. Kiểm tra lại bằng `java -version` và cài đúng JDK 21.

**Lỗi `Cannot connect to database` / `Communications link failure`**

Kiểm tra kết nối Internet — ứng dụng cần kết nối tới database cloud trên Railway. Nếu đang dùng VPN hoặc proxy, thử tắt đi.

**Lỗi `Module not found` hoặc JavaFX không tìm thấy khi chạy từ IDE**

Không chạy `MainApp.java` trực tiếp. Phải chạy qua `Launcher.java`. Hoặc dùng lệnh `./mvnw javafx:run` từ terminal — Maven sẽ tự cấu hình module-path.

**Build thất bại / lỗi compile**

```bash
# Làm sạch cache Maven và build lại từ đầu

# Linux / macOS
./mvnw clean compile

# Windows
mvnw.cmd clean compile
```

**Màn hình không hiển thị / giao diện bị trắng**

Đảm bảo đang dùng đúng JDK 21 (không phải JRE). Kiểm tra bằng `java -version` — phải thấy `openjdk 21...`.

---

## 10. Tài Liệu & Demo

- **Báo cáo PDF:** https://drive.google.com/file/d/1t32vY0WKgKNZymWr1Eae8QXezUMPcK2e/view?usp=sharing(#) 
- **Video demo:** [Link video demo](#) *(cập nhật link tại đây)*
