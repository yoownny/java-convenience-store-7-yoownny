package store.domain.receipt;

import java.util.List;
import store.service.PromotionService;

public class Receipt {
    private static final double MEMBERSHIP_DISCOUNT_RATE = 0.3;
    private static final int MAX_MEMBERSHIP_DISCOUNT = 8000;

    private final List<ReceiptItem> items;
    private final int promotionDiscount;
    private final boolean useMembership;
    private final PromotionService promotionService;


    public Receipt(List<ReceiptItem> items, int promotionDiscount, boolean useMembership, PromotionService promotionService) {
        this.items = items;
        this.promotionDiscount = promotionDiscount;
        this.useMembership = useMembership;
        this.promotionService = promotionService;
    }

    public List<String> createOrderLines() {
        return items.stream()
                .map(ReceiptItem::describeOrder)
                .toList();
    }

    public List<String> createGiftLines() {
        return items.stream()
                .filter(ReceiptItem::hasGift)
                .map(ReceiptItem::describeGift)
                .toList();
    }

    public int getTotalQuantity() {
        return items.stream()
                .mapToInt(ReceiptItem::getQuantity)
                .sum();
    }

    public int calculateTotalAmount() {
        return items.stream()
                .mapToInt(ReceiptItem::calculateAmount)
                .sum();
    }

    public int getPromotionDiscount() {
        return promotionDiscount;
    }

    public int calculateMembershipDiscount() {
        if (!useMembership) {
            return 0;
        }
        return calculateDiscountAmount();
    }

    private int calculateDiscountAmount() {
        int discountableAmount = calculateDiscountableAmount();
        int membershipDiscount = (int) (discountableAmount * MEMBERSHIP_DISCOUNT_RATE);
        return Math.min(membershipDiscount, MAX_MEMBERSHIP_DISCOUNT);
    }

    private int calculateDiscountableAmount() {
        return items.stream()
                .filter(this::isDiscountableItem)
                .mapToInt(ReceiptItem::calculateAmount)
                .sum();
    }

    private boolean isDiscountableItem(ReceiptItem item) {
        return !item.hasGift() && !promotionService.isPromotionProduct(item.getName());
    }

    public int calculateFinalAmount() {
        return calculateTotalAmount() - promotionDiscount - calculateMembershipDiscount();
    }

}
