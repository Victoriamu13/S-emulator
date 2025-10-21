package controllers.components;

import javafx.scene.control.SplitPane;


public class FirstScreenController {
    private static final double DIVIDER_POSITION = 0.6;

    private void lockDivider(SplitPane splitPane) {
        SplitPane.Divider divider = splitPane.getDividers().get(0);
        divider.setPosition(FirstScreenController.DIVIDER_POSITION);

        divider.positionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() != FirstScreenController.DIVIDER_POSITION) {
                divider.setPosition(FirstScreenController.DIVIDER_POSITION);
            }
        });
    }
}

