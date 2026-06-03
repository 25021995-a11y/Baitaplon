# BidCycle — Ứng Dụng Đấu Giá Trực Tuyến

## Mô Tả Bài Toán & Phạm Vi Hệ Thống

**BidCycle** là ứng dụng desktop quản lý đấu giá trực tuyến xây dựng bằng JavaFX, cho phép nhiều người dùng cùng tham gia đấu giá sản phẩm theo thời gian thực thông qua cơ sở dữ liệu MySQL dùng chung trên cloud.

Hệ thống phục vụ ba nhóm người dùng:

- **Người dùng thông thường (User)** — Seller có thể đăng sản phẩm lên đấu giá, Bidder có thể tham gia trả giá trên sản phẩm của người khác.
- **Quản trị viên (Admin)** — quản lý toàn bộ tài khoản, sản phẩm, giao dịch và cấu hình hệ thống.

Phạm vi bài toán bao gồm:
- Đăng ký/đăng nhập
- Quản lý hồ sơ cá nhân
- Tạo và quản lý sản phẩm đấu giá
- Cơ chế đấu giá thủ công và tự động (Auto-Bid)
- Thanh toán ảo
- Thống kê doanh thu
- Bảng quản trị toàn hệ thống.

---

## Công Nghệ & Môi Trường

| Thành phần | Phiên bản |
|---|---|
| Java (JDK) | 21 |
| JavaFX | 21.0.6 |
| Maven | 3.9+ |
| MySQL Connector/J | 8.3.0 |
| ControlsFX | 11.2.1 |
| JUnit Jupiter | 5.12.1 |
| Database | MySQL 8.0 (cloud — Railway) |

> **Lưu ý:** Ứng dụng kết nối tới database MySQL đã được triển khai sẵn trên Railway cloud. **Không cần cài đặt MySQL local.** Chỉ cần Java 21 và Maven là đủ để chạy.

### Yêu Cầu Cài Đặt

**1. Cài Java JDK 21:**

```bash
# macOS (Homebrew)
brew install openjdk@21

# Ubuntu / Debian
sudo apt-get install openjdk-21-jdk

# Fedora / RHEL
sudo dnf install java-21-openjdk-devel

# Windows
# Tải từ https://www.oracle.com/java/technologies/downloads/
# Sau đó thiết lập biến môi trường JAVA_HOME
```

**2. Cài Maven 3.9+:**

```bash
# macOS
brew install maven

# Ubuntu / Debian
sudo apt-get install maven

# Fedora
sudo dnf install maven

# Windows (Chocolatey)
choco install maven
```

Hoặc dùng **Maven Wrapper** đi kèm trong project — không cần cài Maven riêng (xem Bước 4).

**3. Kiểm tra môi trường:**

```bash
java -version   # Phải là 21+
mvn -version    # Phải là 3.9+ 
```

---

## Cấu Trúc Thư Mục

```
BidCycle/
├── src/
│   ├── main/
│   │   ├── java/com/bidcycle/
│   │   │   ├── app/                   # Entry point ứng dụng JavaFX
│   │   │   │   ├── Launcher.java      # Class khởi chạy (tránh lỗi module-path)
│   │   │   │   └── MainApp.java       # Ứng dụng JavaFX chính
│   │   │   ├── controller/            # MVC Controllers (xử lý logic giao diện)
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
│   │   │   ├── model/                 # Domain models
│   │   │   │   ├── User.java
│   │   │   │   ├── Admin.java
│   │   │   │   ├── Product.java
│   │   │   │   ├── BidRecord.java
│   │   │   │   ├── ProductStorage.java
│   │   │   │   └── UserSession.java
│   │   │   ├── dao/                   # Data Access Objects (truy cập DB)
│   │   │   │   ├── DatabaseConnection.java
│   │   │   │   ├── UserDAO.java
│   │   │   │   ├── ProductDAO.java
│   │   │   │   └── AdminDAO.java
│   │   │   ├── exception/             # Custom exceptions
│   │   │   │   ├── BidCycleException.java
│   │   │   │   ├── AuctionClosedException.java
│   │   │   │   ├── AuthenticationException.java
│   │   │   │   └── InvalidBidException.java
│   │   │   └── util/
│   │   │       └── ViewNavigator.java # Điều hướng giữa các màn hình
│   │   └── resources/
│   │       ├── com.bidcycle.view/     # FXML layouts & CSS
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
├── pom.xml                            # Maven build & dependencies
├── mvnw                               # Maven Wrapper (Linux/macOS)
├── mvnw.cmd                           # Maven Wrapper (Windows)
└── README.md
```

