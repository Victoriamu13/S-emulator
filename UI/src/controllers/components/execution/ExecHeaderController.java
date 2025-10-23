package controllers.components.execution;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;
import controllers.utils.server.ServerResponseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class ExecHeaderController {
    @FXML private TextField userNameField;
    @FXML private TextField creditsField;

    @FXML
    private void initialize() {
        JsonElement response = ServerRequestUtils.sendGet("/currentUser");

        if (response != null && response.isJsonObject()) {
            JsonObject obj = response.getAsJsonObject();

            if (obj.has("state")) {
                String state = obj.get("state").getAsString();

                if ("SUCCESS".equalsIgnoreCase(state)) {
                    String username = obj.has("username") ? obj.get("username").getAsString() : "Unknown";
                    String credits = obj.has("credits") ? obj.get("credits").getAsString() : "0";

                    userNameField.setText(username);
                    creditsField.setText(credits);
                } else {
                    userNameField.setText("Unknown");
                    creditsField.setText("0");
                }
            }
        } else {
            ServerResponseHandler.showAlert("Error", "No response from server.", Alert.AlertType.ERROR);
        }
    }

}
