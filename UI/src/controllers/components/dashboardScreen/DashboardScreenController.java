package controllers.components.dashboardScreen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.screens.ScreenManager;
import controllers.utils.client.SelectedClientState;
import controllers.utils.server.ServerRequestUtils;
import controllers.utils.server.ServerResponseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import okhttp3.RequestBody;


public class DashboardScreenController {
    private static final double DIVIDER_POSITION = 0.6;
    @FXML
    private SplitPane mainSplitPane;
    @FXML
    private Button btnLogOut;

    @FXML
    private void initialize() {
        lockDivider(mainSplitPane);
        btnLogOut.setOnAction(e -> onLogout());
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

    private void onLogout() {
        JsonElement response = ServerRequestUtils.sendPost("/logout", RequestBody.create(new byte[0]));
        if (response != null && response.isJsonObject()) {
            JsonObject obj = response.getAsJsonObject();

            if (obj.has("state") && obj.get("state").isJsonPrimitive()
                    && "SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) {
                SelectedClientState.clear();
                ScreenManager.showLoginScreen();
            } else {
                ServerResponseHandler.showAlert("Logout Error", "An error accured while logging out.", Alert.AlertType.ERROR);
            }
        }
    }
}

