package controllers.components.executionwDebug;

import engineHolder.EngineHolder;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.ExecutionReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static engineHolder.EngineHolder.hasEngine;


public class ExecutionwDebuggerController {

    @FXML private RadioButton rbNormal;     // Normal run mode
    @FXML private RadioButton rbDebug;      // Debug run mode
    @FXML private Button btnNewRun;         // Start new run
    @FXML private Button btnResume;         // Resume execution (debug mode)
    @FXML private Button btnStop;           // Stop execution (debug mode)
    @FXML private Button btnRun;            // Run with current inputs
    @FXML private Button btnStepOver;       // Step over (debug mode)
    @FXML private Button btnStepBack;       // Step back (debug mode)
    @FXML private TableView<VarRow>varsTable;             // Table of variables and their values
    @FXML private TableColumn<VarRow, String> colVar;     // Variable name column
    @FXML private TableColumn<VarRow, String>  colValue;  // Variable value column
    @FXML private  ListView<VarRow> inputsList;           // List of input variables
    @FXML private Label lblCycles;                        // Label showing total cycles

    private ToggleGroup runModeGroup;
    private boolean debugMode = false;                           // Flag for current run mode
    private EngineHolder holder;                                 // Holds EngineFacade instance
    private IntegerProperty currentPc;                           // Program counter binding
    private Supplier<Integer> degreeSupplier = () -> 0;          // Provides current expansion degree
    private Set<String> lastChangedVars = Set.of();             // Variables changed in last step
    private Runnable onHistoryChanged;                           // Callback when history updates
    private long[] lastInputsUsed = new long[0];                 // Last run inputs

    // === setup helpers ===
    public void setEngineHolder(EngineHolder holder) {
        this.holder = holder;
    }
    public void setDegreeSupplier(Supplier<Integer> supplier) {this.degreeSupplier = (supplier != null) ? supplier : () -> 0;}
    public void setOnHistoryChanged(Runnable r) { this.onHistoryChanged = r; }


    @FXML
    private void initialize() {
        setupVarsTable();
        setupInputsList();
        setupButtons();
        setupRunModeToggle();
        loadInputVars();
    }

