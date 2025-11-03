package controllers.components.dashboardScreen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import controllers.screens.ScreenManager;
import controllers.utils.refreshers.GenericRefresher;
import controllers.utils.server.ServerRequestUtils;
import controllers.utils.server.ServerResponseHandler;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import logic.system.user.history.userHstory.UserHistory;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Timer;


public class HistoryTableController {
    private Timer timer;
    private GenericRefresher<UserHistory> refresher;
    private final BooleanProperty autoUpdate=new SimpleBooleanProperty(true);

    @FXML private TableView<UserHistory> historyTable;
    @FXML private TableColumn<UserHistory, Integer> colRunID;
    @FXML private TableColumn<UserHistory, String> colType;
    @FXML private TableColumn<UserHistory, String> colName;
    @FXML private TableColumn<UserHistory, String> colArch;
    @FXML private TableColumn<UserHistory, Integer> colDegree;
    @FXML private TableColumn<UserHistory, Double> colYVal;
    @FXML private TableColumn<UserHistory, Long> colCycles;
    @FXML private Button btnShowStatus;
    @FXML private Button btnReRun;

    @FXML
    public void initialize(){
        System.out.println("[HistoryRefresher] started polling /historyUpdated...");
        setupColumns();
        btnShowStatus.setOnAction(e -> showStatus());
        btnReRun.setOnAction(e -> reRun());
        startRefresher();
    }

    private void setupColumns(){
        colRunID.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().runID()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().progType()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        colArch.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().architecture()));
        colDegree.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().runDegree()));
        colYVal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().yValue()));
        colCycles.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().totalCycles()));
    }

    public void startRefresher(){
        // Generic refresher to auto-update table
        Type listType=new TypeToken<List<UserHistory>>(){}.getType();
        refresher=new GenericRefresher<>(autoUpdate,"/historyUpdated", "/userHistory",
                historyTable,listType,null,true);

        timer=new Timer(true);
        timer.schedule(refresher,0,1000);
        System.out.println("[HistoryTable] Refresher started for /historyUpdated every 1s");

    }

    private void showStatus() {
        UserHistory selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ServerResponseHandler.showAlert("Error", "Select a run first.", Alert.AlertType.WARNING);
            return;
        }

        // Request final variables snapshot for selected run
        new Thread(() -> {
            JsonElement response = ServerRequestUtils.sendGet("/showHistoryStatus?runID=" + selected.runID());
            if (response == null || !response.isJsonObject()) return;

            JsonObject obj = response.getAsJsonObject();
            if (obj.has("finalVars")) {
                JsonObject vars = obj.getAsJsonObject("finalVars");

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Run #" + selected.runID());
                    alert.setHeaderText("Final Variables Snapshot");
                    StringBuilder content = new StringBuilder();

                    vars.entrySet().forEach(entry ->
                            content.append(entry.getKey())
                                    .append(" = ")
                                    .append(entry.getValue())
                                    .append("\n")
                    );

                    alert.setContentText(content.toString());
                    alert.showAndWait();
                });
            }
        }).start();
    }


    private void reRun() {
        UserHistory selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ServerResponseHandler.showAlert("Error", "Select a run first.", Alert.AlertType.WARNING);
            return;
        }

        String ownerUser = selected.executedBy();

    // Send selected run data to server → it will create Re-Run cookies
        new Thread(() -> {
            // 1) Send Re-Run cookies to server
            RequestBody body = new FormBody.Builder()
                    .add("runID", String.valueOf(selected.runID()))
                    .add("degree", String.valueOf(selected.runDegree()))
                    .add("progName", selected.name())
                    .add("progType", selected.progType())
                    .add("ownerUser", ownerUser)
                    .build();

            JsonElement cookieResp = ServerRequestUtils.sendPost("/setReRunCookies", body);
            if (cookieResp == null || !cookieResp.isJsonObject()) return;


            JsonObject cookieObj = cookieResp.getAsJsonObject();
            if (!"SUCCESS".equalsIgnoreCase(cookieObj.get("state").getAsString())) {
                Platform.runLater(() ->
                        ServerResponseHandler.showAlert("Error", "Failed to set ReRun cookies.", Alert.AlertType.ERROR)
                );
                return;
            }


            // 2) Activate Re-Run mode for user
            RequestBody activateBody = new FormBody.Builder()
                    .add("progName", selected.name())
                    .add("progType", selected.progType())
                    .add("degree", String.valueOf(selected.runDegree()))
                    .add("runID", String.valueOf(selected.runID()))
                    .add("ownerUser", ownerUser)
                    .build();

            JsonElement activateResp = ServerRequestUtils.sendPost("/activateReRun", activateBody);
            if (activateResp == null || !activateResp.isJsonObject()) return;


            JsonObject activateObj = activateResp.getAsJsonObject();
            if (!"SUCCESS".equalsIgnoreCase(activateObj.get("state").getAsString())) {
                Platform.runLater(() ->
                        ServerResponseHandler.showAlert("Error", "Failed to Activate Re-Run mode for user.", Alert.AlertType.ERROR)
                );
                return;
            }
            Platform.runLater(ScreenManager::showExecutionScreen);
        }).start();
    }

}

