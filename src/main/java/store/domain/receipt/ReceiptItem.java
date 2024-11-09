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
    public int calculateAmount() {
        return price * quantity;
    }

    public String describeOrder() {
        return String.format("%s\t\t%d \t%,d", name, quantity, calculateAmount());
    }

    public String describeGift() {
        return String.format("%s\t\t%d", name, giftQuantity);
    }

    public boolean hasGift() {
        return giftQuantity > 0;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }
}
