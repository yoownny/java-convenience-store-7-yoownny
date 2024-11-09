package store.domain.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Products {
    private final List<Product> products;

    public Products(List<Product> products) {
        this.products = new ArrayList<>(products);
    }

    // 상품명으로 상품 검색
    public Optional<Product> findByName(String name) {
        return products.stream()
                .filter(product -> product.matchesName(name))
                .findFirst();
    }

    // 모든 상품 정보를 문자열 목록으로 반환
    public List<String> describeAllProducts() {
        return products.stream()
                .map(product -> product.describeProduct(products)) // 모든 상품 리스트 전달
                .toList();
    }

    public boolean hasProductWithName(String name) {
        return products.stream()
                .anyMatch(product -> product.matchesName(name));
    }

    public boolean canFulfillOrder(String productName, int quantity) {
        return findByName(productName)
                .map(product -> product.hasEnoughStock(quantity))
                .orElse(false);
    }
}
