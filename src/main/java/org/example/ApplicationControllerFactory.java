package org.example;

import javafx.util.Callback;
import org.example.controller.category.AdminCategoryController;

public class ApplicationControllerFactory implements Callback<Class<?>, Object> {

    private final ApplicationContext context;

    public ApplicationControllerFactory(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object call(Class<?> cls) {
        if (cls == AdminCategoryController.class) {
            return new AdminCategoryController(context.getCategoryService());
        }

        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create controller: " + cls.getName(), e);
        }
    }
}
