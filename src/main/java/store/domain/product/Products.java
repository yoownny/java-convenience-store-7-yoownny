package store.domain.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Products {
    private final List<Product> products;

    public Products(List<Product> products) {
        this.products = new ArrayList<>(products);
    }

    public List<String> describeAllProducts() {
        return products.stream()
                .map(product -> product.describeProduct(products))
                .toList();
    }

    public Optional<Product> findByName(String name) {
        return products.stream()
                .filter(product -> product.matchesName(name))
                .findFirst();
    }

    public List<Product> findAllByName(String name) {
        return products.stream()
                .filter(product -> product.matchesName(name))
                .toList();
    }
}
