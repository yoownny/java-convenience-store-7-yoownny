package store.domain.receipt;

import java.util.List;

public class Receipt {
    private static final double MEMBERSHIP_DISCOUNT_RATE = 0.3;
    private static final int MAX_MEMBERSHIP_DISCOUNT = 8000;

    private final List<ReceiptItem> items;
    private final int promotionDiscount;
    private final boolean useMembership;

    public Receipt(final List<ReceiptItem> items, final int promotionDiscount, final boolean useMembership) {
        this.items = items;
        this.promotionDiscount = promotionDiscount;
        this.useMembership = useMembership;
    }

    // 구매 항목들을 영수증 형식의 문자열 목록으로 변환
    public List<String> createOrderLines() {
        return items.stream()
                .map(ReceiptItem::describeOrder)
                .toList();
    }

    // 증정 항목들을 영수증 형식의 문자열 목록으로 변환
    public List<String> createGiftLines() {
        return items.stream()
                .filter(ReceiptItem::hasGift)
                .map(ReceiptItem::describeGift)
                .toList();
    }

    // 전체 구매 수량 계산
    public int getTotalQuantity() {
        return items.stream()
                .mapToInt(ReceiptItem::getQuantity)
                .sum();
    }

    // 총 구매 금액 계산 (할인 전)
    public int calculateTotalAmount() {
        return items.stream()
                .mapToInt(ReceiptItem::calculateAmount)
                .sum();
    }

    // 프로모션 할인 금액 조회
    public int getPromotionDiscount() {
        return promotionDiscount;
    }

    // 멤버십 할인 금액 계산
    public int calculateMembershipDiscount() {
        if (!useMembership) {
            return 0;
        }

        int discountableAmount = calculateDiscountableAmount();;
        int membershipDiscount = (int) (discountableAmount * MEMBERSHIP_DISCOUNT_RATE);
        return Math.min(membershipDiscount, MAX_MEMBERSHIP_DISCOUNT);
    }

    // 프로모션 적용되지 않은 상품만 필터링
    private int calculateDiscountableAmount() {
        return items.stream()
                .filter(item -> !item.hasGift())
                .mapToInt(ReceiptItem::calculateAmount)
                .sum();
    }

    // 최종 결제 금액 계산
    public int calculateFinalAmount() {
        return calculateTotalAmount() - promotionDiscount - calculateMembershipDiscount();
    }

    // 프로모션 할인이 적용되었는지 확인
    public boolean hasPromotionDiscount() {
        return promotionDiscount > 0;
    }

    // 멤버십 할인이 적용되었는지 확인
    public boolean hasMembershipDiscount() {
        return useMembership && calculateMembershipDiscount() > 0;
    }

}
