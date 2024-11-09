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
        validateOrder(orderItems);
        List<ReceiptItem> receiptItems = processOrderItems(orderItems);
        return createReceipt(receiptItems, useMembership);
    }

    // 주문 유효성 검사
    private void validateOrder(Map<String, Integer> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("주문 항목이 비어있습니다.");
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
        Product product = findProduct(productName);
        validateStock(product, quantity);
        product.decreaseQuantity(quantity);

        int giftQuantity = promotionService.calculateGiftQuantity(product, quantity);
        return new ReceiptItem(productName, quantity, product.getPrice(), giftQuantity);
    }

    // 상품 찾기
    public Product findProduct(String productName) {
        return products.findByName(productName)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("존재하지 않는 상품입니다: %s", productName)
                ));
    }

    // 재고 확인
    private void validateStock(Product product, int quantity) {
        if (!product.hasEnoughStock(quantity)) {
            throw new IllegalArgumentException(
                    String.format("재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요.")
            );
        }
    }

    // 영수증 생성
    private Receipt createReceipt(List<ReceiptItem> items, boolean useMembership) {
        int promotionDiscount = promotionService.calculateTotalDiscount(items);
        return new Receipt(items, promotionDiscount, useMembership);
    }
}
