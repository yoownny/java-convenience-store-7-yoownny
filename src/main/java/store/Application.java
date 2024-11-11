package store;

import java.util.HashMap;
import java.util.Map;
import store.domain.product.Product;
import store.domain.receipt.Receipt;
import store.service.OrderService;
import store.service.ProductService;
import store.service.PromotionService;
import store.view.InputView;
import store.view.OutputView;

public class Application {
    private static final String ERROR_PREFIX = "\n[ERROR] ";

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
            processOrders();
        } catch (IllegalArgumentException e) {
            printError(e.getMessage());
        }
    }

    private void processOrders() {
        displayInitialInformation();
        Receipt receipt = processOrder();
        handleReceipt(receipt);
    }

    private void displayInitialInformation() {
        inputView.start();
        outputView.printProducts(productService.getAllProductDescriptions());
    }

    private Receipt processOrder() {
        try {
            Map<String, Integer> items = collectOrderItems();
            if (items == null) {
                return null;
            }
            boolean useMembership = inputView.readMembershipOption();
            return orderService.createOrder(items, useMembership);
        } catch (IllegalArgumentException e) {
            printError(e.getMessage());
            return null;
        }
    }

    private Map<String, Integer> collectOrderItems() {
        Map<String, Integer> items = inputView.readItem();
        items = handlePromotionOptions(items);
        orderService.validateOrder(items);
        if (!checkNonPromotionalItems(items)) {
            return null;
        }
        orderService.validateOrder(items);
        return items;
    }

    private void handleReceipt(Receipt receipt) {
        if (receipt != null) {
            outputView.printReceipt(receipt);
            if (inputView.readContinueShopping()) {
                processOrders();
            }
        }
    }

    private boolean checkNonPromotionalItems(Map<String, Integer> items) {
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            if (!checkNonPromotionalItem(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean checkNonPromotionalItem(String productName, int quantity) {
        if (orderService.shouldShowNonPromotionalWarning(productName, quantity)) {
            int nonPromotionalQty = orderService.calculateNonPromotionalQuantity(productName, quantity);
            return inputView.confirmNonPromotionalPurchase(productName, nonPromotionalQty);
        }
        return true;
    }

    private Map<String, Integer> handlePromotionOptions(Map<String, Integer> items) {
        Map<String, Integer> updatedItems = new HashMap<>(items);
        items.forEach((productName, quantity) ->
                processPromotionOption(updatedItems, productName, quantity));
        return updatedItems;
    }

    private void processPromotionOption(Map<String, Integer> updatedItems, String productName, int quantity) {
        Product product = orderService.findProduct(productName);
        if (promotionService.canAddMoreItems(productName, product, quantity)
                && inputView.readAdditionalPurchase(productName, 1)) {
            updatedItems.put(productName, quantity + 1);
        }
    }

    private void printError(String message) {
        System.out.println(ERROR_PREFIX + message);
    }
}