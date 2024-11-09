package store;

import java.util.Map;
import store.domain.receipt.Receipt;
import store.service.OrderService;
import store.service.ProductService;
import store.service.PromotionService;
import store.view.InputView;
import store.view.OutputView;

public class Application {
    private final InputView inputView;
    private final OutputView outputView;
    private final OrderService orderService;
    private final ProductService productService;
    private final PromotionService promotionService;

    public Application() {
        this.productService = new ProductService();
        this.promotionService = new PromotionService();
        this.orderService = new OrderService(productService.getProducts(), promotionService);
        this.inputView = new InputView();
        this.outputView = new OutputView();
    }

    public static void main(String[] args) {
        new Application().run();
    }

    public void run() {
        try {
            inputView.start();
            processOrders();
        } catch (IllegalStateException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void processOrders() {
        boolean continueOrdering = true;
        while (continueOrdering) {
            outputView.printProducts(productService.getAllProductDescriptions());
            Receipt receipt = processOneOrder();
            if (receipt != null) {
                outputView.printReceipt(receipt);
            }
            continueOrdering = inputView.readContinueShopping();
        }
    }

    private Receipt processOneOrder() {
        try {
            Map<String, Integer> items = inputView.readItem();
            items = handlePromotionOptions(items);

            if (items.isEmpty()) {
                return null;
            }

            boolean useMembership = inputView.readMembershipOption();
            return orderService.createOrder(items, useMembership);

        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] " + e.getMessage());
            return null;
        }
    }

    private Map<String, Integer> handlePromotionOptions(Map<String, Integer> items) {
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            String productName = entry.getKey();
            int quantity = entry.getValue();

            if (promotionService.canAddMoreItems(productName, quantity)) {
                if (inputView.readAdditionalPurchase(productName, 1)) {
                    items.put(productName, quantity + 1);
                }
            }
        }
        return items;
    }
}