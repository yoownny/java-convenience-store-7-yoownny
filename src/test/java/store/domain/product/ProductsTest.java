package store.domain.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductsTest {
    @Test
    @DisplayName("상품 목록을 관리한다")
    void manageProducts() {
        List<Product> productList = List.of(
                new Product("콜라", 1000, 10, "탄산2+1"),
                new Product("물", 500, 10, "null")
        );
        Products products = new Products(productList);
        assertThat(products.hasProductWithName("콜라")).isTrue();
        assertThat(products.hasProductWithName("사이다")).isFalse();
    }

    @Test
    @DisplayName("상품 주문 가능 여부를 확인한다")
    void checkOrderAvailability() {
        List<Product> productList = List.of(
                new Product("콜라", 1000, 10, "탄산2+1"),
                new Product("물", 500, 10, "null")
        );
        Products products = new Products(productList);
        assertThat(products.canFulfillOrder("콜라", 5)).isTrue();
        assertThat(products.canFulfillOrder("콜라", 15)).isFalse();
        assertThat(products.canFulfillOrder("사이다", 1)).isFalse();
    }

    @Test
    @DisplayName("모든 상품 설명을 생성한다")
    void describeAllProducts() {
        List<Product> productList = List.of(
                new Product("콜라", 1000, 10, "탄산2+1"),
                new Product("물", 500, 10, "null")
        );
        Products products = new Products(productList);
        List<String> descriptions = products.describeAllProducts();
        assertThat(descriptions).containsExactly(
                "- 콜라 1,000원 10개 탄산2+1",
                "- 물 500원 10개"
        );
    }
}