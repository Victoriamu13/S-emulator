package controllers.components.dashboardScreen;

import com.google.gson.reflect.TypeToken;
import controllers.utils.refreshers.GenericRefresher;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import logic.system.user.history.userHstory.UserHistory;

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

    @FXML
    public void initialize(){
        colRunID.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().runID()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().progType()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        colArch.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().architecture()));
        colDegree.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().runDegree()));
        colYVal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().yValue()));
        colCycles.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().totalCycles()));

        startRefresher();
    }

    public void startRefresher(){
        Type listType=new TypeToken<List<UserHistory>>(){}.getType();
        refresher=new GenericRefresher<>(autoUpdate,"/historyUpdated", "/userHistory",
                historyTable,listType);

        timer=new Timer(true);
        timer.schedule(refresher,0,2000);
    }
}

