package controllers.components.execution;

import com.google.gson.reflect.TypeToken;
import controllers.utils.refreshers.GenericRefresher;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import logic.engineFacade.model.InstructionDTO;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Timer;

public class InstructionsTableController {
    Timer timer;
    private GenericRefresher<InstructionDTO> refresher;
    private final BooleanProperty autoUpdate=new SimpleBooleanProperty(true);

    @FXML private TableView<InstructionDTO> instructionsTable;
    @FXML private TableColumn<InstructionDTO, Number> colIndex;
    @FXML private TableColumn<InstructionDTO, String> colType;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colCommand;
    @FXML private TableColumn<InstructionDTO, String> colCycles;

    @FXML
    public void initialize() {
        colIndex.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().index()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type()));
        colLabel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().label()));
        colCommand.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().command()));
        colCycles.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cyclesText()));

        startRefresher();
    }

    public void startRefresher() {
        Type listType = new TypeToken<List<InstructionDTO>>(){}.getType();
        refresher = new GenericRefresher<>(autoUpdate,
                "/degreeUpdated", "/programData",
                instructionsTable, listType, "instructions");

        timer = new Timer(true);
        timer.schedule(refresher, 0, 2000);
    }
}

