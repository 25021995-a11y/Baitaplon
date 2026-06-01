# BidCycle - Ứng Dụng Đấu Giá Trực Tuyến

## 📋 Mô Tả Dự Án

**BidCycle** là ứng dụng quản lý hệ thống đấu giá trực tuyến cho phép người dùng:
- **Người bán (Seller)**: Đưa sản phẩm lên đấu giá, quản lý kho hàng, xem kết quả đấu giá
- **Người mua (Bidder)**: Tham gia đấu giá, xem sản phẩm, thanh toán
- **Quản trị viên (Admin)**: Quản lý người dùng, sản phẩm và giao dịch

### Phạm Vi Hệ Thống
- Quản lý tài khoản người dùng (Đăng ký, Đăng nhập, Quản lý hồ sơ)
- Quản lý sản phẩm đấu giá (Tạo, sửa, xóa)
- Hệ thống đấu giá real-time với xác định giá trị cuối cùng
- Quản lý thanh toán và đơn hàng
- Dashboard thống kê cho người bán và quản trị viên

---

## 🛠️ Công Nghệ & Yêu Cầu

### Công Nghệ Sử Dụng
| Thành Phần | Phiên Bản |
|-----------|---------|
| **Java** | 21 |
| **JavaFX** | 21.0.6 |
| **Maven** | 3.9+ |
| **MySQL** | 8.0+ |
| **ControlsFX** | 11.2.1 |
| **MySQL Connector** | 8.3.0 |
| **JUnit** | 5.12.1 |

### Yêu Cầu Cài Đặt

**1. Yêu cầu hệ thống:**
- Java JDK 21 hoặc cao hơn
- Maven 3.9 hoặc cao hơn
- MySQL Server 8.0 hoặc cao hơn
- RAM: 4GB (tối thiểu)
- Dung lượng ổ cứng: 500MB

**2. Cài đặt Java JDK 21:**

