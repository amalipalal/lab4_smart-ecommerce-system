package org.example.ui;

import java.util.function.Consumer;

public record ActionDefinition<T>(
        String iconName,
        int iconSize,
        String styleClass,
        Consumer<T> action
) {}