    // === Initialize helper funcs ===
    private void setupVarsTable() {
        // Map table columns to VarRow properties
        colVar.setCellValueFactory(c -> c.getValue().varNameProperty());
        colValue.setCellValueFactory(c -> c.getValue().varValueProperty());

        // Custom cell renderer: highlights changed variables
        colVar.setCellFactory(col -> createChangedAwareCell());
        colValue.setCellFactory(col -> createChangedAwareCell());
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

    private TableCell<VarRow, String> createChangedAwareCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    getStyleClass().remove("changed-var");
                } else {
                    setText(value);
                    VarRow row = getTableView().getItems().get(getIndex());
                    if (row != null && lastChangedVars.contains(row.getVarName())) {
                        if (!getStyleClass().contains("changed-var")) {
                            getStyleClass().add("changed-var");
                        }
                    } else {
                        getStyleClass().remove("changed-var");
                    }
                }
            }
        };
    }

    public void loadInputVars(){
        if (!hasEngine(holder)) {
            inputsList.getItems().clear();
            return;
        }
        EngineFacade engine = holder.getEngine();
        int degree = degreeSupplier.get();

        List<String> inputs = engine.getInputsUsed(degree);
        inputsList.getItems().clear();
        for (String name : inputs) {
            inputsList.getItems().add(new VarRow(name, "0"));
        }
    }

    private void setupButtons() {
        btnStop.setOnAction(e->onStopClicked());
        btnResume.setOnAction(e->onResumeClicked());
        btnStepOver.setOnAction(e->onStepOverClicked());
        btnStepBack.setOnAction(e -> onStepBackClicked());
        btnNewRun.setOnAction(e -> onNewRunClicked());
        btnRun.setOnAction(e -> onRunClicked());
    }


    private void setupRunModeToggle() {
        runModeGroup = new ToggleGroup();
        rbNormal.setToggleGroup(runModeGroup);
        rbDebug.setToggleGroup(runModeGroup);

        //Default to Normal mode
        rbNormal.setSelected(true);
        debugMode = false;

        runModeGroup.selectedToggleProperty().addListener((obs, old, nw) -> {
            // listener: switch between Normal and Debug mode
            if (nw == rbDebug) {
                debugMode = true;
                disableDebugButtons(true);
            } else {
                debugMode = false;
                disableDebugButtons(true);
            }
            clearExecutionResults();
            inputsList.getItems().clear();
        });
    }

    // === Bindings ===
    public void bindSelectedProgramName(StringProperty programNameProp) {
        programNameProp.addListener((obs, oldVal, newVal) -> {
            clearExecutionResults();
            if (newVal != null && holder.hasEngine()) {
                holder.getEngine().selectProgramOrFunction(newVal);
                loadInputVars();
            }
        });
    }

    public void bindCurrentPc(IntegerProperty pcProperty) {
        this.currentPc = pcProperty;
    }

    // === Actions ===
    private void onNewRunClicked() {
        clearExecutionResults();
        loadInputVars();
        if (debugMode) {
            disableDebugButtons(false);
        }
    }

    public void prefillInputs(long[] inputs) { //Save user inputs
        for (int i = 0; i < inputsList.getItems().size(); i++) {
            String val = (i < inputs.length) ? String.valueOf(inputs[i]) : "";
            inputsList.getItems().get(i).setVarValue(val);
        }
    }

    public void clearAndReloadInputs() {
        clearExecutionResults();
        loadInputVars();
    }

    @FXML
    private void onRunClicked() {
        if (!hasEngine(holder)) {
            showError("No program loaded.");
            return;
        }
        EngineFacade engine = holder.getEngine();
        int degree = degreeSupplier.get();

        List<String> rawValues = collectInputsFromUI();
        long[] inputs;
        try {
            inputs = engine.prepareInputsFields(degree, rawValues);
        } catch (IllegalArgumentException e) {
            showError("Invalid input:\n" + e.getMessage());
            return;
        }

        if (debugMode) {
            if (engine.startDebugSession(degree, inputs)) {
                clearExecutionResults();
                lastInputsUsed = inputs;
                if (currentPc != null)currentPc.set(engine.getCurrentPc());

                ExecutionReport initReport = engine.buildInitialReport();
                if (initReport != null) {
                    updateVarsTable(initReport);
                    lblCycles.setText("Cycles: " + initReport.totalCycles());
                }
                disableDebugButtons(false);
            }
        } else { // NORMAL mode
            Set<Integer> bps = holder.getEngine().getBreakpoints();
            if (bps != null && !bps.isEmpty()) {

                if (engine.startDebugSession(degree, inputs)) {
                    lastInputsUsed = inputs;
                    ExecutionReport report = engine.resume();

                    if (report != null) {
                        rbDebug.setSelected(true);
                        debugMode = true;
                        disableDebugButtons(false);

                        updateVarsTable(report);
                        lblCycles.setText("Cycles: " + report.totalCycles());
                        if (currentPc != null) {
                            currentPc.set(engine.getCurrentPc());
                        }
                    }
                }
            } else {
                ExecutionReport report = engine.runWithReport(degree, inputs);

                if (report != null) {
                    updateVarsTable(report);
                    lblCycles.setText("Cycles: " + report.totalCycles());
                    saveToHistory(report);
                    if (onHistoryChanged != null) onHistoryChanged.run();
                }
            }
        }
    }


    @FXML
    private void onStepOverClicked() {
        ExecutionReport report = holder.getEngine().stepOver();
        if (report != null) {
            updateVarsTable(report);
            lblCycles.setText("Cycles: " + report.totalCycles());
            if (currentPc != null) {
                currentPc.set(holder.getEngine().getCurrentPc());
            }

            if (!holder.getEngine().isDebugActive()) {
                saveToHistory(report);
                if (currentPc != null) currentPc.set(-1);
                disableDebugButtons(true);
            }
        }
    }

    @FXML
    private void onStepBackClicked(){
        ExecutionReport report= holder.getEngine().stepBack();
        if(report!=null){
            updateVarsTable(report);
            lblCycles.setText("Cycles: " + report.totalCycles());
            if(currentPc!=null){
                currentPc.set(holder.getEngine().getCurrentPc());
            }
        }
    }


    @FXML
    private void onResumeClicked() {
        if (!hasEngine(holder)) {
            showError("No program loaded.");
            return;
        }
        EngineFacade engine = holder.getEngine();
        int degree = degreeSupplier.get();

        ExecutionReport report = engine.resume();
        if (report != null) {
            updateVarsTable(report);
            lblCycles.setText("Cycles: " + report.totalCycles());
            saveToHistory(report);
        }

        engine.stopDebugSession();
        if (currentPc != null) currentPc.set(-1);
        disableDebugButtons(true);
    }


    @FXML
    private void onStopClicked() {
        if (hasEngine(holder)) {
            EngineFacade engine = holder.getEngine();
            ExecutionReport report = engine.stopDebugSession();
            if (report != null) {
                updateVarsTable(report);
                lblCycles.setText("Cycles: " + report.totalCycles());
                saveToHistory(report);
            }
        }
        if (currentPc != null) currentPc.set(-1);
        disableDebugButtons(true);
    }


    private List<String> collectInputsFromUI() {
        List<String> values = new ArrayList<>();
        for (VarRow row : inputsList.getItems()) {
            values.add(row.getVarValue());
        }
        return values;
    }

    private void updateVarsTable(ExecutionReport report) {
        lastChangedVars = report.changedVars();
        List<VarRow> rows = report.finalVars().entrySet().stream()
                .map(e -> new VarRow(e.getKey(), String.valueOf(e.getValue())))
                .toList();

        varsTable.getItems().setAll(rows);
        varsTable.refresh();
    }


    private long[] getInputsForHistory(EngineFacade engine, int degree) {
        List<String> rawValues = collectInputsFromUI();
        try {
            return engine.prepareInputsFields(degree, rawValues);
        } catch (IllegalArgumentException e) {
            int required = engine.getInputsUsed(degree).size();
            return new long[required];
        }
    }

    private void saveToHistory(ExecutionReport report) {
        if (report == null || !hasEngine(holder)) return;
        EngineFacade engine = holder.getEngine();
        int degree = degreeSupplier.get();

        long[] inputs = getInputsForHistory(engine, degree);
        holder.history().add(degree, inputs, report.yValue(), report.totalCycles(), report.finalVars());
        if (onHistoryChanged != null) onHistoryChanged.run();
    }

    public void triggerRun() {
        onRunClicked();
    }

    private void disableDebugButtons(boolean enable) {
        btnStepOver.setDisable(enable);
        btnStepBack.setDisable(enable);
        btnStop.setDisable(enable);
        btnResume.setDisable(enable);
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
    }

}
