package controllers.components;

import com.google.gson.reflect.TypeToken;
import controllers.utils.refreshers.GenericRefresher;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import logic.system.programs.info.ProgramsInfo;

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

    @FXML
    public void initialize() {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        colUploadedBy.setCellValueFactory(c->new SimpleStringProperty(c.getValue().uploader()));
        colInstructionCount.setCellValueFactory(c->new SimpleObjectProperty<>(c.getValue().instCount()));
        colMaxDegree.setCellValueFactory(c->new SimpleObjectProperty<>(c.getValue().maxDegree()));
        colRunCount.setCellValueFactory(c->new SimpleObjectProperty<>(c.getValue().numExecutions()));
        colAvgCreditCost.setCellValueFactory(c->new SimpleDoubleProperty(c.getValue().avgCreditCost()));

        startRefresher();
    }

    public void startRefresher() {
       Type listType=new TypeToken<List<ProgramsInfo>>(){}.getType();
       refresher=new GenericRefresher<>(autoUpdate,"/programsUpdated", "/programsList",
               programsTable,listType);

       timer=new Timer(true);
       timer.schedule(refresher,0,2000);
    }


}
