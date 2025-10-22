package controllers.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.screens.ScreenManager;
import controllers.utils.server.ServerRequestUtils;
import controllers.utils.server.ServerResponseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import okhttp3.FormBody;
import okhttp3.RequestBody;


public class LoginController {
    @FXML private TextField userNameField;

    @FXML
    private void onLoginClicked(){
        String username=userNameField.getText().trim();

        if(username.isEmpty()){
            ServerResponseHandler.showAlert("Input Error", "Please enter your username", Alert.AlertType.WARNING);
            return;
        }

        RequestBody body=new FormBody.Builder()
                .add("user",username)
                .build();

        JsonElement response = ServerRequestUtils.sendPost("/login", body);

        if(response!=null && response.isJsonObject()){
            JsonObject obj=response.getAsJsonObject();

            if (obj.has("state") && obj.get("state").isJsonPrimitive()
                    && "SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) {
                ScreenManager.showDashboardScreen();
            } else {
                String message = obj.has("message") ? obj.get("message").getAsString() : "Login failed.";
                ServerResponseHandler.showAlert("Login Failed", message, Alert.AlertType.ERROR);
            }
        }else{
            ServerResponseHandler.showAlert("Error", "No response from server.", Alert.AlertType.ERROR);
        }
    }
}
