package store.service;

import java.util.List;
import store.FileLoader;
import store.domain.product.Product;
import store.domain.product.Products;

public class ProductService {
    private final Products products;

    public ProductService() {
        List<Product> productList = FileLoader.loadProducts();
        this.products = new Products(productList);
    }

    // 전체 상품 목록 조회
    public List<String> getAllProductDescriptions() {
        return products.describeAllProducts();
    }

    public Products getProducts() {
        return products;
    }
}