### Các Module Chính

- **`app`** — Khởi tạo Stage JavaFX, load màn hình đầu tiên.
- **`controller`** — Xử lý toàn bộ logic tương tác người dùng theo pattern MVC.
- **`model`** — Định nghĩa cấu trúc dữ liệu: User, Product, BidRecord, v.v.
- **`dao`** — Tầng truy cập cơ sở dữ liệu qua JDBC (Singleton pattern cho DatabaseConnection).
- **`exception`** — Xử lý ngoại lệ nghiệp vụ (đấu giá đã đóng, bid không hợp lệ…).
- **`util`** — ViewNavigator quản lý điều hướng màn hình.

---

## Hướng Dẫn Chạy Chương Trình

Ứng dụng BidCycle là **ứng dụng desktop một tiến trình** — không có server/client tách biệt. Chỉ cần clone project và chạy là dùng được ngay, vì database đã có sẵn trên cloud.

### Bước 1: Lấy Source Code

```bash
# Clone qua Git
git clone <repository-url>
cd BidCycle

# Hoặc giải nén file ZIP tải về
unzip BidCycle.zip
cd BidCycle
```

### Bước 2: Build Project

```bash
# Linux / macOS — dùng Maven Wrapper 
./mvnw clean package -DskipTests

# Windows — dùng Maven Wrapper
mvnw.cmd clean package -DskipTests

# Dùng Maven đã cài sẵn 
mvn clean package -DskipTests
```

### Bước 3: Chạy Ứng Dụng

**Cách 1 — Maven JavaFX plugin :**

```bash
# Linux / macOS
./mvnw javafx:run

# Windows
mvnw.cmd javafx:run

# Hoặc dùng Maven đã cài sẵn
mvn javafx:run
```

**Cách 2 — Chạy trực tiếp từ IDE:**

1. Mở project trong IntelliJ IDEA hoặc Eclipse.
2. Chuột phải vào `src/main/java/com/bidcycle/app/Launcher.java` → **Run 'Launcher.main()'**.

> ⚠️ **Không chạy `MainApp.java` trực tiếp** — phải chạy qua `Launcher.java` để tránh lỗi JavaFX module-path khi chạy từ IDE.

**Cách 3 — Chạy từ file JAR :**

JavaFX yêu cầu cung cấp đường dẫn module. Cách đơn giản nhất vẫn là dùng `mvn javafx:run` ở trên. Nếu muốn chạy JAR trực tiếp, cần cài JavaFX SDK riêng và thêm `--module-path`:

```bash
# Linux / macOS (thay /path/to/javafx-sdk bằng đường dẫn thực tế)
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml \
     -jar target/BidCycle-2.0-SNAPSHOT.jar

# Windows (thay đường dẫn tương ứng)
java --module-path C:\javafx-sdk\lib --add-modules javafx.controls,javafx.fxml ^
     -jar target\BidCycle-2.0-SNAPSHOT.jar
```

### Bước 4: Đăng Nhập

Sau khi ứng dụng khởi động, màn hình đăng nhập hiện ra:

| Tài khoản | Tên đăng nhập | Mật khẩu | Quyền |
|---|---|---|---|
| Admin mặc định | `admin` | `admin123` | Quản trị viên |
| Tài khoản thường | Đăng ký mới | Tự đặt | User |

---

## Danh Sách Chức Năng Đã Hoàn Thành

### Quản Lý Tài Khoản
- [x] Đăng ký tài khoản (với email, giới tính, ngày tạo)
- [x] Đăng nhập và phân quyền (Admin / User)
- [x] Xem và chỉnh sửa hồ sơ cá nhân
- [x] Đổi mật khẩu
- [x] Quản lý số dư ví ảo (`vir_money`)

