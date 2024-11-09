package store.domain.promotion;

import java.time.LocalDate;

public class Promotion {
    private final String name;
    private final int buyQuantity;
    private final int giftQuantity;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public Promotion(String name, int buyQuantity, int giftQuantity, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.buyQuantity = buyQuantity;
        this.giftQuantity = giftQuantity;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isValidOn(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public int calculateGiftQuantity(int purchaseQuantity) {
        return (purchaseQuantity / buyQuantity) * giftQuantity;
    }

    public int calculateDiscount(int quantity, int price) {
        int giftQuantity = calculateGiftQuantity(quantity);
        return giftQuantity * price;
    }

    public String getName() {
        return name;
    }
}
