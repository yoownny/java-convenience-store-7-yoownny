package store.domain.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Products {
    private final List<Product> products;

    public Products(List<Product> products) {
        this.products = new ArrayList<>(products);
    }

    public Optional<Product> findByName(String name) {
        return products.stream()
                .filter(product -> product.matchesName(name))
                .findFirst();
    }

    public List<String> describeAllProducts() {
        return products.stream()
                .map(Product::describeProduct)
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