### Chức Năng Người Dùng — Vai Trò Người Bán
- [x] Đăng sản phẩm lên đấu giá (tên, giá khởi điểm, thời gian, mô tả, ảnh, danh mục)
- [x] Chỉnh sửa và xóa sản phẩm chưa có lượt bid
- [x] Quản lý kho hàng (Inventory)
- [x] Xem lịch sử và kết quả đấu giá của sản phẩm đã đăng
- [x] Dashboard thống kê doanh số cá nhân

### Chức Năng Người Dùng — Vai Trò Người Mua
- [x] Duyệt danh sách sản phẩm đang đấu giá
- [x] Tìm kiếm theo tên và lọc theo danh mục
- [x] Sắp xếp theo giá, thời gian còn lại
- [x] Xem chi tiết sản phẩm, biểu đồ lịch sử giá
- [x] Đặt giá thủ công (Manual Bid)
- [x] Đặt giá tự động (Auto-Bid) — hệ thống tự động trả giá theo bước đến mức tối đa
- [x] Hỗ trợ nhiều người cùng đặt Auto-Bid trên một sản phẩm
- [x] Xem lịch sử các lần đấu giá của bản thân
- [x] Thanh toán sản phẩm đã thắng từ ví ảo

### Hệ Thống Đấu Giá
- [x] Đồng hồ đếm ngược thời gian thực
- [x] Cập nhật giá và người dẫn đầu theo thời gian thực (polling)
- [x] Xác định và thông báo người thắng khi hết giờ
- [x] Kiểm tra và xử lý ngoại lệ (bid thấp hơn giá hiện tại, đấu giá đã đóng, số dư không đủ)

### Admin Dashboard
- [x] Thống kê tổng quan: tổng user, user đang hoạt động/bị khóa, tổng sản phẩm, đấu giá đang chạy/đã kết thúc, doanh thu
- [x] Quản lý người dùng: duyệt, khóa tài khoản, cấp quyền Admin
- [x] Quản lý sản phẩm: xem, xóa sản phẩm
- [x] Tab xử lý tranh chấp (Disputes)
- [x] Cấu hình tham số hệ thống (Config)

### Giao Diện & Trải Nghiệm
- [x] Giao diện hiện đại với CSS tùy chỉnh (dark theme)
- [x] Điều hướng mượt mà giữa các màn hình qua ViewNavigator
- [x] Validation form phía client
- [x] Biểu đồ đường lịch sử giá đấu giá (LineChart)
- [x] Hiển thị ảnh sản phẩm

---

## Database Schema

Database nằm trên cloud (Railway). Các bảng chính:

| Bảng | Mô tả |
|---|---|
| `users` | Tài khoản người dùng (username, password, email, role, money, vir_money) |
| `products` | Sản phẩm đấu giá (tên, giá, thời gian, người bán, trạng thái) |
| `bid_records` | Lịch sử từng lượt đấu giá |
| `auto_bids` | Cấu hình Auto-Bid của từng user trên từng sản phẩm |
| `system_config` | Cấu hình tham số hệ thống của Admin |

Script migration tham khảo tại:
- `src/main/resources/com.bidcycle.view/db_migration_v2.sql`
- `src/main/resources/com.bidcycle.view/db_migration_v3_autobid.sql`
- `src/main/resources/com.bidcycle.view/db_migration_v4_multi_autobid.sql`

---

## Xử Lý Sự Cố Thường Gặp

**Lỗi `UnsupportedClassVersionError`:**
Java đang chạy thấp hơn 21. Kiểm tra lại: `java -version`.

**Lỗi `Cannot connect to database` / `Communications link failure`:**
Kiểm tra kết nối Internet — ứng dụng cần kết nối tới database cloud trên Railway.

**Lỗi `Module not found` hoặc JavaFX không tìm thấy:**
Dùng đúng lệnh `./mvnw javafx:run` (hoặc `mvnw.cmd javafx:run` trên Windows). Maven sẽ tự tải JavaFX qua dependency.

**Ứng dụng không build được:**
```bash
# Làm sạch cache và build lại
./mvnw clean compile   # Linux/macOS
mvnw.cmd clean compile # Windows
```

---

## Tài Liệu & Demo

- **Báo cáo PDF:** [Link báo cáo](https://example.com/report.pdf)
- **Video demo:** [Link video demo](https://example.com/demo.mp4) 
