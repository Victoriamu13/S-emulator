package uiDisplay.components;

import engineHolder.EngineHolder;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import logic.engineFacade.model.RunRecord;
import uiDisplay.components.execution.ExecutionController;

public class RunHistoryController {

    @FXML
    private TableView<RunRecord> runHistoryTable;
    @FXML private TableColumn<RunRecord, Number> colRunNo;
    @FXML private TableColumn<RunRecord, Number> colDegree;
    @FXML private TableColumn<RunRecord, String>  colYValue;
    @FXML private TableColumn<RunRecord, Number> colCycles;
    @FXML private Button btnReRun;
    @FXML private Button btnShow;

    private EngineHolder holder;
    private ProgramControlsController programControls;
    private ExecutionController executionController;

    // === setup helpers ===

    public void setEngineHolder(EngineHolder holder) { this.holder = holder; }
    public void setProgramControls(ProgramControlsController pc) { this.programControls = pc; }
    public void setExecutionController(ExecutionController ec) { this.executionController = ec; }

    @FXML
    private void initialize(){
        setupRunHistoryTable();
        setupButtons();
    }

    // === Initialize helper funcs ===

    private void setupRunHistoryTable() {
        colRunNo.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().runNo()));
        colDegree.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().degree()));
        colYValue.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().yValue())));
        colCycles.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().cycles()));

        runHistoryTable.setPlaceholder(new Label("No runs yet"));
    }

    private void setupButtons() {
        btnReRun.setDisable(true);
        btnShow.setDisable(true);

        runHistoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, sel) -> {
            boolean hasSelection = (sel != null);
            btnReRun.setDisable(!hasSelection);
            btnShow.setDisable(!hasSelection);
        });

        btnReRun.setOnAction(e -> onReRun());
        btnShow.setOnAction(e -> onShowStatus());
    }

    public void refreshHistory() {
        if (holder == null) return;
        var hist = holder.history();
        runHistoryTable.getItems().setAll(hist.records());
        runHistoryTable.refresh();
    }

    public void bindSelectedProgramName(StringProperty programNameProp) {
        programNameProp.addListener((obs, oldVal, newVal) -> {
            clear();
            if (newVal != null && holder != null && holder.hasEngine()) {
                runHistoryTable.getItems().setAll(holder.history().records());
                runHistoryTable.refresh();
            }
        });
    }

    // === Actions ===
    private void onReRun() {
        RunRecord selected = runHistoryTable.getSelectionModel().getSelectedItem();
        if (selected == null || holder == null) return;

        programControls.currentDegreeProperty().set(selected.degree());

        executionController.clearExecutionResults();
        executionController.loadInputVars();

        executionController.prefillInputs(selected.inputs());
    }


    private void onShowStatus() {
        RunRecord selected = runHistoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showRunStatus(selected);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a run from the history first.");
            alert.showAndWait();
        }
    }


    private void showRunStatus(RunRecord record) {
        StringBuilder sb = new StringBuilder("Final variables for run #" + record.runNo() + ":\n\n");
        record.finalVars().forEach((name, val) ->
                sb.append(name).append(" = ").append(val).append("\n")
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Run Status");
        alert.setHeaderText("Variables for run #" + record.runNo());
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    public void clear() {
        runHistoryTable.getItems().clear();
        runHistoryTable.setPlaceholder(new Label("No runs yet"));
    }
}
