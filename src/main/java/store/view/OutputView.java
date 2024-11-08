package store.view;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class OutputView {
    private static final String PRODUCTS_FILE = "src/main/resources/products.md";

    public void printProducts() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(PRODUCTS_FILE));
            printProductList(lines);
        } catch (Exception e) {
            System.out.println("[ERROR] 상품 목록을 불러올 수 없습니다.");
        }
    }


    private void printProductList(List<String> lines) {
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.trim().isEmpty()) {
                printProductLine(line);
            }
        }
    }

    private void printProductLine(String line) {
        String[] parts = line.split(",");
        String name = parts[0];
        String price = formatPrice(parts[1]);
        String quantity = parts[2];
        String promotion = formatPromotion(parts[3]);
        System.out.printf("- %s %s원 %s개 %s\n", name, price, quantity, promotion.trim());
    }

    private String formatPrice(String price) {
        return String.format("%,d", Integer.parseInt(price));
    }

    private String formatPromotion(String promotion) {
        return promotion.equals("null") ? "" : " " + promotion;
    }
}