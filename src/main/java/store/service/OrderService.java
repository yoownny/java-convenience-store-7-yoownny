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
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("주문 항목이 비어있습니다.");
        }
        validateOrderStock(orderItems);
    }

    private void validateOrderStock(Map<String, Integer> orderItems) {
        for (Map.Entry<String, Integer> entry : orderItems.entrySet()) {
            List<Product> availableProducts = products.findAllByName(entry.getKey());
            validateTotalStock(entry.getValue(), availableProducts);
        }
    }

    private void validateTotalStock(int quantity, List<Product> availableProducts) {
        int totalStock = availableProducts.stream()
                .mapToInt(Product::getQuantity)
                .sum();
        if (totalStock < quantity) {
            throw new IllegalArgumentException("재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요.");
        }
    }

    private List<ReceiptItem> processOrderItems(Map<String, Integer> orderItems) {
        List<ReceiptItem> receiptItems = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : orderItems.entrySet()) {
            ReceiptItem item = processOneItem(entry.getKey(), entry.getValue());
            receiptItems.add(item);
        }
        return receiptItems;
    }

    private ReceiptItem processOneItem(String productName, int quantity) {
        List<Product> availableProducts = products.findAllByName(productName);
        Product promotionProduct = findPromotionProduct(availableProducts);
        Product normalProduct = findNormalProduct(availableProducts);
        ProcessedQuantity processed = processPromotionQuantity(promotionProduct, quantity);
        processRemainingQuantity(normalProduct, processed.remainingQuantity());
        return new ReceiptItem(productName, quantity, findProduct(productName).getPrice(), processed.giftQuantity());
    }

    private ProcessedQuantity processPromotionQuantity(Product promotionProduct, int quantity) {
        if (!isValidPromotionProduct(promotionProduct)) {
            return new ProcessedQuantity(0, quantity);
        }
        Promotion promotion = promotionService.getPromotion(promotionProduct.getPromotionName());
        if (!isActivePromotion(promotion)) {
            return new ProcessedQuantity(0, quantity);
        }
        return calculatePromotionQuantities(promotionProduct, quantity);
    }

    private boolean isValidPromotionProduct(Product product) {
        return product != null && product.hasPromotion();
    }

    private boolean isActivePromotion(Promotion promotion) {
        return promotion != null && promotion.isActive(DateTimes.now().toLocalDate());
    }

    private ProcessedQuantity calculatePromotionQuantities(Product product, int quantity) {
        int availableQuantity = Math.min(product.getQuantity(), quantity);
        int promotionDivisor = getPromotionDivisor(product.getPromotionName());
        int maxSets = availableQuantity / promotionDivisor;
        int useQuantity = maxSets * promotionDivisor;
        if (useQuantity > 0) {
            product.decreaseQuantity(availableQuantity);
            return new ProcessedQuantity(maxSets, quantity - availableQuantity);
        }
        return new ProcessedQuantity(0, quantity);
    }

    private int getPromotionDivisor(String promotionName) {
        if (promotionService.isTwoPlusOnePromotion(promotionName)) {
            return 3;
        }
        return 2;
    }

    private void processRemainingQuantity(Product normalProduct, int remainingQuantity) {
        if (remainingQuantity > 0 && normalProduct != null && normalProduct.hasEnoughStock(remainingQuantity)) {
            normalProduct.decreaseQuantity(remainingQuantity);
        }
    }

    public boolean shouldShowNonPromotionalWarning(String productName, int quantity) {
        Product promotionProduct = findPromotionProduct(products.findAllByName(productName));
        return promotionProduct != null && quantity > promotionProduct.getQuantity();
    }

    public int calculateNonPromotionalQuantity(String productName, int quantity) {
        Product promotionProduct = findPromotionProduct(products.findAllByName(productName));
        if (promotionProduct == null) {
            return 0;
        }
        int promotionStock = promotionProduct.getQuantity();
        int promotionDivisor = getPromotionDivisor(promotionProduct.getPromotionName());
        int usablePromotionQuantity = (promotionStock / promotionDivisor) * promotionDivisor;
        return Math.max(0, quantity - usablePromotionQuantity);
    }

    private Product findPromotionProduct(List<Product> products) {
        return products.stream()
                .filter(Product::hasPromotion)
                .findFirst()
                .orElse(null);
    }

    private Product findNormalProduct(List<Product> products) {
        return products.stream()
                .filter(p -> !p.hasPromotion())
                .findFirst()
                .orElse(null);
    }

    public Product findProduct(String productName) {
        return products.findByName(productName)
                .orElseThrow(() -> new IllegalArgumentException(String.format("존재하지 않는 상품입니다")));
    }

    private Receipt createReceipt(List<ReceiptItem> items, boolean useMembership) {
        int promotionDiscount = promotionService.calculateTotalDiscount(items);
        return new Receipt(items, promotionDiscount, useMembership, promotionService);
    }

    private record ProcessedQuantity(int giftQuantity, int remainingQuantity) {
    }
}