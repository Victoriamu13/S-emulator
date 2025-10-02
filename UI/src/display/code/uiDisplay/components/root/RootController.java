package uiDisplay.components.root;

import engineHolder.EngineHolder;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import uiDisplay.components.executionwDebug.ExecutionwDebuggerController;
import uiDisplay.components.expansion.HistoryChainController;
import uiDisplay.components.instructions.InstructionsController;
import uiDisplay.components.load.LoaderController;
import uiDisplay.components.programFeatures.ProgBottomControlsController;
import uiDisplay.components.programFeatures.ProgUpperControlsController;
import uiDisplay.components.runHistory.RunHistoryController;

import static engineHolder.EngineHolder.hasEngine;

public class RootController {

    @FXML
    private SplitPane mainSplitPane;
    private static final double DIVIDER_POSITION = 0.5;
    @FXML private BorderPane mainContent;
    @FXML private LoaderController loaderController;
    @FXML private InstructionsController instructionsController;
    @FXML private HistoryChainController historyChainController;
    @FXML private ProgUpperControlsController progUpperControlsController;
    @FXML private ExecutionwDebuggerController executionwDebuggerController;
    @FXML private RunHistoryController runHistoryController;
    @FXML private ProgBottomControlsController progBottomControlsController;


    private final EngineHolder holder = new EngineHolder();
    private final IntegerProperty currentPc = new SimpleIntegerProperty(-1);
    public IntegerProperty currentPcProperty() {return currentPc;}

    @FXML
    private void initialize() {
        lockDivider(mainSplitPane);
        loaderController.setEngineHolder(holder);
        loaderController.engineProperty().addListener((obs, oldVal, newEngine) -> {
            // listener: when a new Engine is loaded -> initialize all UI controllers
           if (newEngine != null) {
                onEngineReady();
            }else {
                reset();
                progUpperControlsController.clear();
            }
        });

        loaderController.loadStateProperty().addListener((obs, oldVal, newVal) -> {
            // listener: update loading bar UI based on state
            switch (newVal) {
                case LOADING -> {
                    progBottomControlsController.resetLoading();
                    progBottomControlsController.startLoadingSimulation();
                }
                case SUCCESS -> {
                    progBottomControlsController.setOnFinished(progBottomControlsController::finishLoading);
                }
                case ERROR,IDLE -> {
                    progBottomControlsController.resetLoading();
                }
            }
        });
    }

    private void lockDivider(SplitPane splitPane) {
        SplitPane.Divider divider = splitPane.getDividers().get(0);
        divider.setPosition(RootController.DIVIDER_POSITION);

        divider.positionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() != RootController.DIVIDER_POSITION) {
                divider.setPosition(RootController.DIVIDER_POSITION);
            }
        });
    }


    private void reset() {
        instructionsController.clear();
        historyChainController.clear();
        runHistoryController.clear();
        executionwDebuggerController.clearExecutionResults();

        if (hasEngine(holder)) {
            holder.getEngine().clearAllBreakpoints();
        }
    }

    private void onEngineReady(){
        reset();

        progUpperControlsController.setEngineHolder(holder);
        instructionsController.setEngineHolder(holder);
        historyChainController.setEngineHolder(holder);
        executionwDebuggerController.setEngineHolder(holder);
        runHistoryController.setEngineHolder(holder);


        instructionsController.bindSelectedProgramName(progUpperControlsController.selectedProgramNameProperty());
        historyChainController.bindSelectedProgramName(progUpperControlsController.selectedProgramNameProperty());
        executionwDebuggerController.bindSelectedProgramName(progUpperControlsController.selectedProgramNameProperty());
        runHistoryController.bindSelectedProgramName(progUpperControlsController.selectedProgramNameProperty());

        instructionsController.bindDegree(progUpperControlsController.currentDegreeProperty());
        executionwDebuggerController.setDegreeSupplier(() -> progUpperControlsController.getCurrentDegree());
        instructionsController.bindHighlight(progUpperControlsController.highlightSelectionProperty());
        historyChainController.bindHighlight(progUpperControlsController.highlightSelectionProperty());
        historyChainController.attachHistoryChain(
                instructionsController,
                progUpperControlsController.currentDegreeProperty(),
                progUpperControlsController
        );

        runHistoryController.setOnSetDegree(deg -> progUpperControlsController.currentDegreeProperty().set(deg));
        runHistoryController.setOnClearExecution(() -> executionwDebuggerController.clearAndReloadInputs());
        runHistoryController.setOnPrefillInputs(inputs -> executionwDebuggerController.prefillInputs(inputs));
        runHistoryController.setOnTriggerRun(() -> executionwDebuggerController.triggerRun());
        executionwDebuggerController.setOnHistoryChanged(() -> runHistoryController.refreshHistory());
        executionwDebuggerController.bindCurrentPc(currentPcProperty());
        instructionsController.bindCurrentPc(currentPcProperty());


        progUpperControlsController.refreshHighlightList(0);
        instructionsController.setDegree(progUpperControlsController.getCurrentDegree());
        executionwDebuggerController.clearExecutionResults();
    }


}
