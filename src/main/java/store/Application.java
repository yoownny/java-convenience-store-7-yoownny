package store;

import store.view.InputView;
import store.view.OutputView;

public class Application {
    private final InputView inputView;
    private final OutputView outputView;

    public Application() {
        this.inputView = new InputView();
        this.outputView = new OutputView();
    }

    public static void main(String[] args) {
        new Application().run();
    }

    private void run() {
        do {
            inputView.start();
            inputView.readItem();
            inputView.readMembershipOption();
        } while (inputView.readContinueShopping());
    }
}