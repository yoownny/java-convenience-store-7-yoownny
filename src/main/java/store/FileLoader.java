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
    private static final String PRODUCT_LOAD_ERROR = "상품 정보를 불러올 수 없습니다.";
    private static final String PROMOTION_LOAD_ERROR = "프로모션 정보를 불러올 수 없습니다.";
    private static final String INVALID_PRODUCT_FORMAT = "잘못된 상품 데이터 형식입니다.";
    private static final String INVALID_PROMOTION_FORMAT = "잘못된 프로모션 데이터 형식입니다.";
    private static final int PRODUCT_PARTS_COUNT = 4;
    private static final int PROMOTION_PARTS_COUNT = 5;
    private static final int HEADER_LINE_COUNT = 1;

    public static List<Product> loadProducts() {
        List<String> lines = readProductLines();
        return parseProducts(lines);
    }

    private static List<String> readProductLines() {
        try {
            return Files.readAllLines(Paths.get(PRODUCTS_FILE));
        } catch (Exception e) {
            throw new IllegalStateException(PRODUCT_LOAD_ERROR, e);
        }
    }

    private static List<Product> parseProducts(List<String> lines) {
        List<Product> products = new ArrayList<>();
        processProductLines(lines, products);
        return products;
    }

    private static void processProductLines(List<String> lines, List<Product> products) {
        for (int i = HEADER_LINE_COUNT; i < lines.size(); i++) {
            processProductLine(lines.get(i), products);
        }
    }

    private static void processProductLine(String line, List<Product> products) {
        if (line.trim().isEmpty()) {
            return;
        }
        products.add(createProduct(line));
    }

    private static Product createProduct(String line) {
        String[] parts = line.split(",");
        validateProductParts(parts);
        return buildProduct(parts);
    }

    private static Product buildProduct(String[] parts) {
        return new Product(
                parts[0],
                parseInteger(parts[1]),
                parseInteger(parts[2]),
                parts[3]
        );
    }

    private static int parseInteger(String value) {
        return Integer.parseInt(value.trim());
    }

    public static Map<String, Promotion> loadPromotions() {
        List<String> lines = readPromotionLines();
        return parsePromotions(lines);
    }

    private static List<String> readPromotionLines() {
        try {
            return Files.readAllLines(Paths.get(PROMOTIONS_FILE));
        } catch (Exception e) {
            throw new IllegalStateException(PROMOTION_LOAD_ERROR, e);
        }
    }

    private static Map<String, Promotion> parsePromotions(List<String> lines) {
        Map<String, Promotion> promotions = new ConcurrentHashMap<>();
        processPromotionLines(lines, promotions);
        return promotions;
    }

    private static void processPromotionLines(List<String> lines, Map<String, Promotion> promotions) {
        for (int i = HEADER_LINE_COUNT; i < lines.size(); i++) {
            processPromotionLine(lines.get(i), promotions);
        }
    }

    private static void processPromotionLine(String line, Map<String, Promotion> promotions) {
        if (line.trim().isEmpty()) {
            return;
        }
        addPromotionToMap(line, promotions);
    }

    private static void addPromotionToMap(String line, Map<String, Promotion> promotions) {
        Promotion promotion = createPromotion(line);
        promotions.put(promotion.nameValue(), promotion);
    }

    private static Promotion createPromotion(String line) {
        String[] parts = line.split(",");
        validatePromotionParts(parts);
        return buildPromotion(parts);
    }

    private static Promotion buildPromotion(String[] parts) {
        return new Promotion(
                parts[0],
                LocalDate.parse(parts[3]),
                LocalDate.parse(parts[4])
        );
    }

    private static void validateProductParts(String[] parts) {
        if (parts.length != PRODUCT_PARTS_COUNT) {
            throw new IllegalStateException(INVALID_PRODUCT_FORMAT);
        }
    }

    private static void validatePromotionParts(String[] parts) {
        if (parts.length != PROMOTION_PARTS_COUNT) {
            throw new IllegalStateException(INVALID_PROMOTION_FORMAT);
        }
    }
}