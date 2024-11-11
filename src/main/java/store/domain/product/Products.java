package store.domain.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Products {
    private final List<Product> products;

    public Products(List<Product> products) {
        this.products = new ArrayList<>(products);
    }

    public List<Product> findAllByName(String name) {
        return products.stream()
                .filter(product -> product.getName().equals(name))
                .sorted((p1, p2) -> Boolean.compare(p2.hasPromotion(), p1.hasPromotion()))
                .collect(Collectors.toList());
    }

    public Optional<Product> findByName(String name) {
        return products.stream()
                .filter(product -> product.matchesName(name))
                .findFirst();
    }

    public List<String> describeAllProducts() {
        return products.stream()
                .map(product -> product.describeProduct(products))
                .toList();
    }
}
