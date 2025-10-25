package controllers.utils.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.client.HttpClientProvider;
import javafx.scene.control.Alert;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class ServerRequestUtils {
    private static final OkHttpClient client= HttpClientProvider.getClient();
    private static final String SERVER_URL = "http://localhost:8080";

    // ==== Generic POST ====
    public static JsonElement sendPost(String endpoint, RequestBody body){
        Request request=new Request.Builder()
                .url(SERVER_URL+endpoint)
                .post(body)
                .build();
        return handleResponse(request);
    }

    // ==== Generic GET ====
    public static JsonElement sendGet(String endpoint){
       Request request=new Request.Builder()
               .url(SERVER_URL+endpoint)
               .get()
               .build();
       return handleResponse(request);
    }



    // ==== Helper Func ====
    private static JsonElement handleResponse(Request request){
        try(Response response=client.newCall(request).execute()){

            if(response.isSuccessful() && response.body()!=null){

                String responseBody=response.body().string();

                JsonElement json=ServerResponseHandler.gson.fromJson(responseBody,JsonElement.class);

                if (json.isJsonObject()) {
                    JsonObject obj = json.getAsJsonObject();

                    if (obj.has("state") && obj.get("state").isJsonPrimitive()
                            && "ERROR".equalsIgnoreCase(obj.get("state").getAsString())) {

                        String message = obj.has("message")
                                ? obj.get("message").getAsString()
                                : "Unknown server error";

                        ServerResponseHandler.showAlert("Server Error", message, Alert.AlertType.ERROR);
                    }
                }
                return json;

            } else {
                ServerResponseHandler.showAlert("Error", "Server error: " + response.code(), Alert.AlertType.ERROR);
            }

        } catch (IOException e) {
            ServerResponseHandler.showAlert("Error", "Connection failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        return null;
    }

}
