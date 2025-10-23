package controllers.components.dashboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;
import controllers.utils.server.ServerResponseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import okhttp3.*;
import javafx.scene.paint.Color;

import java.io.File;


public class DashHeaderController {
    @FXML private Button btnLoadFile;
    @FXML private TextField filePathField;
    @FXML private Label loadStatusLabel;
    @FXML private TextField userNameField;
    @FXML private Button btnChargeCredits;
    @FXML private Button btnAdd500Credits;
    @FXML private TextField creditsField;
    @FXML private TextField creditsInputField;

    @FXML
    private void initialize() {
        btnLoadFile.setOnAction(e -> onLoadFile());
        btnChargeCredits.setOnAction(e -> onChargeCredits());
        btnAdd500Credits.setOnAction(e -> onAdd500Credits());

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
        String user = userNameField.getText();
        String amountCredits = creditsInputField.getText();

        RequestBody body = new FormBody.Builder()
                .add("user", user)
                .add("credits", amountCredits)
                .add("action", "DEDUCT")
                .build();

        JsonElement response = ServerRequestUtils.sendPost("/chargeCredits", body);
        if (response != null && response.isJsonObject()) {
            JsonObject obj = response.getAsJsonObject();

            if (obj.has("state")) {
                String state = obj.get("state").getAsString();

                if ("SUCCESS".equalsIgnoreCase(state)) {
                    String newCredits = obj.has("credits") ? obj.get("credits").getAsString() : "";
                    creditsField.setText(newCredits);
                }
            }
        } else {
            ServerResponseHandler.showAlert("Error", "No response from server.", Alert.AlertType.ERROR);
        }
    }

    private void onAdd500Credits() {
        String user = userNameField.getText();

        RequestBody body = new FormBody.Builder()
                .add("user", user)
                .add("credits", "500")
                .add("action", "ADD")
                .build();

        JsonElement response = ServerRequestUtils.sendPost("/chargeCredits", body);
        if (response != null && response.isJsonObject()) {
            JsonObject obj = response.getAsJsonObject();
            if (obj.has("state")) {
                String state = obj.get("state").getAsString();

                if ("SUCCESS".equalsIgnoreCase(state)) {
                    String newCredits = obj.has("credits") ? obj.get("credits").getAsString() : "";
                    creditsField.setText(newCredits);
                }

            } else {
                ServerResponseHandler.showAlert("Error", "No response from server.", Alert.AlertType.ERROR);
            }
        }
    }
}




