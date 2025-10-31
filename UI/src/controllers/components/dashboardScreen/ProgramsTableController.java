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
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import logic.system.programs.info.ProgramsInfo;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Timer;


public class ProgramsTableController{
    Timer timer;
    private GenericRefresher<ProgramsInfo> refresher;
    private final BooleanProperty autoUpdate = new SimpleBooleanProperty(true);

    @FXML private TableView<ProgramsInfo> programsTable;
    @FXML private TableColumn<ProgramsInfo,String> colName;
    @FXML private TableColumn<ProgramsInfo,String> colUploadedBy;
    @FXML private TableColumn<ProgramsInfo,Integer> colInstructionCount;
    @FXML private TableColumn<ProgramsInfo,Integer> colMaxDegree;
    @FXML private TableColumn<ProgramsInfo,Integer> colRunCount;
    @FXML private TableColumn<ProgramsInfo,Number> colAvgCreditCost;
    @FXML private Button btnRunProgram;

    @FXML
    public void initialize() {
        setupColumns();
        btnRunProgram.setOnAction(e->onRunProgram());
        startRefresher();
    }

    private void setupColumns(){
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        colUploadedBy.setCellValueFactory(c->new SimpleStringProperty(c.getValue().uploader()));
        colInstructionCount.setCellValueFactory(c->new SimpleObjectProperty<>(c.getValue().instCount()));
        colMaxDegree.setCellValueFactory(c->new SimpleObjectProperty<>(c.getValue().maxDegree()));
        colRunCount.setCellValueFactory(c->new SimpleObjectProperty<>(c.getValue().numExecutions()));
        colAvgCreditCost.setCellValueFactory(c->new SimpleDoubleProperty(c.getValue().avgCreditCost()));
    }

    private void onRunProgram(){
        ProgramsInfo selectedProgram=programsTable.getSelectionModel().getSelectedItem();
        if(selectedProgram==null) return;

        RequestBody body=new FormBody.Builder()
                .add("type","program")
                .add("name", selectedProgram.name())
                .build();

        new Thread(()->{
            System.out.println("[Dashboard] Selecting program = " + selectedProgram.name());
            JsonElement response =ServerRequestUtils.sendPost("/selected", body);

            if (response != null && response.isJsonObject()) {
                JsonObject obj = response.getAsJsonObject();

                if ("SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) {
                    boolean ready = obj.get("ready").getAsBoolean();
                    if (ready) {
                        SelectedClientState.setProgram(selectedProgram.name());
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

    public void startRefresher() {
       Type listType=new TypeToken<List<ProgramsInfo>>(){}.getType();
       refresher=new GenericRefresher<>(autoUpdate,"/programsUpdated", "/programsList",
               programsTable,listType);

       timer=new Timer(true);
        timer.schedule(refresher,0,2000);
    }


}
