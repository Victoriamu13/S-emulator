package controllers.components;

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

        JsonObject obj = ServerRequestUtils.sendPost("/login", body);

        if(obj!=null && "SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())){
            ScreenManager.showFirstScreen();
        }
    }
}
