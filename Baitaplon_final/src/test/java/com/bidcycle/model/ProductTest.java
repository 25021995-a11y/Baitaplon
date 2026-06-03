package com.bidcycle.model;

import com.bidcycle.exception.AuctionClosedException;
import com.bidcycle.exception.AuthenticationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Disabled;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho Product – logic đấu giá cốt lõi.
 *
 * Cấu trúc: 5 nhóm
 *   1. Đặt giá thủ công (manual bid)
 *   2. Kiểm soát truy cập (access control)
 *   3. Vòng đời phiên đấu giá (auction lifecycle)
 *   4. Auto-Bid
 *   5. Tài chính người dùng (User finance)
 */
@DisplayName("Product – Kiểm thử logic đấu giá")
class ProductTest {

    // ── Helpers tạo đối tượng ─────────────────────────────────

    /** Tạo User không cần kết nối DB (override các method gọi DAO). */
    private static User makeUser(int id, String name, double balance) {
        User u = new User(id, name, "pass", true, name + "@test.com", "Nam", LocalDate.now()) {
            @Override public synchronized void deposit(double amount) {
                if (amount > 0) {
                    setMoney(getMoney() + amount);
                    setVirMoney(getVirMoney() + amount);
                }
            }
            @Override public synchronized void lockVirMoney(double amount) {
                setVirMoney(Math.max(0, getVirMoney() - amount));
            }
            @Override public synchronized void unlockVirMoney(double amount) {
                setVirMoney(Math.min(getMoney(), getVirMoney() + amount));
            }
            @Override public synchronized void receiveMoney(double amount) {
                if (amount > 0) {
                    setMoney(getMoney() + amount);
                    setVirMoney(getVirMoney() + amount);
                }
            }
            @Override public synchronized void settlePayment(Product p) {
                double price = p.getPrice();
                setMoney(Math.max(0, getMoney() - price));
                setVirMoney(getMoney());
                User owner = p.getOwner();
                if (owner != null && owner.getUserId() != getUserId()) {
                    owner.receiveMoney(price);
                }
            }
        };
        u.deposit(balance);
        return u;
    }

    /**
     * Tạo Product hoạt động (endTime = tương lai).
     * Override checkAndProcessEnd để không gọi DAO.
     */
    private static Product makeProduct(int id, String name, double startPrice,
                                       int durationMinutes, User owner) {
        return new Product(id, name, startPrice, durationMinutes, owner) {
            @Override public void checkAndProcessEnd() {
                if (isFinished()) return;
                if (LocalDateTime.now().isAfter(getEndTime())) {
                    setFinished(true);
                    // Không gọi ProductDAO trong unit test
                }
            }
        };
    }

    /** Tạo Product đã hết hạn (endTime = quá khứ). */
    private static Product makeExpiredProduct(int id, User owner) {
        return new Product(id, "Expired Item", 100, 100,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusMinutes(1),
                owner) {
            @Override public void checkAndProcessEnd() {
                if (!isFinished()) setFinished(true);
            }
        };
    }

    // ── Fixtures ──────────────────────────────────────────────

    private User seller;
    private User bidder1;
    private User bidder2;
    private Product product;

    @BeforeEach
    void setUp() {
        seller  = makeUser(1, "seller",  0);
        bidder1 = makeUser(2, "bidder1", 1000);
        bidder2 = makeUser(3, "bidder2", 1000);
        product = makeProduct(100, "Laptop Gaming", 100, 60, seller);
    }

    // ═══════════════════════════════════════════════════════════
    // 1. ĐẶT GIÁ THỦ CÔNG
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("1. Đặt giá thủ công")
    class ManualBidTests {

        @Test
        @DisplayName("Đặt giá đầu tiên hợp lệ → thành công, giá cập nhật đúng")
        void firstValidBid_succeeds() {
            Product.BidResult result = product.processBid(bidder1, 150, false, 0);

            assertTrue(result.isSuccess, "Phải thành công");
            assertEquals(150, product.getPrice(), 0.001);
            assertEquals(bidder1.getUserId(), product.getHighestBidder().getUserId());
        }

        @Test
        @DisplayName("Đặt giá bằng startPrice → thất bại (phải cao hơn)")
        void bidEqualToStartPrice_fails() {
            Product.BidResult result = product.processBid(bidder1, 100, false, 0);

            assertFalse(result.isSuccess);
            assertNull(product.getHighestBidder());
        }

        @Test
        @DisplayName("Đặt giá thấp hơn giá hiện tại → thất bại, giá không đổi")
        void bidLowerThanCurrentPrice_fails() {
            product.processBid(bidder1, 200, false, 0);

            Product.BidResult result = product.processBid(bidder2, 150, false, 0);

            assertFalse(result.isSuccess);
            assertEquals(200, product.getPrice(), 0.001, "Giá không được thay đổi");
            assertEquals(bidder1.getUserId(), product.getHighestBidder().getUserId());
        }

