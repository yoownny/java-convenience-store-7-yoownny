package store.domain.receipt;

import java.util.List;

public class Receipt {
    private static final double MEMBERSHIP_DISCOUNT_RATE = 0.3;
    private static final int MAX_MEMBERSHIP_DISCOUNT = 8000;
    private static final String MD_PROMOTION = "MD추천상품";
    private static final String FLASH_SALE = "반짝할인";

    private final List<ReceiptItem> items;
    private final int promotionDiscount;
    private final boolean useMembership;

    public Receipt(final List<ReceiptItem> items, final int promotionDiscount, final boolean useMembership) {
        this.items = items;
        this.promotionDiscount = promotionDiscount;
        this.useMembership = useMembership;
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

        int discountableAmount = items.stream()
                .filter(item -> !item.hasGift() && !hasPromotionType(item.getName()))
                .mapToInt(ReceiptItem::calculateAmount)
                .sum();

        int membershipDiscount = (int) (discountableAmount * MEMBERSHIP_DISCOUNT_RATE);
        return Math.min(membershipDiscount, MAX_MEMBERSHIP_DISCOUNT);
    }

    private boolean hasPromotionType(String productName) {
        return productName.contains(MD_PROMOTION) ||
                productName.contains(FLASH_SALE);
    }

    public int calculateFinalAmount() {
        return calculateTotalAmount() - promotionDiscount - calculateMembershipDiscount();
    }

}
