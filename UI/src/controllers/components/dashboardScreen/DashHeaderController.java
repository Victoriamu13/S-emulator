package controllers.components.dashboardScreen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;
import controllers.utils.server.ServerResponseHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import logic.system.api.SelectedClientState;
import okhttp3.*;
import javafx.scene.paint.Color;

import java.io.File;


public class DashHeaderController {
    @FXML private Button btnLoadFile;
    @FXML private TextField filePathField;
    @FXML private Label loadStatusLabel;
    @FXML private TextField userNameField;
    @FXML private Button btnChargeCredits;
    @FXML private TextField creditsField;
    @FXML private TextField creditsInputField;

    @FXML
    private void initialize() {
        btnLoadFile.setOnAction(e -> onLoadFile());
        btnChargeCredits.setOnAction(e -> onChargeCredits());

        JsonElement response = ServerRequestUtils.sendGet("/currentUser");

        if (response != null && response.isJsonObject()) {
            JsonObject obj = response.getAsJsonObject();

            if (obj.has("state")) {
                String state = obj.get("state").getAsString();

                if ("SUCCESS".equalsIgnoreCase(state)) {
                    String username = obj.get("username").getAsString();
                    int credits = obj.get("credits").getAsInt();

                    userNameField.setText(username);
                    creditsField.setText(String.valueOf(credits));

                    SelectedClientState.setCurrentUser(username);
                    SelectedClientState.setCurrentCredits(credits);
                } else {
                    userNameField.setText("Unknown");
                    creditsField.setText("0");
                }
            }
        } else {
            ServerResponseHandler.showAlert("Error", "No response from server.", Alert.AlertType.ERROR);
        }
    }


    private void onLoadFile() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File f = fc.showOpenDialog(btnLoadFile.getScene().getWindow());
        if (f == null) return;

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", f.getName(), RequestBody.create(f, MediaType.parse("application/xml")))
                .addFormDataPart("user", userNameField.getText())
                .build();

        JsonElement response = ServerRequestUtils.sendPost("/loadProgram", body);

        if (response != null && response.isJsonObject()) {
            JsonObject obj = response.getAsJsonObject();

            if (obj.has("state")) {
                String state = obj.get("state").getAsString();

                if ("SUCCESS".equalsIgnoreCase(state)) {
                    filePathField.setText(f.getAbsolutePath());
                    loadStatusLabel.setTextFill(Color.GREEN);
                    loadStatusLabel.setText("File loaded successfully.");
                } else {
                    String errorMsg = obj.has("message") ? obj.get("message").getAsString() : "Unknown error.";
                    loadStatusLabel.setTextFill(Color.RED);
                    loadStatusLabel.setText("Error: " + errorMsg);

                }
            }
        } else {
            ServerResponseHandler.showAlert("Error", "No response from server.", Alert.AlertType.ERROR);
        }
    }


    private void onChargeCredits() {
        String amountCredits = creditsInputField.getText();

        int amount;
        try {
            amount = Integer.parseInt(amountCredits);
            if (amount <= 0) {
                ServerResponseHandler.showAlert("Input Error", "Please enter a positive number.", Alert.AlertType.WARNING);
                return;
            }
        } catch (NumberFormatException e) {
            ServerResponseHandler.showAlert("Input Error", "Invalid number format.", Alert.AlertType.WARNING);
            return;
        }

        new Thread(() -> {
            JsonElement userResp = ServerRequestUtils.sendGet("/currentUser");
            if (userResp == null || !userResp.isJsonObject()) return;

            JsonObject userObj = userResp.getAsJsonObject();
            if (!"SUCCESS".equalsIgnoreCase(userObj.get("state").getAsString())) {
                Platform.runLater(() ->
                        ServerResponseHandler.showAlert("Error", "No active user session.", Alert.AlertType.ERROR));
                return;
            }

            String user = userObj.get("username").getAsString();
            RequestBody body = new FormBody.Builder().add("user", user).add("credits", String.valueOf(amount))
                    .add("action", "ADD").build();

            JsonElement resp = ServerRequestUtils.sendPost("/chargeCredits", body);
            if (resp == null || !resp.isJsonObject()) return;

            JsonObject obj = resp.getAsJsonObject();
            String state = obj.has("state") ? obj.get("state").getAsString() : "";

            if ("SUCCESS".equalsIgnoreCase(state)) {
                String newCredits = obj.has("credits") ? obj.get("credits").getAsString() : "";
                Platform.runLater(() -> {
                    creditsField.setText(newCredits);
                    ServerResponseHandler.showAlert("Credits Updated",
                            "Successfully added " + amount + " credits.\nNew balance: " + newCredits,
                            Alert.AlertType.INFORMATION);
                });
            } else {
                String msg = obj.has("message") ? obj.get("message").getAsString() : "Operation failed.";
                Platform.runLater(() ->
                        ServerResponseHandler.showAlert("Error", msg, Alert.AlertType.ERROR));
            }
        }).start();
    }
}




