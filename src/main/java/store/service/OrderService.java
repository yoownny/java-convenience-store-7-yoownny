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

        for (Map.Entry<String, Integer> entry : orderItems.entrySet()) {
            String productName = entry.getKey();
            int quantity = entry.getValue();

            List<Product> availableProducts = products.findAllByName(productName);
            validateTotalStock(productName, quantity, availableProducts);
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

        int giftQuantity = 0;
        int remainingQuantity = quantity;

        if (promotionProduct != null && promotionProduct.hasPromotion()) {
            // PromotionService를 통해 프로모션을 가져옵니다.
            Promotion promotion = promotionService.getPromotion(promotionProduct.getPromotionName());

            // 프로모션이 유효한지 확인
            if (promotion != null && promotion.isActive(DateTimes.now().toLocalDate())) {
                if (promotionProduct.getPromotionName().equals("탄산2+1")) {
                    int availableQuantity = Math.min(promotionProduct.getQuantity(), quantity);
                    int maxPromotionSets = availableQuantity / 3;
                    int promotionUseQuantity = maxPromotionSets * 3;

                    if (promotionUseQuantity > 0) {
                        giftQuantity = maxPromotionSets;
                        promotionProduct.decreaseQuantity(availableQuantity);
                        remainingQuantity = quantity - availableQuantity;
                    }
                } else if (promotionProduct.getPromotionName().equals("MD추천상품") ||
                        promotionProduct.getPromotionName().equals("반짝할인")) {

                    int availableQuantity = Math.min(promotionProduct.getQuantity(), quantity);
                    int maxPromotionPairs = availableQuantity / 2;
                    int promotionUseQuantity = maxPromotionPairs * 2;

                    if (promotionUseQuantity > 0) {
                        giftQuantity = maxPromotionPairs;
                        promotionProduct.decreaseQuantity(availableQuantity);
                        remainingQuantity = quantity - availableQuantity;
                    }
                }
            }
        }

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

        return quantity > promotionProduct.getQuantity();
    }

    public int calculateNonPromotionalQuantity(String productName, int quantity) {
        List<Product> availableProducts = products.findAllByName(productName);
        Product promotionProduct = findPromotionProduct(availableProducts);

        if (promotionProduct == null) {
            return 0;
        }

        int nonPromotionalQuantity = quantity - promotionProduct.getQuantity();
        if (nonPromotionalQuantity > 0) {
            int promotionStock = promotionProduct.getQuantity();
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


    public Product findProduct(String productName) {
        return products.findByName(productName)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("존재하지 않는 상품입니다: %s", productName)
                ));
    }

    private Receipt createReceipt(List<ReceiptItem> items, boolean useMembership) {
        int promotionDiscount = promotionService.calculateTotalDiscount(items);
        return new Receipt(items, promotionDiscount, useMembership, promotionService);
    }
}