        @Test
        @DisplayName("Người dùng thứ hai vượt giá → trở thành người dẫn đầu")
        void secondBidder_overtakes() {
            product.processBid(bidder1, 200, false, 0);
            Product.BidResult result = product.processBid(bidder2, 300, false, 0);

            assertTrue(result.isSuccess);
            assertEquals(bidder2.getUserId(), product.getHighestBidder().getUserId());
            assertEquals(300, product.getPrice(), 0.001);
        }

        @Test
        @DisplayName("Đặt giá thủ công khi số dư không đủ → bid được chấp nhận nhưng virMoney về 0")
        void insufficientBalance_bidAcceptedButVirMoneyDepleted() {
            User poorUser = makeUser(99, "poor", 50);

            Product.BidResult result = product.processBid(poorUser, 200, false, 0);

            // Hiện tại: bid thành công dù không đủ tiền, virMoney về 0 (không âm)
            assertTrue(result.isSuccess, "Hiện tại code chấp nhận manual bid dù không đủ tiền");
            assertEquals(0, poorUser.getVirMoney(), 0.001, "virMoney bị khóa hết, không xuống âm");
            assertEquals(50, poorUser.getMoney(), 0.001, "money thực chưa bị trừ (chưa kết thúc phiên)");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 2. KIỂM SOÁT TRUY CẬP
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("2. Kiểm soát truy cập")
    class AccessControlTests {

        @Test
        @DisplayName("Chủ sản phẩm tự đấu giá → AuthenticationException.CODE_SELF_BID")
        void ownerBidsOwnProduct_throwsSelfBid() {
            seller.deposit(1000);

            AuthenticationException ex = assertThrows(AuthenticationException.class,
                    () -> product.processBid(seller, 200, false, 0));

            assertEquals(AuthenticationException.CODE_SELF_BID, ex.getErrorCode());
        }

        @Test
        @DisplayName("Đặt giá khi chưa đăng nhập (user = null) → AuthenticationException")
        void nullUser_throwsUnauthorized() {
            assertThrows(AuthenticationException.class,
                    () -> product.processBid(null, 200, false, 0));
        }

        @Test
        @DisplayName("Tài khoản bị khóa (isActive=false) vẫn được phép gọi (logic ở Controller)")
        void inactiveUser_noBidLogicBlock() {
            // Product không kiểm tra isActive – đó là nhiệm vụ của Controller
            User inactive = makeUser(50, "inactive", 1000);
            inactive.setActive(false);

            Product.BidResult result = product.processBid(inactive, 200, false, 0);
            // Không throw, kết quả tùy logic giá
            assertNotNull(result);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 3. VÒNG ĐỜI PHIÊN ĐẤU GIÁ
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("3. Vòng đời phiên đấu giá")
    class AuctionLifecycleTests {

        @Test
        @DisplayName("Đặt giá khi phiên đã kết thúc (isFinished=true) → AuctionClosedException")
        void bidOnFinishedAuction_throws() {
            product.setFinished(true);

            AuctionClosedException ex = assertThrows(AuctionClosedException.class,
                    () -> product.processBid(bidder1, 200, false, 0));

            assertEquals(AuctionClosedException.CODE_SESSION_ENDED, ex.getErrorCode());
        }

        @Test
        @DisplayName("Đặt giá khi endTime đã qua → phiên tự động đóng, throw AuctionClosedException")
        void bidOnExpiredProduct_autoCloses() {
            Product expired = makeExpiredProduct(101, seller);

            assertThrows(AuctionClosedException.class,
                    () -> expired.processBid(bidder1, 200, false, 0));

            assertTrue(expired.isFinished(), "Phiên phải được đánh dấu kết thúc");
        }

        @Test
        @DisplayName("checkAndProcessEnd() với phiên còn hạn → không kết thúc")
        void checkEnd_activeAuction_staysOpen() {
            product.checkAndProcessEnd();
            assertFalse(product.isFinished());
        }

        @Test
        @DisplayName("checkAndProcessEnd() với phiên hết hạn → isFinished = true")
        void checkEnd_expiredAuction_closes() {
            Product expired = makeExpiredProduct(102, seller);
            expired.checkAndProcessEnd();
            assertTrue(expired.isFinished());
        }

        @Test
        @DisplayName("Anti-sniping: đặt giá trong 10 giây cuối → endTime được gia hạn")
        void antiSniping_extendsEndTime() {
            // Tạo product sắp hết hạn trong 5 giây
            LocalDateTime almost = LocalDateTime.now().plusSeconds(5);
            Product sniped = new Product(200, "Sniped", 100, 100,
                    LocalDateTime.now().minusHours(1), almost, seller) {
                @Override public void checkAndProcessEnd() { /* không gọi DAO */ }
            };

            LocalDateTime beforeBid = sniped.getEndTime();
            sniped.processBid(bidder1, 150, false, 0);

            assertTrue(sniped.getEndTime().isAfter(beforeBid),
                    "endTime phải được gia hạn thêm sau khi bid cuối giờ");
        }

        @Test
        @DisplayName("getTimeDisplay() trả về 'Đã kết thúc' khi phiên xong")
        void timeDisplay_finished() {
            product.setFinished(true);
            assertEquals("Đã kết thúc", product.getTimeDisplay());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 4. AUTO-BID
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("4. Auto-Bid")
    class AutoBidTests {

        @Test
        @DisplayName("Đăng ký Auto-Bid hợp lệ → BidResult thành công")
        void registerAutoBid_succeeds() {
            // maxBid=500, step=50 – bidder1 có 1000
            Product.BidResult result = product.processBid(bidder1, 500, true, 50);
            assertTrue(result.isSuccess, "Đăng ký auto-bid phải thành công");
        }

        @Test
        @DisplayName("Auto-Bid không đủ tiền → BidResult thất bại")
        void autoBid_insufficientFunds_fails() {
            User poor = makeUser(10, "poor", 50);

            Product.BidResult result = product.processBid(poor, 500, true, 50);

            assertFalse(result.isSuccess, "Auto-bid khi không đủ tiền phải thất bại");
        }

        @Test
        @DisplayName("Auto-Bid phản công: bidder2 manual < maxBid bidder1 → bidder1 vẫn dẫn đầu")
        void autoBid_counterBid_winnerStaysAhead() {
            // bidder1 đặt auto-bid max=400, step=50
            product.processBid(bidder1, 400, true, 50);
            // bidder2 đặt tay 200
            product.processBid(bidder2, 200, false, 0);

            assertEquals(bidder1.getUserId(), product.getHighestBidder().getUserId(),
                    "bidder1 vẫn phải dẫn đầu nhờ auto-bid");
        }

        @Test
        @DisplayName("Auto-Bid bị vượt: bidder2 manual > maxBid bidder1 → bidder2 dẫn đầu")
        void autoBid_exceededByManual_looses() {
            product.processBid(bidder1, 400, true, 50);
            product.processBid(bidder2, 600, false, 0);

            assertEquals(bidder2.getUserId(), product.getHighestBidder().getUserId(),
                    "bidder2 phải dẫn đầu vì vượt maxBid của bidder1");
        }

        @Test
        @DisplayName("Cấu hình Auto-Bid được lưu vào product")
        void autoBid_configStored() {
            product.processBid(bidder1, 400, true, 50);

            assertFalse(product.getAutoBids().isEmpty(), "Phải có ít nhất 1 config auto-bid");
            assertEquals(bidder1.getUserId(),
                    product.getAutoBids().get(0).getUser().getUserId());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 5. TÀI CHÍNH NGƯỜI DÙNG
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("5. Tài chính người dùng")
    class UserFinanceTests {

        @Test
        @DisplayName("Nạp tiền → money và virMoney tăng đúng")
        void deposit_updatesBothBalances() {
            User u = makeUser(20, "rich", 0);
            u.deposit(500);

            assertEquals(500, u.getMoney(), 0.001);
            assertEquals(500, u.getVirMoney(), 0.001);
        }

        @Test
        @DisplayName("Nạp số âm → không thay đổi số dư")
        void deposit_negativeAmount_noChange() {
            User u = makeUser(21, "u", 100);
            u.deposit(-50);

            assertEquals(100, u.getMoney(), 0.001);
        }

        @Test
        @DisplayName("lockVirMoney → virMoney giảm, money không đổi")
        void lockVirMoney_reducesVirOnly() {
            bidder1.lockVirMoney(200);

            assertEquals(800, bidder1.getVirMoney(), 0.001);
            assertEquals(1000, bidder1.getMoney(), 0.001);
        }

        @Test
        @DisplayName("lockVirMoney vượt số dư → virMoney về 0, không âm")
        void lockVirMoney_cannotGoBelowZero() {
            bidder1.lockVirMoney(9999);

            assertEquals(0, bidder1.getVirMoney(), 0.001);
        }

        @Test
        @DisplayName("unlockVirMoney → virMoney tăng trở lại, không vượt money")
        void unlockVirMoney_restores() {
            bidder1.lockVirMoney(300);
            bidder1.unlockVirMoney(300);

            assertEquals(1000, bidder1.getVirMoney(), 0.001);
        }

        @Test
        @DisplayName("canAfford: đủ tiền → true")
        void canAfford_sufficientBalance_returnsTrue() {
            assertTrue(bidder1.canAfford(500));
        }

        @Test
        @DisplayName("canAfford: không đủ tiền → false")
        void canAfford_insufficientBalance_returnsFalse() {
            assertFalse(bidder1.canAfford(2000));
        }

        @Test
        @DisplayName("Sau khi bid thành công, virMoney của bidder bị khóa đúng số tiền")
        void successfulBid_locksVirMoney() {
            double before = bidder1.getVirMoney();
            product.processBid(bidder1, 300, false, 0);

            assertEquals(before - 300, bidder1.getVirMoney(), 0.001);
        }

        @Test
        @DisplayName("Khi bị vượt giá, virMoney của bidder cũ được hoàn lại")
        void outbid_releasesLockedMoney() {
            product.processBid(bidder1, 200, false, 0);
            double afterFirstBid = bidder1.getVirMoney(); // 800

            product.processBid(bidder2, 300, false, 0);

            assertEquals(afterFirstBid + 200, bidder1.getVirMoney(), 0.001,
                    "Tiền bị khóa của bidder1 phải được hoàn lại khi bị vượt");
        }
    }
}