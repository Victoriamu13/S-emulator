package controllers.components.executionScreen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;
import controllers.utils.server.ServerResponseHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import logic.system.api.SelectedClientState;

import java.util.Timer;
import java.util.TimerTask;

    public class ExecHeaderController {
        @FXML private TextField userNameField;
        @FXML private TextField creditsField;
        private final Timer creditsTimer = new Timer(true);

        @FXML
        private void initialize() {
            loadUserInfoFromServer();
            startCreditsAutoRefresh();
        }

        private void loadUserInfoFromServer() {
            JsonElement response = ServerRequestUtils.sendGet("/currentUser");

            if (response == null || !response.isJsonObject()) {
                ServerResponseHandler.showAlert("Error", "No response from server.", Alert.AlertType.ERROR);
                return;
            }

            JsonObject obj = response.getAsJsonObject();
            String state = obj.has("state") ? obj.get("state").getAsString() : "ERROR";

            if (!"SUCCESS".equalsIgnoreCase(state)) {
                userNameField.setText("Unknown");
                creditsField.setText("0");
                return;
            }

            String username = obj.has("username") ? obj.get("username").getAsString() : "Unknown";
            int credits = obj.has("credits") ? obj.get("credits").getAsInt() : 0;

            userNameField.setText(username);
            creditsField.setText(String.valueOf(credits));

            SelectedClientState.setCurrentUser(username);
            SelectedClientState.setCurrentCredits(credits);
        }

        private void startCreditsAutoRefresh() {
            creditsTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int currentCredits = SelectedClientState.getCurrentCredits();
                    Platform.runLater(() ->
                            creditsField.setText(String.valueOf(currentCredits))
                    );
                }
            }, 1000, 2000);
        }
    }

