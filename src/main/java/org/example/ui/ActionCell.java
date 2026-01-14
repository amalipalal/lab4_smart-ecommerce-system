package org.example.ui;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class ActionCell<T> extends TableCell<T, Void> {
    private final HBox container = new HBox(5);

    public ActionCell(List<ActionDefinition<T>> actions) {
        for (ActionDefinition<T> def : actions) {
            Button btn = new Button();
            FontIcon icon = new FontIcon(def.iconName());
            icon.setIconSize(def.iconSize());
            btn.setGraphic(icon);
            btn.getStyleClass().add(def.styleClass());
            if(def.action() != null)
                btn.setOnAction(e -> def.action().accept(getTableView().getItems().get(getIndex())));
            container.getChildren().add(btn);
        }
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : container);
    }
}

