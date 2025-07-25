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
        this.orderService = initializeOrderService();
        this.inputView = new InputView();
        this.outputView = new OutputView();
    }

    private OrderService initializeOrderService() {
        return new OrderService(
                productService.productsValue(),
                promotionService
        );
    }

    public static void main(String[] args) {
        new Application().run();
    }

    public void run() {
        try {
            startShoppingProcess();
        } catch (IllegalArgumentException e) {
            printError(e.getMessage());
        }
    }

    private void startShoppingProcess() {
        displayInitialInformation();
        processShoppingSession();
    }

    private void displayInitialInformation() {
        inputView.start();
        outputView.printProducts(productService.createProductDescriptions());
    }

    private void processShoppingSession() { // 쇼핑 프로세스
        Receipt receipt = createShoppingReceipt();
        processShoppingResult(receipt);
    }

    private Receipt createShoppingReceipt() {
        Map<String, Integer> items = null;
        while (items == null) {
            try {
                items = processOrderItems();
            } catch (IllegalArgumentException e) {
                printError(e.getMessage());
            }
        }
        return createReceiptWithMembership(items);
    }

    private Map<String, Integer> processOrderItems() {
        Map<String, Integer> items = readItemWithRetry();
        if (items == null) {
            return null;
        }

        items = processPromotions(items);

        if (!validateAndUpdateItems(items)) {  // 유효성 검증 실패시 재시도
            return processOrderItems();
        }

        return items;
    }

    private Map<String, Integer> readItemWithRetry() {
        while (true) {
            try {
                return inputView.readItem();
            } catch (IllegalArgumentException e) {
                printError(e.getMessage());
            }
        }
    }

    private boolean validateAndUpdateItems(Map<String, Integer> items) {
        try {
            orderService.validateOrder(items);
            if (!validateNonPromotionalItems(items)) {
                return false;  // 비프로모션 구매 거부시 false 반환
            }
            orderService.validateOrder(items);
            return true;
        } catch (IllegalArgumentException e) {
            printError(e.getMessage());
            return false;  // 유효성 검증 실패시 false 반환
        }
    }

    private Map<String, Integer> processPromotions(Map<String, Integer> items) {
        Map<String, Integer> updatedItems = new HashMap<>(items);
        items.forEach((name, quantity) ->
                processProductPromotion(updatedItems, name, quantity));
        return updatedItems;
    }

    private void processProductPromotion(Map<String, Integer> updatedItems,
                                         String productName,
                                         int quantity) {
        Product product = orderService.findProduct(productName);
        if (canApplyPromotion(productName, product, quantity)) {
            updatePromotionQuantity(updatedItems, productName, quantity);
        }
    }

    private boolean canApplyPromotion(String productName, Product product, int quantity) {
        return promotionService.canAddMoreItems(product, quantity)
                && inputView.readAdditionalPurchase(productName, 1);
    }

    private void updatePromotionQuantity(Map<String, Integer> items,
                                         String productName,
                                         int quantity) {
        items.put(productName, quantity + 1);
    }

    private boolean validateNonPromotionalItems(Map<String, Integer> items) {
        return items.entrySet().stream()
                .allMatch(this::validateNonPromotionalItem);
    }

    private boolean validateNonPromotionalItem(Map.Entry<String, Integer> entry) {
        String productName = entry.getKey();
        int quantity = entry.getValue();

        if (!needsNonPromotionalWarning(productName, quantity)) {
            return true;
        }
        return confirmNonPromotionalPurchase(productName, quantity);
    }

    private boolean needsNonPromotionalWarning(String productName, int quantity) {
        return orderService.shouldShowNonPromotionalWarning(productName, quantity);
    }

    private boolean confirmNonPromotionalPurchase(String productName, int quantity) {
        int nonPromotionalQty = orderService.calculateNonPromotionalQuantity(productName, quantity);
        return inputView.confirmNonPromotionalPurchase(productName, nonPromotionalQty);
    }

    private Receipt createReceiptWithMembership(Map<String, Integer> items) {
        boolean useMembership = inputView.readMembershipOption();
        return orderService.createOrder(items, useMembership);
    }

    private void processShoppingResult(Receipt receipt) {
        if (receipt == null) {
            outputView.printContinueShopping();  // 새로 추가된 메소드
            if (inputView.readContinueShopping()) {
                startShoppingProcess();
            }
            return;
        }
        displayReceiptAndContinueShopping(receipt);
    }

    private void displayReceiptAndContinueShopping(Receipt receipt) {
        outputView.printReceipt(receipt);
        if (inputView.readContinueShopping()) {
            startShoppingProcess();
        }
    }

    private void printError(String message) {
        System.out.println(ERROR_PREFIX + message);
    }
}