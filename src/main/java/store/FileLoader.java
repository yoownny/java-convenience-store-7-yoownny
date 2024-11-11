package store;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import store.domain.product.Product;
import store.domain.promotion.Promotion;

public class FileLoader {
    private static final String PRODUCTS_FILE = "src/main/resources/products.md";
    private static final String PROMOTIONS_FILE = "src/main/resources/promotions.md";

    public static List<Product> loadProducts() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(PRODUCTS_FILE));
            return parseProducts(lines);
        } catch (Exception e) {
            throw new IllegalStateException("상품 정보를 불러올 수 없습니다.", e);
        }
    }

    private static List<Product> parseProducts(List<String> lines) {
        List<Product> products = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.trim().isEmpty()) {
                products.add(parseProduct(line));
            }
        }
        return products;
    }

    private static Product parseProduct(String line) {
        String[] parts = line.split(",");
        validateProductParts(parts);
        String name = parts[0];
        int price = Integer.parseInt(parts[1]);
        int quantity = Integer.parseInt(parts[2]);
        String promotionName = parts[3];
        return new Product(name, price, quantity, promotionName);
    }

    public static Map<String, Promotion> loadPromotions() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(PROMOTIONS_FILE));
            return parsePromotions(lines);
        } catch (Exception e) {
            throw new IllegalStateException("프로모션 정보를 불러올 수 없습니다.", e);
        }
    }

    private static Map<String, Promotion> parsePromotions(List<String> lines) {
        Map<String, Promotion> promotions = new ConcurrentHashMap<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.trim().isEmpty()) {
                Promotion promotion = parsePromotion(line);
                promotions.put(promotion.getName(), promotion);
            }
        }
        return promotions;
    }

    private static Promotion parsePromotion(String line) {
        String[] parts = line.split(",");
        validatePromotionParts(parts);
        return new Promotion(
                parts[0],
                LocalDate.parse(parts[3]),
                LocalDate.parse(parts[4])
        );
    }

    private static void validateProductParts(String[] parts) {
        if (parts.length != 4) {
            throw new IllegalStateException("잘못된 상품 데이터 형식입니다.");
        }
    }

    private static void validatePromotionParts(String[] parts) {
        if (parts.length != 5) {
            throw new IllegalStateException("잘못된 프로모션 데이터 형식입니다.");
        }
    }

}
