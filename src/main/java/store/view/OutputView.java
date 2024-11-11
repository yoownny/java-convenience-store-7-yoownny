package store.view;

import java.util.List;
import store.domain.receipt.Receipt;

public class OutputView {
    private static final String STORE_HEADER = "==============W 편의점================";
    private static final String GIFT_HEADER = "=============증     정===============";
    private static final String FOOTER = "====================================";
    private static final String ORDER_HEADER = "상품명\t\t\t수량\t\t금액";
    private static final String TOTAL_FORMAT = "총구매액\t\t\t%d\t\t%,d\n";
    private static final String PROMOTION_FORMAT = "행사할인\t\t\t\t\t-%,d\n";
    private static final String MEMBERSHIP_FORMAT = "멤버십할인\t\t\t\t-%,d\n";
    private static final String FINAL_FORMAT = "내실돈\t\t\t\t\t %,d\n";
    private static final String CONTINUOUS_SHOPPING = "\n감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)";

    public void printProducts(List<String> products) {
        products.forEach(System.out::println);
    }

    public void printReceipt(Receipt receipt) {
        printReceiptHeader();
        printReceiptBody(receipt);
        printReceiptSummary(receipt);
    }

    private void printReceiptHeader() {
        System.out.println();
        System.out.println(STORE_HEADER);
        System.out.println(ORDER_HEADER);
    }

    private void printReceiptBody(Receipt receipt) {
        printOrderLines(receipt);
        printGiftSection(receipt);
        System.out.println(FOOTER);
    }

    private void printOrderLines(Receipt receipt) {
        receipt.createOrderLines()
                .forEach(System.out::println);
    }

    private void printGiftSection(Receipt receipt) {
        List<String> giftLines = receipt.createGiftLines();
        if (giftLines.isEmpty()) {
            return;
        }
        printGiftLines(giftLines);
    }

    private void printGiftLines(List<String> giftLines) {
        System.out.println(GIFT_HEADER);
        giftLines.forEach(System.out::println);
    }

    private void printReceiptSummary(Receipt receipt) {
        printTotalAmount(receipt);
        printDiscounts(receipt);
        printFinalAmount(receipt);
    }

    private void printTotalAmount(Receipt receipt) {
        System.out.printf(TOTAL_FORMAT,
                receipt.calculateTotalQuantity(),
                receipt.calculateTotalAmount());
    }

    private void printDiscounts(Receipt receipt) {
        System.out.printf(PROMOTION_FORMAT, receipt.promotionDiscountValue());
        System.out.printf(MEMBERSHIP_FORMAT, receipt.calculateMembershipDiscount());
    }

    private void printFinalAmount(Receipt receipt) {
        System.out.printf(FINAL_FORMAT, receipt.calculateFinalAmount());
    }

    public void printContinueShopping() {
        System.out.println(CONTINUOUS_SHOPPING);
    }
}