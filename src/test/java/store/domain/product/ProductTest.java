package store.domain.product;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProductTest {
    @Nested
    @DisplayName("상품 생성")
    class ProductCreation {
        @Test
        @DisplayName("상품을 정상적으로 생성한다")
        void createProduct() {
            assertThatNoException()
                    .isThrownBy(() -> new Product("콜라", 1000, 10, "탄산2+1"));
        }

        @Test
        @DisplayName("상품 이름이 null이면 예외가 발생한다")
        void validateNullName() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Product(null, 1000, 10, "탄산2+1"))
                    .withMessage("상품명은 필수입니다.");
        }

        @Test
        @DisplayName("상품 이름이 비어있으면 예외가 발생한다")
        void validateEmptyName() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Product("", 1000, 10, "탄산2+1"))
                    .withMessage("상품명은 필수입니다.");
        }

        @Test
        @DisplayName("상품 가격이 0 이하면 예외가 발생한다")
        void validatePrice() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Product("콜라", 0, 10, "탄산2+1"))
                    .withMessage("상품 가격은 0보다 커야 합니다.");
        }
    }

    @Nested
    @DisplayName("재고 관리")
    class StockManagement {
        @Test
        @DisplayName("재고를 정상적으로 감소시킨다")
        void decreaseQuantity() {
            Product product = new Product("콜라", 1000, 10, "탄산2+1");
            product.decreaseQuantity(3);
            assertThat(product.describeProduct())
                    .isEqualTo("- 콜라 1,000원 7개 탄산2+1");
        }

        @Test
        @DisplayName("주문 수량이 0 이하면 예외가 발생한다")
        void validateOrderQuantity() {
            Product product = new Product("콜라", 1000, 10, "탄산2+1");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> product.decreaseQuantity(0))
                    .withMessage("주문 수량은 0보다 커야 합니다.");
        }

        @Test
        @DisplayName("재고보다 많은 수량을 주문하면 예외가 발생한다")
        void validateInsufficientStock() {
            Product product = new Product("콜라", 1000, 10, "탄산2+1");
            assertThatIllegalStateException()
                    .isThrownBy(() -> product.decreaseQuantity(11))
                    .withMessage("[콜라] 상품의 재고가 부족합니다.");
        }
    }

    @Nested
    @DisplayName("상품 정보")
    class ProductInformation {
        @Test
        @DisplayName("상품명이 일치하는지 확인한다")
        void matchesName() {
            Product product = new Product("콜라", 1000, 10, "탄산2+1");
            assertThat(product.matchesName("콜라")).isTrue();
            assertThat(product.matchesName("사이다")).isFalse();
        }

        @Test
        @DisplayName("프로모션 정보가 있는 상품을 올바르게 표시한다")
        void describeProductWithPromotion() {
            Product withPromotion = new Product("콜라", 1000, 10, "탄산2+1");
            assertThat(withPromotion.describeProduct()).contains("탄산2+1");
        }

        @Test
        @DisplayName("프로모션 정보가 없는 상품을 올바르게 표시한다")
        void describeProductWithoutPromotion() {
            Product withoutPromotion = new Product("물", 500, 10, "null");
            assertThat(withoutPromotion.describeProduct()).doesNotContain("null");
        }

        @Test
        @DisplayName("상품 금액을 계산한다")
        void calculateAmount() {
            Product product = new Product("콜라", 1000, 10, "탄산2+1");
            assertThat(product.calculateAmountFor(3)).isEqualTo(3000);
        }

        @Test
        @DisplayName("상품 설명을 생성한다")
        void describeProduct() {
            Product product = new Product("콜라", 1000, 10, "탄산2+1");
            assertThat(product.describeProduct()).isEqualTo("- 콜라 1,000원 10개 탄산2+1");
        }

        @Test
        @DisplayName("재고가 없는 상품 설명을 생성한다")
        void describeEmptyProduct() {
            Product product = new Product("콜라", 1000, 0, "탄산2+1");
            assertThat(product.describeProduct()).isEqualTo("- 콜라 1,000원 재고 없음 탄산2+1");
        }
    }
}