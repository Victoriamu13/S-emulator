package controllers.utils.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.scene.control.Alert;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ServerResponseHandler {
    public static final Gson gson=new Gson();

    public static JsonObject executeRequest(OkHttpClient client, Request request){
        try(Response response = client.newCall(request).execute()){

            if(response.isSuccessful() && response.body()!=null){
                String responseBody=response.body().string();
                return gson.fromJson(responseBody, JsonObject.class);
            } else {
                showAlert("Error", "Server error: " + response.code(), Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Error", "Connection failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        return null; //case of error
    }


    public static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
