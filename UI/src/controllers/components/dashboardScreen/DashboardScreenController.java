package controllers.components.dashboardScreen;

import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;


public class DashboardScreenController {
    private static final double DIVIDER_POSITION = 0.6;
    @FXML private SplitPane mainSplitPane;

    @FXML
    private void initialize(){
        lockDivider(mainSplitPane);
    }

    private void lockDivider(SplitPane splitPane) {
        SplitPane.Divider divider = splitPane.getDividers().get(0);
        divider.setPosition(DashboardScreenController.DIVIDER_POSITION);

        divider.positionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() != DashboardScreenController.DIVIDER_POSITION) {
                divider.setPosition(DashboardScreenController.DIVIDER_POSITION);
            }
        });
    }
}

