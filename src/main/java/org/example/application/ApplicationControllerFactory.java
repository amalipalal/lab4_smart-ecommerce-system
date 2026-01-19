package org.example.application;

import javafx.util.Callback;
import org.example.controller.order.AdminOrderController;
import org.example.controller.shell.BuyerShellController;
import org.example.controller.category.AdminCategoryController;
import org.example.controller.product.AdminProductController;

public class ApplicationControllerFactory implements Callback<Class<?>, Object> {

    private final ApplicationContext context;

    public ApplicationControllerFactory(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object call(Class<?> cls) {
        if (cls == AdminCategoryController.class) {
            return new AdminCategoryController(context.getCategoryService());
        } else if (cls == AdminProductController.class) {
            return new AdminProductController(context.getProductService(), context.getCategoryService());
        } else if (cls == AdminOrderController.class) {
            return new AdminOrderController(context.getPurchaseService());
        } else if (cls == BuyerShellController.class) {
            return new BuyerShellController(
                    context.getProductService(),
                    context.getCategoryService(),
                    context.getPurchaseService(),
                    context.getReviewService()
            );
        }

        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create controller: " + cls.getName(), e);
        }
    }
}