| Hệ điều hành | Hướng dẫn |
|-------------|---------|
| **Windows** | Tải từ [oracle.com](https://www.oracle.com/java/technologies/downloads/) → Cài đặt → Thiết lập JAVA_HOME |
| **macOS** | `brew install openjdk@21` |
| **Linux (Ubuntu/Debian)** | `sudo apt-get install openjdk-21-jdk` |
| **Linux (Fedora/RHEL)** | `sudo dnf install java-21-openjdk-devel` |

**3. Cài đặt Maven:**

```bash
# Windows (sử dụng Chocolatey)
choco install maven

# macOS
brew install maven

# Linux Ubuntu/Debian
sudo apt-get install maven

# Linux Fedora
sudo dnf install maven
```

**4. Cài đặt MySQL:**

| Hệ điều hành | Hướng dẫn |
|-------------|---------|
| **Windows** | Tải từ [mysql.com](https://dev.mysql.com/downloads/mysql/) → Cài đặt → Khởi động MySQL Service |
| **macOS** | `brew install mysql` → `brew services start mysql` |
| **Linux** | `sudo apt-get install mysql-server` (Ubuntu) |

**5. Kiểm tra cài đặt:**

```bash
# Kiểm tra Java
java -version

# Kiểm tra Maven
mvn -version

# Kiểm tra MySQL
mysql --version
```

---

## 📁 Cấu Trúc Dự Án

```
BidCycle/
├── src/
│   └── main/
│       ├── java/
│       │   ├── com/bidcycle/
│       │   │   ├── app/                    # Điểm khởi động ứng dụng
│       │   │   │   ├── Launcher.java       # Entry point (tránh lỗi module-path)
│       │   │   │   └── MainApp.java        # Ứng dụng JavaFX chính
│       │   │   ├── controller/             # MVC Controllers
│       │   │   │   ├── LoginController.java
│       │   │   │   ├── RegisterController.java
│       │   │   │   ├── BidderHomeController.java
│       │   │   │   ├── SellerController.java
│       │   │   │   ├── ProductDetailController.java
│       │   │   │   ├── InventoryController.java
│       │   │   │   ├── PaymentController.java
│       │   │   │   ├── DashboardController.java
│       │   │   │   └── AccountController.java
│       │   │   ├── model/                  # Model classes
│       │   │   │   ├── User.java           # Người dùng
│       │   │   │   ├── Product.java        # Sản phẩm
│       │   │   │   ├── BidRecord.java      # Ghi chép đấu giá
│       │   │   │   ├── UserSession.java    # Phiên đăng nhập
│       │   │   │   └── Admin.java
│       │   │   ├── dao/                    # Data Access Objects
│       │   │   │   ├── DatabaseConnection.java
│       │   │   │   ├── UserDAO.java        # Quản lý người dùng
│       │   │   │   └── ProductDAO.java     # Quản lý sản phẩm
│       │   │   └── util/
│       │   │       └── ViewNavigator.java  # Điều hướng View
│       │   └── module-info.java
│       └── resources/
│           ├── com/bidcycle/view/          # FXML files (giao diện)
│           │   ├── login.fxml
│           │   ├── register.fxml
│           │   ├── bidder-home.fxml
│           │   ├── seller-home.fxml
│           │   ├── product-detail.fxml
│           │   ├── inventory.fxml
│           │   ├── payment.fxml
│           │   ├── dashboard.fxml
│           │   ├── account.fxml
│           │   ├── style.css                # CSS styling
│           │   └── db_migration_v2.sql      # Database schema
│           └── images/
│               └── iconn.png
├── pom.xml                                  # Maven configuration
├── mvnw / mvnw.cmd                          # Maven wrapper
└── README.md
```

### Các Module Chính
1. **Model**: Định nghĩa cấu trúc dữ liệu (User, Product, BidRecord)
2. **DAO**: Xử lý truy cập database (DatabaseConnection, UserDAO, ProductDAO)
3. **Controller**: Logic xử lý giao diện (MVC pattern)
4. **Util**: Các tiện ích (ViewNavigator, UserSession)
5. **Resources**: FXML layouts và CSS styling

---

## 🚀 Hướng Dẫn Chạy Ứng Dụng

### Bước 1: Clone/Tải Dự Án

```bash
# Nếu có Git
git clone <repository-url>
cd Baitaplon_final

# Hoặc tải file ZIP và giải nén
```

### Bước 2: Cấu Hình Database

**2.1. Khởi động MySQL Server:**

```bash
# Windows (Command Prompt - Admin)
net start MySQL80

# macOS
brew services start mysql

# Linux (Ubuntu/Debian)
sudo systemctl start mysql

# Linux (Fedora/RHEL)
sudo systemctl start mysqld
```

**2.2. Tạo database và chạy migration:**

```bash
# Kết nối MySQL
mysql -u root -p

# Nhập password của MySQL (mặc định có thể không có password)
```

Trong MySQL CLI:
```sql
CREATE DATABASE bidcycle;
USE bidcycle;
SOURCE src/main/resources/com/bidcycle/view/db_migration_v2.sql;
```

**2.3. Cấu hình kết nối database:**

Mở file `DatabaseConnection.java` (trong `dao/`) và kiểm tra thông tin kết nối:
```java
// Ví dụ cấu hình
String url = "jdbc:mysql://localhost:3306/bidcycle";
String user = "root";
String password = "your_password";  // Thay đổi nếu cần
```

### Bước 3: Build Dự Án

```bash
# Sử dụng Maven wrapper (khuyên dùng)
# Windows
mvnw.cmd clean package

# macOS / Linux
./mvnw clean package

# HOẶC sử dụng Maven cài sẵn
mvn clean package
```

### Bước 4: Chạy Ứng Dụng

#### Option 1: Sử dụng Maven (Khuyên dùng trên mọi hệ điều hành)

```bash
# Windows
mvnw.cmd javafx:run

# macOS / Linux
./mvnw javafx:run

# HOẶC sử dụng Maven cài sẵn
mvn javafx:run
```

#### Option 2: Chạy từ JAR (sau khi build)

```bash
# Windows
java -jar target/BidCycle-2.0-SNAPSHOT.jar

# macOS / Linux
java -jar target/BidCycle-2.0-SNAPSHOT.jar
```

#### Option 3: Chạy từ IDE (IntelliJ IDEA)
1. Mở project trong IntelliJ IDEA
2. Chuột phải vào `Launcher.java` → Run 'Launcher.main()'
3. Hoặc sử dụng IDE Maven tool: Maven → Plugins → javafx → javafx:run

---

## 📝 Danh Sách Chức Năng Đã Hoàn Thành

### ✅ Quản Lý Tài Khoản
- [x] Đăng ký tài khoản người dùng (Seller/Bidder)
- [x] Đăng nhập hệ thống
- [x] Xem/Sửa hồ sơ cá nhân
- [x] Quản lý mật khẩu
- [x] Quản lý quyền người dùng

### ✅ Chức Năng Seller (Người Bán)
- [x] Tạo sản phẩm đấu giá
- [x] Chỉnh sửa thông tin sản phẩm
- [x] Xóa sản phẩm
- [x] Quản lý kho hàng (Inventory)
- [x] Xem lịch sử đấu giá
- [x] Xem kết quả đấu giá
- [x] Dashboard thống kê doanh số

### ✅ Chức Năng Bidder (Người Mua)
- [x] Duyệt danh sách sản phẩm
- [x] Xem chi tiết sản phẩm
- [x] Tham gia đấu giá
- [x] Xem lịch sử các lần đấu giá của mình
- [x] Thanh toán sản phẩm đã thắng
- [x] Quản lý giỏ hàng/Đơn hàng

### ✅ Hệ Thống Đấu Giá
- [x] Real-time bidding
- [x] Xác định người thắng đấu giá
- [x] Tính toán giá cao nhất
- [x] Lịch sử bids chi tiết
- [x] Thông báo khi bị trả giá

### ✅ Quản Lý Thanh Toán
- [x] Xử lý thanh toán sản phẩm
- [x] Lịch thanh toán
- [x] Xác nhận thanh toán

### ✅ Admin Dashboard
- [x] Thống kê toàn hệ thống
- [x] Quản lý người dùng
- [x] Quản lý sản phẩm
- [x] Báo cáo giao dịch

### ✅ Giao Diện & UX
- [x] Giao diện Modern với CSS styling
- [x] Navigation mượt mà giữa các màn hình
- [x] Form validation
- [x] Xử lý lỗi thân thiện với người dùng
- [x] Icon và hình ảnh (BidCycle logo)

---

## 📊 Danh Sách Database Schema

**Các bảng chính:**
- `users` - Thông tin người dùng
- `products` - Thông tin sản phẩm đấu giá
- `bid_records` - Ghi chép các lần đấu giá
- `payments` - Thông tin thanh toán
- `inventory` - Kho hàng

Chi tiết schema xem tại: `src/main/resources/com/bidcycle/view/db_migration_v2.sql`

---

## 🐛 Xử Lý Sự Cố Thường Gặp

### Lỗi: "Module not found"
**Giải pháp**: Đảm bảo `module-info.java` được compile đúng
```bash
mvn clean compile
```

### Lỗi: "Cannot connect to database"
**Giải pháp**: 
- Kiểm tra MySQL server đang chạy
- Kiểm tra thông tin kết nối trong `DatabaseConnection.java`
- Đảm bảo database `bidcycle` được tạo

### Lỗi: "JavaFX not found"
**Giải pháp**: Maven sẽ tự download, chạy:
```bash
mvnw.cmd clean dependency:resolve
```

### Ứng dụng không khởi động
**Giải pháp**: Kiểm tra Java version
```bash
java -version  # Phải là 21+
```

---

## 📚 Công Nghệ Chi Tiết

- **JavaFX 21**: Giao diện desktop hiện đại
- **FXML**: Markup language cho JavaFX UI
- **MVC Pattern**: Architecture rõ ràng (Model-View-Controller)
- **Module System (Java 9+)**: Quản lý package an toàn
- **MySQL JDBC**: Kết nối database
- **Maven**: Build tool và dependency management

---

## 📄 Tài Liệu & Demo

### Link Báo Cáo PDF
- [Link Báo Cáo](https://example.com/report.pdf) *(Thay bằng link thực tế)*

### Video Demo
- [Demo Video Application](https://example.com/demo.mp4) *(Thay bằng link thực tế)*
- [Setup & Installation Guide](https://example.com/installation.mp4) *(Thay bằng link thực tế)*

---

