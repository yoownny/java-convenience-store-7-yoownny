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
        this.inputView = new InputView(orderService);
        this.outputView = new OutputView();
    }

    public static void main(String[] args) {
        new Application().run();
    }

    public void run() {
        inputView.start();
        try {
            processOrders();
        } catch (IllegalArgumentException e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        }
    }

    private void processOrders() {
        outputView.printProducts(productService.getAllProductDescriptions());
        Receipt receipt = createOrder();
        if (receipt != null) {
            outputView.printReceipt(receipt);
            if (inputView.readContinueShopping()) {
                processOrders();
            }
        }
    }

    private Receipt createOrder() {
        try {
            Map<String, Integer> items = inputView.readItem();
            items = handlePromotionOptions(items);
            boolean useMembership = inputView.readMembershipOption();
            return orderService.createOrder(items, useMembership);
        } catch (IllegalArgumentException e) {
            System.out.println("\n[ERROR] " + e.getMessage());
            return null;
        }
    }

    private Map<String, Integer> handlePromotionOptions(Map<String, Integer> items) {
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            if (promotionService.canAddMoreItems(entry.getKey(), entry.getValue())) {
                if (inputView.readAdditionalPurchase(entry.getKey(), 1)) {
                    items.put(entry.getKey(), entry.getValue() + 1);
                }
            }
        }
        return items;
    }
}