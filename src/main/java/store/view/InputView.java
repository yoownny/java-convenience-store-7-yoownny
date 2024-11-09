package store.view;

import camp.nextstep.edu.missionutils.Console;
import java.util.HashMap;
import java.util.Map;

public class InputView {
    private static final String ITEM_DELIMITER = "-";

    public void start(){
        System.out.println("안녕하세요. W편의점입니다.");
        System.out.println("현재 보유하고 있는 상품입니다.");
        System.out.println();
    }

    public Map<String, Integer> readItem() {
        while (true) {
            try {
                return tryReadItem();
            } catch (IllegalArgumentException e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    private Map<String, Integer> tryReadItem() {
        System.out.println("\n구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])");
        String input = Console.readLine();
        validateInput(input);
        return parseOrderInput(input);
    }

    private void validateInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("입력값이 비어있습니다.");
        }
        if (!input.startsWith("[") || !input.endsWith("]")) {
            throw new IllegalArgumentException("입력값이 올바른 형식이 아닙니다.");
        }
    }

    private Map<String, Integer> parseOrderInput(String input) {
        String cleanInput = input.replace("[", "").replace("]", "");
        String[] orderItems = cleanInput.split(",");

        Map<String, Integer> orders = new HashMap<>();
        for (String item : orderItems) {
            String[] parts = item.split(ITEM_DELIMITER);
            addOrder(orders, parts);
        }
        return orders;
    }

    private void addOrder(Map<String, Integer> orders, String[] parts) {
        validateOrderFormat(parts);
        String product = parts[0].trim();
        int quantity = parseQuantity(parts[1].trim());
        orders.put(product, quantity);
    }

    private void validateOrderFormat(String[] parts) {
        if (parts.length != 2) {
            throw new IllegalArgumentException("상품명-수량 형식으로 입력해주세요.");
        }
        if (parts[0].trim().isEmpty()) {
            throw new IllegalArgumentException("상품명이 비어있습니다.");
        }
    }

    private int parseQuantity(String quantity) {
        try {
            int parsedQuantity = Integer.parseInt(quantity);
            validateQuantity(parsedQuantity);
            return parsedQuantity;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("수량은 숫자여야 합니다.");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
    }


    public boolean readAdditionalPurchase(String productName, int plusQuantity) {
        System.out.printf("현재 %s은(는) %d개를 무료로 더 받을 수 있습니다. 추가하시겠습니까? (Y/N)\n",
                productName, plusQuantity);
        String input = Console.readLine();
        validateYesNo(input);
        return input.trim().toUpperCase().equals("Y");
    }

    public boolean readPromotionPurchase(String productName, int exceptQuantity) {
        System.out.printf("현재 %s %d개는 프로모션 할인이 적용되지 않습니다. 그래도 구매하시겠습니까? (Y/N)\n",
                productName, exceptQuantity);
        String input = Console.readLine();
        validateYesNo(input);
        return input.trim().toUpperCase().equals("Y");
    }


    public boolean readMembershipOption() {
        while (true) {
            try {
                return tryReadMembershipOption();
            } catch (IllegalArgumentException e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    private boolean tryReadMembershipOption() {
        System.out.println("\n멤버십 할인을 받으시겠습니까? (Y/N)");
        String input = Console.readLine();
        validateYesNo(input);
        return input.trim().toUpperCase().equals("Y");
    }

    public boolean readContinueShopping() {
        while (true) {
            try {
                return tryReadContinueShopping();
            } catch (IllegalArgumentException e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    private boolean tryReadContinueShopping() {
        System.out.println("\n감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)");
        String input = Console.readLine();
        validateYesNo(input);
        return input.trim().toUpperCase().equals("Y");
    }

    private void validateYesNo(String input) {
        String upperInput = input.trim().toUpperCase();
        if (!upperInput.equals("Y") && !upperInput.equals("N")) {
            throw new IllegalArgumentException("Y 또는 N으로 입력해주세요.");
        }
    }
}