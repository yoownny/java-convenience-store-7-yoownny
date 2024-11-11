package store.service;

import camp.nextstep.edu.missionutils.DateTimes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import store.domain.product.Product;
import store.domain.product.Products;
import store.domain.promotion.Promotion;
import store.domain.receipt.Receipt;
import store.domain.receipt.ReceiptItem;

public class OrderService {
    private static final String EMPTY_ORDER_ERROR = "주문 항목이 비어있습니다.";
    private static final String STOCK_EXCEEDED_ERROR = "재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요.";
    private static final String PRODUCT_NOT_FOUND_ERROR = "존재하지 않는 상품입니다.";

    private final Products products;
    private final PromotionService promotionService;

    public OrderService(Products products, PromotionService promotionService) {
        this.products = products;
        this.promotionService = promotionService;
    }

    public Receipt createOrder(Map<String, Integer> orderItems, boolean useMembership) {
        List<ReceiptItem> receiptItems = processOrderItems(orderItems);
        return createReceipt(receiptItems, useMembership);
    }

    public void validateOrder(Map<String, Integer> orderItems) {
        validateOrderNotEmpty(orderItems);
        validateOrderStock(orderItems);
    }

    private void validateOrderNotEmpty(Map<String, Integer> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException(EMPTY_ORDER_ERROR);
        }
    }

    private void validateOrderStock(Map<String, Integer> orderItems) {
        orderItems.forEach(this::validateProductStock);
    }

    private void validateProductStock(String productName, int quantity) {
        List<Product> availableProducts = products.findAllByName(productName);
        validateTotalStock(quantity, availableProducts);
    }

    private void validateTotalStock(int quantity, List<Product> availableProducts) {
        int totalStock = calculateTotalStock(availableProducts);
        if (totalStock < quantity) {
            throw new IllegalArgumentException(STOCK_EXCEEDED_ERROR);
        }
    }

    private int calculateTotalStock(List<Product> availableProducts) {
        return availableProducts.stream()
                .mapToInt(Product::quantityValue)
                .sum();
    }

    private List<ReceiptItem> processOrderItems(Map<String, Integer> orderItems) {
        List<ReceiptItem> receiptItems = new ArrayList<>();
        orderItems.forEach((name, quantity) ->
                receiptItems.add(processOneItem(name, quantity)));
        return receiptItems;
    }

    private ReceiptItem processOneItem(String productName, int quantity) {
        List<Product> availableProducts = products.findAllByName(productName);
        Product promotionProduct = findPromotionProduct(availableProducts);

        ProcessedQuantity processed = processPromotionQuantity(promotionProduct, quantity);

        return createReceiptItem(productName, quantity, processed.giftQuantity());
    }

    private ReceiptItem createReceiptItem(String productName, int quantity, int giftQuantity) {
        Product product = findProduct(productName);
        return new ReceiptItem(productName, quantity, product.priceValue(), giftQuantity);
    }

    private ProcessedQuantity processPromotionQuantity(Product product, int quantity) {
        if (!isValidForPromotion(product)) {
            return new ProcessedQuantity(0, quantity);
        }

        Promotion promotion = promotionService.findPromotion(product.promotionNameValue());
        if (!isActivePromotion(promotion)) {
            return new ProcessedQuantity(0, quantity);
        }

        return calculatePromotionQuantities(product, quantity);
    }

    private boolean isValidForPromotion(Product product) {
        return product != null && product.hasPromotion();
    }

    private boolean isActivePromotion(Promotion promotion) {
        return promotion != null && promotion.isActive(DateTimes.now().toLocalDate());
    }

    private ProcessedQuantity calculatePromotionQuantities(Product product, int quantity) {
        int availableQuantity = calculateAvailableQuantity(product, quantity);
        int promotionDivisor = getPromotionDivisor(product.promotionNameValue());

        return processPromotionSets(product, quantity, availableQuantity, promotionDivisor);
    }

    private int calculateAvailableQuantity(Product product, int quantity) {
        return Math.min(product.quantityValue(), quantity);
    }

    private int getPromotionDivisor(String promotionName) {
        if (promotionService.isTwoPlusOnePromotion(promotionName)) {
            return 3;
        }
        return 2;
    }

    private ProcessedQuantity processPromotionSets(Product product, int quantity,
                                                   int availableQuantity, int promotionDivisor) {
        int maxSets = availableQuantity / promotionDivisor;
        int useQuantity = maxSets * promotionDivisor;

        if (useQuantity <= 0) {
            return new ProcessedQuantity(0, quantity);
        }

        product.decreaseQuantity(availableQuantity);
        return new ProcessedQuantity(maxSets, quantity - availableQuantity);
    }

    public boolean shouldShowNonPromotionalWarning(String productName, int quantity) {
        Product promotionProduct = findPromotionProduct(products.findAllByName(productName));
        return isPromotionalWarningRequired(promotionProduct, quantity);
    }

    private boolean isPromotionalWarningRequired(Product promotionProduct, int quantity) {
        return promotionProduct != null && quantity > promotionProduct.quantityValue();
    }

    public int calculateNonPromotionalQuantity(String productName, int quantity) {
        Product promotionProduct = findPromotionProduct(products.findAllByName(productName));
        if (promotionProduct == null) {
            return 0;
        }
        return calculateExcessQuantity(promotionProduct, quantity);
    }

    private int calculateExcessQuantity(Product product, int quantity) {
        int promotionStock = product.quantityValue();
        int promotionDivisor = getPromotionDivisor(product.promotionNameValue());
        int usablePromotionQuantity = (promotionStock / promotionDivisor) * promotionDivisor;

        return Math.max(0, quantity - usablePromotionQuantity);
    }

    private Product findPromotionProduct(List<Product> products) {
        return products.stream()
                .filter(Product::hasPromotion)
                .findFirst()
                .orElse(null);
    }

    public Product findProduct(String productName) {
        return products.findByName(productName)
                .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND_ERROR));
    }

    private Receipt createReceipt(List<ReceiptItem> items, boolean useMembership) {
        int promotionDiscount = promotionService.calculateTotalDiscount(items);
        return new Receipt(items, promotionDiscount, useMembership, promotionService);
    }

    private record ProcessedQuantity(int giftQuantity, int remainingQuantity) {}
}