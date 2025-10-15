package controllers.components.load;

import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import okhttp3.*;
import javafx.scene.paint.Color;

import java.io.File;


public class HeaderController {
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

        JsonObject obj = ServerRequestUtils.sendGet("/currentUser");

        if (obj.has("state")) {
            String state = obj.get("state").getAsString();

            if ("SUCCESS".equals(state)) {
                String username = obj.has("username") ? obj.get("username").getAsString() : "Unknown";
                String credits = obj.has("credits") ? obj.get("credits").getAsString() : "0";

                userNameField.setText(username);
                creditsField.setText(credits);
            }
        } else {
            userNameField.setText("Unknown");
            creditsField.setText("0");
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

        JsonObject obj = ServerRequestUtils.sendPost("/loadProgram", body);

        if (obj != null && obj.has("state")) {
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
    }


        private void onChargeCredits () {
            String user = userNameField.getText();
            String amountCredits = creditsInputField.getText();

            RequestBody body = new FormBody.Builder()
                    .add("user", user)
                    .add("credits", amountCredits)
                    .add("action", "DEDUCT")
                    .build();

            JsonObject obj = ServerRequestUtils.sendPost("/chargeCredits", body);

            if (obj != null && "SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) {
                String newCredits = obj.has("credits") ? obj.get("credits").getAsString() : "";
                creditsField.setText(newCredits);
            }
        }

        private void onAdd500Credits(){
            String user=userNameField.getText();

            RequestBody body = new FormBody.Builder()
                    .add("user", user)
                    .add("credits", "500")
                    .add("action", "ADD")
                    .build();

            JsonObject obj=ServerRequestUtils.sendPost("/chargeCredits", body);
            if (obj != null && "SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) {
                String newCredits = obj.has("credits") ? obj.get("credits").getAsString() : "";
                creditsField.setText(newCredits);
            }
        }
    }




