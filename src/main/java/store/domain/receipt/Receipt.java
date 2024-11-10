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

    public int calculateMembershipDiscount() {
        if (!useMembership) {
            return 0;
        }

        // MD추천상품이나 반짝할인이 적용된 상품은 멤버십 할인 제외
        int discountableAmount = items.stream()
                .filter(item -> !item.hasGift() && !hasPromotionType(item.getName()))
                .mapToInt(ReceiptItem::calculateAmount)
                .sum();

        int membershipDiscount = (int) (discountableAmount * MEMBERSHIP_DISCOUNT_RATE);
        return Math.min(membershipDiscount, MAX_MEMBERSHIP_DISCOUNT);
    }

    // 프로모션 타입 확인 메서드 수정
    private boolean hasPromotionType(String productName) {
        return productName.contains(MD_PROMOTION) ||
                productName.contains(FLASH_SALE);
    }

    // 최종 결제 금액 계산
    public int calculateFinalAmount() {
        return calculateTotalAmount() - promotionDiscount - calculateMembershipDiscount();
    }

}
