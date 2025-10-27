package controllers.components.executionScreen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import controllers.utils.server.ServerRequestUtils;
import controllers.utils.server.ServerResponseHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.lang.reflect.Type;
import java.util.List;

public class DegreeActionsController {
    @FXML private Button btnExpand;
    @FXML private Button btnCollapse;
    @FXML private Label lblDegree;
    @FXML private ComboBox<String>cmbHighlight;

    private int currentDegree=0;
    private int maxDegree=0;

    @FXML
    private void initialize(){
        loadDegreeFromServer();
        btnExpand.setOnAction(e->updateDegree(currentDegree+1));
        btnCollapse.setOnAction(e -> updateDegree(currentDegree - 1));
        refreshDegree();
    }

    private void loadDegreeFromServer(){
        new Thread(() -> {
            JsonElement response = ServerRequestUtils.sendGet("/programData");
            Platform.runLater(() -> {
                if (response != null && response.isJsonObject()) {
                    JsonObject obj = response.getAsJsonObject();

                    if ("SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) {
                        currentDegree = obj.get("currentDegree").getAsInt();
                        maxDegree = obj.get("maxDegree").getAsInt();
                        refreshDegree();
                        refreshHighlightList(currentDegree);
                    }
                } else {
                    ServerResponseHandler.showAlert("Error", "Failed to load degree info.", Alert.AlertType.ERROR);
                }
            });
        }).start();
    }

    private void updateDegree(int newDegree){
        if(newDegree<0 || newDegree>maxDegree) return;

        RequestBody body=new FormBody.Builder()
                .add("degree",String.valueOf(newDegree))
                .build();

        new Thread(()->{
            JsonElement response= ServerRequestUtils.sendPost("/degreeAction",body);

            Platform.runLater(()->{
                if(response!=null && response.isJsonObject()){
                    JsonObject obj=response.getAsJsonObject();
                    if("SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) {
                        currentDegree = obj.get("degree").getAsInt();
                        refreshDegree();
                        refreshHighlightList(currentDegree);

                        clearHistoryChain();
                    }
                }else{
                    ServerResponseHandler.showAlert("ERROR","Server error updatind degree.", Alert.AlertType.ERROR);
                }
            });
        }).start();
    }

    private void refreshDegree(){
        lblDegree.setText((currentDegree+" / "+maxDegree));
    }

    private void refreshHighlightList(int degree){
        RequestBody body = new FormBody.Builder()
                .add("degree", String.valueOf(currentDegree))
                .build();

        new Thread(()->{
            JsonElement response = ServerRequestUtils.sendPost("/programVariables",body);
            if (response == null || !response.isJsonObject()) return;
            var obj = response.getAsJsonObject();
            if (!obj.has("variables")) return;

            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> vars = new Gson().fromJson(obj.get("variables"), listType);

            Platform.runLater(() -> {
                cmbHighlight.getItems().setAll(vars);
                cmbHighlight.setPromptText("Highlight");
            });
        }).start();
    }

    private void clearHistoryChain(){
        new Thread(()->{
            RequestBody body=new FormBody.Builder()
                    .add("clear","true")
                    .build();

            ServerRequestUtils.sendPost("/historyChain",body);
        }).start();
    }
}
