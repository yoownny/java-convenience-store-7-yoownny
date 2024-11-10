package store.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import store.domain.product.Product;
import store.domain.product.Products;
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

    // 주문 유효성 검사
    public void validateOrder(Map<String, Integer> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("주문 항목이 비어있습니다.");
        }

        // 상품 존재 여부 및 재고 확인
        for (Map.Entry<String, Integer> entry : orderItems.entrySet()) {
            String productName = entry.getKey();
            int quantity = entry.getValue();

            List<Product> availableProducts = products.findAllByName(productName);
            validateTotalStock(productName, quantity, availableProducts);
        }
    }

    // 주문 생성
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

        int promotionQuantity = calculatePromotionQuantity(promotionProduct, quantity);
        int remainingQuantity = quantity - promotionQuantity;
        int giftQuantity = 0;

        // 프로모션 수량 처리
        if (promotionQuantity > 0) {
            giftQuantity = promotionService.calculateGiftQuantity(promotionProduct, promotionQuantity);
            promotionProduct.decreaseQuantity(promotionQuantity);
        }

        // 일반 재고 처리
        if (remainingQuantity > 0) {
            normalProduct.decreaseQuantity(remainingQuantity);
        }

        return new ReceiptItem(productName, quantity, findProduct(productName).getPrice(), giftQuantity);
    }

    // 재고 확인 메서드들
    public void validateTotalStock(String productName, int quantity, List<Product> availableProducts) {
        int totalStock = availableProducts.stream()
                .mapToInt(Product::getQuantity)
                .sum();

        if (totalStock < quantity) {
            throw new IllegalArgumentException(
                    String.format("재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요.", productName, totalStock, quantity));
        }
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

    private int calculatePromotionQuantity(Product promotionProduct, int requestedQuantity) {
        if (promotionProduct == null || !promotionProduct.hasEnoughStock(1)) {
            return 0;
        }

        // 2+1 프로모션의 경우 3의 배수로 계산
        int maxPromotionSets = promotionProduct.getQuantity() / 3 * 3;
        return Math.min(requestedQuantity, maxPromotionSets);
    }

    // 상품 찾기
    public Product findProduct(String productName) {
        return products.findByName(productName)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("존재하지 않는 상품입니다: %s", productName)
                ));
    }

    // 영수증 생성
    private Receipt createReceipt(List<ReceiptItem> items, boolean useMembership) {
        int promotionDiscount = promotionService.calculateTotalDiscount(items);
        return new Receipt(items, promotionDiscount, useMembership);
    }
}
