package controllers.components.dashboardScreen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import controllers.screens.ScreenManager;
import controllers.utils.refreshers.GenericRefresher;
import controllers.utils.refreshers.TimerManager;
import controllers.utils.server.ServerRequestUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import logic.system.programs.functions.info.FunctionInfo;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Timer;

import static controllers.screens.ScreenManager.DASHBOARD;

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
        TimerManager.register(DASHBOARD, timer);

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
            System.out.println("[Dashboard] Selecting function = " + selectedFunction.name());
            JsonElement response = ServerRequestUtils.sendPost("/selected", body);

            if (response != null && response.isJsonObject()) {
                JsonObject obj = response.getAsJsonObject();
                if ("SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) {
                    Platform.runLater(() -> ScreenManager.showExecutionScreen());
                } else {
                    System.out.println("[Dashboard] Selection failed → " + obj.get("message").getAsString());
                }
            } else {
                System.out.println("[Dashboard] Selection request failed (null response)");
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