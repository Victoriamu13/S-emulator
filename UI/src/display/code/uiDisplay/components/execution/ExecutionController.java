package uiDisplay.components.execution;

import engineHolder.EngineHolder;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.ExecutionReport;
import uiDisplay.components.ProgramControlsController;

import java.util.ArrayList;
import java.util.List;


public class ExecutionController {

    @FXML private Button btnNewRun;
    @FXML private Button btnResume;
    @FXML private Button btnStop;
    @FXML private Button btnRun;
    @FXML private TableView<VarRow>varsTable;
    @FXML private TableColumn<VarRow, String> colVar;
    @FXML private TableColumn<VarRow, String>  colValue;
    @FXML private  ListView<VarRow> inputsList;
    @FXML private Label lblCycles;

    private EngineHolder holder;
    private ProgramControlsController programControls;

    // === Setters ===

    public void setEngineHolder(EngineHolder holder) {
        this.holder = holder;
    }

    public void setProgramControls(ProgramControlsController programControls) {
        this.programControls = programControls;
    }

    @FXML
    private void initialize() {
        setupVarsTable();
        setupInputsList();
        setupButtons();
        loadInputVars();
    }

    // === Initialize helper funcs ===
    private void setupVarsTable() {
        colVar.setCellValueFactory(c -> c.getValue().varNameProperty());
        colValue.setCellValueFactory(c -> c.getValue().varValueProperty());
    }

    private void setupInputsList() {
        inputsList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(VarRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item.getVarName());  // variable name (non-editable)
                    TextField tf = new TextField();
                    tf.textProperty().bindBidirectional(item.varValueProperty()); // user fills value
                    HBox row = new HBox(10, lbl, tf);
                    setGraphic(row);
                }
            }
        });
    }

    public void loadInputVars(){
        if (holder == null || !holder.hasEngine()) {
            inputsList.getItems().clear();
            return;
        }
        EngineFacade engine = holder.getEngine();
        int degree = (programControls != null) ? programControls.getCurrentDegree() : 0;

        List<String> inputs = engine.getInputsUsed(degree);
        inputsList.getItems().clear();
        for (String name : inputs) {
            inputsList.getItems().add(new VarRow(name, ""));
        }
    }

    private void setupButtons() {
        btnStop.setDisable(true);
        btnResume.setDisable(true);
        btnNewRun.setOnAction(e -> onNewRunClicked());
        btnRun.setOnAction(e -> onRunClicked());
    }

    private void onNewRunClicked() {
        clearExecutionResults();
        loadInputVars();
    }

    private void onRunClicked(){
        if (holder == null || !holder.hasEngine()) {
            showError("No program loaded.");
            return;
        }
        EngineFacade engine = holder.getEngine();
        int maxDegree = engine.getMaxExpansionDegree();
        int degree = (programControls != null) ? programControls.getCurrentDegree() : 0;

        List<String> rawValues = collectInputsFromUI();
        long[] inputs;
        try {
            inputs = engine.prepareInputsFields(degree, rawValues);
        } catch (IllegalArgumentException e) {
            showError("Invalid input:\n" + e.getMessage());
            return;
        }

        ExecutionReport report = engine.runWithReport(degree, inputs);
        updateVarsTable(report);
        lblCycles.setText("Cycles: " + report.totalCycles());

        holder.history().add(degree, inputs, report.yValue(), report.totalCycles());
    }

    private List<String> collectInputsFromUI() {
        List<String> values = new ArrayList<>();
        for (VarRow row : inputsList.getItems()) {
            values.add(row.getVarValue());
        }
        return values;
    }

    private void updateVarsTable(ExecutionReport report) {
        varsTable.getItems().clear();
        report.finalVars().forEach((name, val) ->
                varsTable.getItems().add(new VarRow(name, String.valueOf(val))));
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void clearExecutionResults() {
        varsTable.getItems().clear();
        lblCycles.setText("Cycles: 0");
        inputsList.getItems().clear();
    }

}
