package store;

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

    private void run() {
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
}