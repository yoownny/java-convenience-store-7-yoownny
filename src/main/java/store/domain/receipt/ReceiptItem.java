package store.domain.receipt;

public class ReceiptItem {
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
}
