package store.domain.receipt;

public class ReceiptItem {
    private static final String ITEM_FORMAT = "%s\t\t\t%d\t\t%,d";
    private static final String GIFT_FORMAT = "%s\t\t\t%d";

    private final String name;
    private final int quantity;
    private final int price;
    private final int giftQuantity;

    public ReceiptItem(String name, int quantity, int price, int giftQuantity) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.giftQuantity = giftQuantity;
    }

    public int calculateAmount() {
        return price * quantity;
    }

    public String describeOrder() {
        return String.format(ITEM_FORMAT, name, quantity, calculateAmount());
    }

    public String describeGift() {
        return String.format(GIFT_FORMAT, name, giftQuantity);
    }

    public boolean hasGift() {
        return giftQuantity > 0;
    }

    public String nameValue() {
        return name;
    }

    public int quantityValue() {
        return quantity;
    }

    public int giftQuantityValue() {
        return giftQuantity;
    }

    public int priceValue() {
        return price;
    }
}