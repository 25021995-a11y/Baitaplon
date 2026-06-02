package com.bidcycle.model;

import com.bidcycle.exception.AuctionClosedException;
import com.bidcycle.exception.AuthenticationException;
import com.bidcycle.exception.InvalidBidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private User seller;
    private User bidder1;
    private User bidder2;
    private Product product;

    @BeforeEach
    void setUp() {

        seller = new User(
                1,
                "seller",
                "123",
                true,
                "seller@test.com",
                "Nam",
                LocalDate.now()
        );

        bidder1 = new User(
                2,
                "bidder1",
                "123",
                true,
                "b1@test.com",
                "Nam",
                LocalDate.now()
        );

        bidder2 = new User(
                3,
                "bidder2",
                "123",
                true,
                "b2@test.com",
                "Nam",
                LocalDate.now()
        );

        bidder1.deposit(1000);
        bidder2.deposit(1000);

        product = new Product(
                100,
                "Laptop Gaming",
                100,
                30,
                seller
        );
    }

    /**
     * Test đặt giá hợp lệ
     */
    @Test
    void testValidBid() {

        Product.BidResult result =
                product.processBid(bidder1, 150, false, 0);

        assertTrue(result.isSuccess);

        assertEquals(150, product.getPrice());

        assertEquals(
                bidder1.getUserId(),
                product.getHighestBidder().getUserId()
        );
    }

    /**
     * Test giá thấp hơn giá hiện tại
     */
    @Test
    void testBidLowerThanCurrentPrice() {

        product.processBid(bidder1, 150, false, 0);

        InvalidBidException ex = assertThrows(
                InvalidBidException.class,
                () -> product.processBid(bidder2, 120, false, 0)
        );

        assertEquals(InvalidBidException.CODE_PRICE_TOO_LOW, ex.getErrorCode());

        assertEquals(150, product.getPrice());
    }

    /**
     * Test không đủ tiền
     */
    @Test
    void testBidWithoutEnoughMoney() {

        User poorUser = new User(
                4,
                "poor",
                "123",
                true
        );

        poorUser.deposit(50);

        InvalidBidException ex = assertThrows(
                InvalidBidException.class,
                () -> product.processBid(poorUser, 200, false, 0)
        );

        assertEquals(InvalidBidException.CODE_INSUFFICIENT, ex.getErrorCode());
    }

    /**
     * Test chủ sản phẩm không được tự đấu giá
     */
    @Test
    void testOwnerCannotBid() {

        seller.deposit(1000);

        AuthenticationException ex = assertThrows(
                AuthenticationException.class,
                () -> product.processBid(seller, 200, false, 0)
        );

        assertEquals(AuthenticationException.CODE_SELF_BID, ex.getErrorCode());
    }

    /**
     * Test kết thúc phiên đấu giá
     */
    @Test
    void testAuctionEnd() {

        Product endedProduct = new Product(
                101,
                "Phone",
                100,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusMinutes(1),
                seller
        );

        assertThrows(
                AuctionClosedException.class,
                () -> endedProduct.processBid(
                        bidder1,
                        200,
                        false,
                        0
                )
        );

        endedProduct.checkAndProcessEnd();

        assertTrue(endedProduct.isFinished());
    }

    /**
     * Test không cho đặt giá sau khi kết thúc
     */
    @Test
    void testBidAfterAuctionEnded() {

        product.setFinished(true);

        AuctionClosedException ex = assertThrows(
                AuctionClosedException.class,
                () -> product.processBid(
                        bidder1,
                        200,
                        false,
                        0
                )
        );

        assertEquals(AuctionClosedException.CODE_SESSION_ENDED, ex.getErrorCode());
    }

    @Test
    void testNotifyObserverWhenNewBidChangesPrice() {
        final int[] notifyCount = {0};
        final double[] notifiedAmount = {0};

        product.addBidObserver(event -> {
            notifyCount[0]++;
            notifiedAmount[0] = event.getAmount();
            assertEquals(product, event.getProduct());
            assertEquals(bidder1, event.getBidder());
            assertFalse(event.isAutoBid());
        });

        Product.BidResult result = product.processBid(bidder1, 150, false, 0);

        assertTrue(result.isSuccess);
        assertEquals(1, notifyCount[0]);
        assertEquals(150, notifiedAmount[0]);
    }
}
