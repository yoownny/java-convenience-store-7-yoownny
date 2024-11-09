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
}
