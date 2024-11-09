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

    // 해당 상품의 총 구매 금액 계산
    public int calculateAmount() {
        return price * quantity;
    }

    // 구매 내역을 영수증 형식의 문자열로 변환
    public String describeOrder() {
        return String.format("%s\t\t%d\t%,d", name, quantity, calculateAmount());
    }

    // 증정 내역을 영수증 형식의 문자열로 변환
    public String describeGift() {
        return String.format("%s\t\t%d", name, giftQuantity);
    }

    // 증정 품목이 있는지 확인
    public boolean hasGift() {
        return giftQuantity > 0;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getGiftQuantity(){
        return giftQuantity;
    }

    public int getPrice() {
        return price;
    }
}
