package controllers.components.root;

import controllers.components.load.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;


public class UsersScreenController {
    private static final double DIVIDER_POSITION = 0.6;

    private void lockDivider(SplitPane splitPane) {
        SplitPane.Divider divider = splitPane.getDividers().get(0);
        divider.setPosition(UsersScreenController.DIVIDER_POSITION);

        divider.positionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() != UsersScreenController.DIVIDER_POSITION) {
                divider.setPosition(UsersScreenController.DIVIDER_POSITION);
            }
        });
    }
}

