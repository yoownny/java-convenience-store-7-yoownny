package store.view;

import java.util.List;
import store.domain.receipt.Receipt;

public class OutputView {
    private static final String STORE_HEADER = "==============W 편의점================";
    private static final String GIFT_HEADER = "=============증     정===============";
    private static final String FOOTER = "====================================";

    public void printProducts(List<String> products) {
        products.forEach(System.out::println);
    }

    public void printReceipt(Receipt receipt) {
        System.out.println();
        printHeader();
        printOrderSection(receipt);

        if (!receipt.createGiftLines().isEmpty()) {
            printGiftSection(receipt);
        }

        printFooter();
        printPaymentSummary(receipt);
    }

    private void printHeader() {
        System.out.println(STORE_HEADER);
        System.out.println("상품명\t\t\t수량\t\t금액");
    }

    private void printOrderSection(Receipt receipt) {
        receipt.createOrderLines()
                .forEach(System.out::println);
    }

    private void printGiftSection(Receipt receipt) {
        System.out.println(GIFT_HEADER);
        receipt.createGiftLines()
                .forEach(System.out::println);
    }

    private void printFooter() {
        System.out.println(FOOTER);
    }

    private void printPaymentSummary(Receipt receipt) {
        System.out.printf("총구매액\t\t\t%d\t\t%,d\n",
                receipt.getTotalQuantity(),
                receipt.calculateTotalAmount());

        System.out.printf("행사할인\t\t\t\t\t-%,d\n",
                receipt.getPromotionDiscount());

        System.out.printf("멤버십할인\t\t\t\t-%,d\n",
                receipt.calculateMembershipDiscount());

        System.out.printf("내실돈\t\t\t\t\t %,d\n",
                receipt.calculateFinalAmount());
    }
}