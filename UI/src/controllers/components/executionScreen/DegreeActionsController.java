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
import java.util.Timer;
import java.util.TimerTask;

public class DegreeActionsController {
    @FXML private Button btnExpand;
    @FXML private Button btnCollapse;
    @FXML private Label lblDegree;
    @FXML private ComboBox<String>cmbHighlight;

    private int currentDegree=0;
    private int maxDegree=0;

    @FXML
    private void initialize(){
        loadDegreeFromServer(); // Load initial degree info from server
        btnExpand.setOnAction(e->updateDegree(currentDegree+1));
        btnCollapse.setOnAction(e -> updateDegree(currentDegree - 1));

        // Handle highlight selection change
        cmbHighlight.setOnAction(e -> {
            String selected = cmbHighlight.getSelectionModel().getSelectedItem();
            updateHighlight(selected);
        });

        setupHighlightComboBoxPlaceholder();      // Add placeholder text "Highlight"
        refreshDegree();   // Display initial degree value
        startProgramVarsRefresher();

    }

    // Sets up placeholder text for ComboBox when nothing is selected
    private void setupHighlightComboBoxPlaceholder() {
        cmbHighlight.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.isBlank()) {
                    setText("Highlight");
                    setStyle("-fx-text-fill: -fx-text-inner-color; -fx-opacity: 0.6;");
                } else {
                    setText(item);
                    setStyle("");
                }
            }
        });
    }

    // Loads the current and max degree from the server
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
                        loadInitialVariables();
                    }
                } else {
                    ServerResponseHandler.showAlert("Error", "Failed to load degree info.", Alert.AlertType.ERROR);
                }
            });
        }).start();
    }

    private void loadInitialVariables() {
        RequestBody body = new FormBody.Builder()
                .add("degree", String.valueOf(currentDegree))
                .build();
        new Thread(() -> {
            JsonElement response = ServerRequestUtils.sendPost("/programVariables", body);
            if (response == null || !response.isJsonObject()) return;
            var obj = response.getAsJsonObject();

            if (!obj.has("variables")) return;

            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> vars = new Gson().fromJson(obj.get("variables"), listType);
            Platform.runLater(() -> cmbHighlight.getItems().setAll(vars));
        }).start();
    }

    // Sends new degree to the server (expand/collapse)
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

                        cmbHighlight.getSelectionModel().clearSelection();
                        cmbHighlight.setValue(null);

                        new Thread(() -> {
                            RequestBody clearBody = new FormBody.Builder()
                                    .add("variable", "")
                                    .build();
                            ServerRequestUtils.sendPost("/highlightVariable", clearBody);
                        }).start();

                        clearHistoryChain();

                        new Thread(() -> {
                            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
                            loadInitialVariables();
                        }).start();
                    }
                }else{
                    ServerResponseHandler.showAlert("ERROR","Server error updatind degree.", Alert.AlertType.ERROR);
                }
            });
        }).start();
    }

    // Updates the degree label text
    private void refreshDegree(){
        lblDegree.setText((currentDegree+" / "+maxDegree));
    }


    private void startProgramVarsRefresher() {
        Timer varsTimer = new Timer(true);
        varsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement resp = ServerRequestUtils.sendGet("/programVariablesUpdated");
                if (resp == null || !resp.isJsonObject()) return;

                boolean updated = resp.getAsJsonObject().get("updated").getAsBoolean();
                if (!updated) return;

                RequestBody body = new FormBody.Builder()
                        .add("degree", String.valueOf(currentDegree))
                        .build();

                JsonElement varsResponse = ServerRequestUtils.sendPost("/programVariables", body);
                if (varsResponse == null || !varsResponse.isJsonObject()) return;

                var obj = varsResponse.getAsJsonObject();
                if (!obj.has("variables")) return;

                Type listType = new TypeToken<List<String>>(){}.getType();
                List<String> newVars = new Gson().fromJson(obj.get("variables"), listType);

                Platform.runLater(() -> {
                    String selectedBefore = cmbHighlight.getSelectionModel().getSelectedItem();

                    cmbHighlight.getItems().setAll(newVars);

                    if (selectedBefore != null && newVars.contains(selectedBefore)) {
                        cmbHighlight.getSelectionModel().select(selectedBefore);
                    } else {
                        cmbHighlight.getSelectionModel().clearSelection();
                    }
                });
            }
        }, 0, 1000);
    }

    // Clears highlight selection (after degree change)
    private void clearHighlight() {
        Platform.runLater(() -> {
            cmbHighlight.getSelectionModel().clearSelection();
            cmbHighlight.setValue(null);
        });
        new Thread(() -> {
            // also clear highlight on server side
            RequestBody body = new FormBody.Builder()
                    .add("variable", "")
                    .build();
            ServerRequestUtils.sendPost("/highlightVariable", body);
        }).start();
    }

    // Clears the history chain on the previous chosen instruction
    private void clearHistoryChain(){
        new Thread(()->{
            RequestBody body=new FormBody.Builder()
                    .add("clear","true")
                    .build();

            ServerRequestUtils.sendPost("/historyChain",body);
        }).start();
    }

    // Sends selected variable name to the server to mark highlight
    private void updateHighlight(String variable) {
        new Thread(() -> {
            RequestBody body = new FormBody.Builder()
                    .add("variable", variable == null ? "" : variable)
                    .build();
            ServerRequestUtils.sendPost("/highlightVariable", body);
        }).start();
    }
}