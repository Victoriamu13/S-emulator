package controllers.components.executionScreen;

import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;

public class ExecutionScreenController {
    private static final double DIVIDER_POSITION = 0.5;
    @FXML private SplitPane mainSplitPane;

    @FXML
    private void initialize(){
        lockDivider(mainSplitPane);
    }

    private void lockDivider(SplitPane splitPane) {
        SplitPane.Divider divider = splitPane.getDividers().get(0);
        divider.setPosition(ExecutionScreenController.DIVIDER_POSITION);

        divider.positionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() != ExecutionScreenController.DIVIDER_POSITION) {
                divider.setPosition(ExecutionScreenController.DIVIDER_POSITION);
            }
        });
    }
}
