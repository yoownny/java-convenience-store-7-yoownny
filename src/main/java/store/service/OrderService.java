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

        int giftQuantity = 0;
        int remainingQuantity = quantity;

        // 프로모션 재고 처리
        if (promotionProduct != null) {
            if (promotionProduct.getPromotionName().equals("탄산2+1")) {
                // 2+1 프로모션의 경우
                int availableQuantity = Math.min(promotionProduct.getQuantity(), quantity);
                int maxPromotionSets = availableQuantity / 3;  // 가능한 세트 수
                int promotionUseQuantity = maxPromotionSets * 3;  // 세트로 사용할 수량

                if (promotionUseQuantity > 0) {
                    giftQuantity = maxPromotionSets;  // 세트 수만큼 증정
                    promotionProduct.decreaseQuantity(availableQuantity);
                    remainingQuantity = quantity - availableQuantity;
                }
            } else if (promotionProduct.getPromotionName().equals("MD추천상품") ||
                    promotionProduct.getPromotionName().equals("반짝할인")) {
                // 1+1 프로모션의 경우
                int availableQuantity = Math.min(promotionProduct.getQuantity(), quantity);
                int maxPromotionPairs = availableQuantity / 2;  // 가능한 페어 수
                int promotionUseQuantity = maxPromotionPairs * 2;  // 페어로 사용할 수량

                if (promotionUseQuantity > 0) {
                    giftQuantity = maxPromotionPairs;  // 페어 수만큼 증정
                    promotionProduct.decreaseQuantity(availableQuantity);
                    remainingQuantity = quantity - availableQuantity;
                }
            }
        }

        // 남은 수량은 일반 재고에서 처리
        if (remainingQuantity > 0) {
            if (normalProduct != null && normalProduct.hasEnoughStock(remainingQuantity)) {
                normalProduct.decreaseQuantity(remainingQuantity);
            } else {
                throw new IllegalArgumentException("일반 상품의 재고가 부족합니다.");
            }
        }

        return new ReceiptItem(productName, quantity, findProduct(productName).getPrice(), giftQuantity);
    }

    public boolean shouldShowNonPromotionalWarning(String productName, int quantity) {
        List<Product> availableProducts = products.findAllByName(productName);
        Product promotionProduct = findPromotionProduct(availableProducts);

        if (promotionProduct == null) {
            return false;
        }

        // 프로모션 재고가 부족할 때만 경고
        return quantity > promotionProduct.getQuantity();
    }

    public int calculateNonPromotionalQuantity(String productName, int quantity) {
        List<Product> availableProducts = products.findAllByName(productName);
        Product promotionProduct = findPromotionProduct(availableProducts);

        if (promotionProduct == null) {
            return 0;
        }

        // 프로모션 재고 초과 수량 계산
        int nonPromotionalQuantity = quantity - promotionProduct.getQuantity();
        if (nonPromotionalQuantity > 0) {
            int promotionStock = promotionProduct.getQuantity();
            // 프로모션 재고에서 사용하고 남은 나머지가 있다면 그것도 일반 재고 수량에 포함
            if (promotionProduct.getPromotionName().equals("탄산2+1")) {
                int usablePromotionQuantity = (promotionStock / 3) * 3;
                nonPromotionalQuantity = quantity - usablePromotionQuantity;
            } else if (promotionProduct.getPromotionName().equals("MD추천상품") ||
                    promotionProduct.getPromotionName().equals("반짝할인")) {
                int usablePromotionQuantity = (promotionStock / 2) * 2;
                nonPromotionalQuantity = quantity - usablePromotionQuantity;
            }
        }

        return nonPromotionalQuantity;
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
