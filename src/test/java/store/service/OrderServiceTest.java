package store.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.FileLoader;
import store.domain.product.Product;
import store.domain.product.Products;
import store.domain.receipt.Receipt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderServiceTest {
    private OrderService orderService;
    private Products products;
    private PromotionService promotionService;

    @BeforeEach
    void setUp() {
        List<Product> productList = FileLoader.loadProducts();
        products = new Products(productList);
        promotionService = new PromotionService();
        orderService = new OrderService(products, promotionService);
    }

    @Nested
    @DisplayName("주문 유효성 검증")
    class ValidateOrder {
        @Test
        @DisplayName("주문이 비어있으면 예외가 발생한다")
        void validateEmptyOrder() {
            Map<String, Integer> emptyOrder = new HashMap<>();
            assertThatThrownBy(() -> orderService.validateOrder(emptyOrder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("주문 항목이 비어있습니다.");
        }

        @Test
        @DisplayName("재고보다 많은 수량을 주문하면 예외가 발생한다")
        void validateExceedingStock() {
            Map<String, Integer> orderItems = new HashMap<>();
            orderItems.put("물", 21);
            assertThatThrownBy(() -> orderService.validateOrder(orderItems))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요.");
        }
    }

    @Nested
    @DisplayName("주문 처리")
    class ProcessOrder {
        @Test
        @DisplayName("프로모션이 적용된 주문을 처리한다")
        void createOrderWithPromotion() {
            Map<String, Integer> orderItems = new HashMap<>();
            orderItems.put("콜라", 3);
            Receipt receipt = orderService.createOrder(orderItems, false);
            assertThat(receipt).isNotNull();
            List<String> giftLines = receipt.createGiftLines();
            assertThat(giftLines).hasSize(1);  // 1개의 증정 항목
        }

        @Test
        @DisplayName("일반 상품 주문을 처리한다")
        void createOrderWithNormalProduct() {
            Map<String, Integer> orderItems = new HashMap<>();
            orderItems.put("물", 2);
            Receipt receipt = orderService.createOrder(orderItems, false);
            assertThat(receipt).isNotNull();
            List<String> giftLines = receipt.createGiftLines();
            assertThat(giftLines).isEmpty();  // 증정 항목 없음
        }
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회시 예외가 발생한다")
    void findNonExistentProduct() {
        assertThatThrownBy(() -> orderService.findProduct("없는상품"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 상품입니다.");
    }
}
