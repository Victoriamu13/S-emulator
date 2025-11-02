package controllers.components.dashboardScreen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import controllers.screens.ScreenManager;
import controllers.utils.client.SelectedClientState;
import controllers.utils.refreshers.GenericRefresher;
import controllers.utils.refreshers.TimerManager;
import controllers.utils.server.ServerRequestUtils;
import controllers.utils.server.ServerResponseHandler;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import logic.system.programs.functions.info.FunctionInfo;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Timer;

public class FunctionsTableController {
    private Timer timer;
    private GenericRefresher<FunctionInfo> refresher;
    private final BooleanProperty autoUpdate=new SimpleBooleanProperty(true);

    @FXML private TableView<FunctionInfo> functionsTable;
    @FXML private TableColumn<FunctionInfo,String>colName;
    @FXML private TableColumn<FunctionInfo,String> colProgramName;
    @FXML private TableColumn<FunctionInfo, String> colUploader;
    @FXML private TableColumn<FunctionInfo, Integer> colInstCount;
    @FXML private TableColumn<FunctionInfo, Integer> colMaxDegree;
    @FXML private Button btnRunAsProgram;


    @FXML
    public void initialize(){
        setupColumns();
        btnRunAsProgram.setOnAction(e-> onRunAsProgram());
        startRefresher();
    }

    private void setupColumns(){
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        colProgramName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().programName()));
        colUploader.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().uploader()));
        colInstCount.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().instructionCount()));
        colMaxDegree.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().maxExpansionDegree()));
    }

    private void onRunAsProgram(){
        FunctionInfo selectedFunction=functionsTable.getSelectionModel().getSelectedItem();
        if(selectedFunction==null) return;

        RequestBody body=new FormBody.Builder()
                .add("type", "function")
                .add("name", selectedFunction.name())
                .build();

        new Thread(()->{
            System.out.println("[Dashboard] Selecting program = " + selectedFunction.name());
            JsonElement response =ServerRequestUtils.sendPost("/selected", body);

            if (response != null && response.isJsonObject()) {
                JsonObject obj = response.getAsJsonObject();

                if ("SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) {
                    boolean ready = obj.get("ready").getAsBoolean();
                    if (ready) {
                        SelectedClientState.setProgram(selectedFunction.name());
                        Platform.runLater(() -> ScreenManager.showExecutionScreen());
                    } else {
                        ServerResponseHandler.showAlert("Warning", "Engine not ready yet.", Alert.AlertType.WARNING);
                    }
                }
            }else{
                ServerResponseHandler.showAlert("ERROR",
                        "Failed to contact server. Please try again.", Alert.AlertType.ERROR);
            }
        }).start();
    }

    private void startRefresher(){
        Type listType=new TypeToken<List<FunctionInfo>>(){}.getType();

        refresher=new GenericRefresher<>(autoUpdate,"/functionsUpdated","/functionsList",
                functionsTable,listType);

        timer=new Timer(true);
        timer.schedule(refresher,0,2000);
    }
}