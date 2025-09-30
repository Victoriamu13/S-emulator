package uiDisplay.components.root;

import engineHolder.EngineHolder;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import uiDisplay.components.executionwDebug.ExecutionwDebuggerController;
import uiDisplay.components.expansion.HistoryChainController;
import uiDisplay.components.instructions.InstructionsController;
import uiDisplay.components.load.LoaderController;
import uiDisplay.components.load.LoadingBarController;
import uiDisplay.components.programFeatures.ProgramControlsController;
import uiDisplay.components.runHistory.RunHistoryController;

public class RootController {

    @FXML
    private SplitPane mainSplitPane;
    private static final double DIVIDER_POSITION = 0.5;
    @FXML private VBox overlay;
    @FXML private BorderPane mainContent;
    @FXML private LoaderController loaderController;
    @FXML private InstructionsController instructionsController;
    @FXML private HistoryChainController historyChainController;
    @FXML private ProgramControlsController programControlsController;
    @FXML private ExecutionwDebuggerController executionwDebuggerController;
    @FXML private RunHistoryController runHistoryController;
    @FXML private LoadingBarController loadingBarController;


    private final EngineHolder holder = new EngineHolder();
    private final IntegerProperty currentPc = new SimpleIntegerProperty(-1);

    public IntegerProperty currentPcProperty() {return currentPc;}

    @FXML
    private void initialize() {
        lockDivider(mainSplitPane);
        loaderController.setEngineHolder(holder);
        loaderController.engineProperty().addListener((obs, oldVal, newEngine) -> {
            if (newEngine != null) {
                onEngineReady();
            }else {
                reset();
                programControlsController.clear();
            }
        });

        loaderController.loadStateProperty().addListener((obs, oldVal, newVal) -> {
            switch (newVal) {
                case LOADING -> {
                    loadingBarController.resetLoading();
                    showOverlay();
                    loadingBarController.setOnFinished(() -> {hideOverlay();});
                    loadingBarController.startLoadingSimulation();
                }
                case SUCCESS -> {
                    loadingBarController.markSuccess();
                }
                case ERROR -> {
                    loadingBarController.resetLoading();
                    hideOverlay();
                }
                case IDLE -> {
                    loadingBarController.resetLoading();
                    hideOverlay();
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

    private void showOverlay() {
        overlay.setVisible(true);
        overlay.setManaged(true);
        mainContent.setDisable(true);
    }

    private void hideOverlay() {
        overlay.setVisible(false);
        overlay.setManaged(false);
        mainContent.setDisable(false);
    }

    private void reset() {
        instructionsController.clear();
        historyChainController.clear();
        runHistoryController.clear();
        executionwDebuggerController.clearExecutionResults();
}

    private void onEngineReady(){
        reset();

        programControlsController.setEngineHolder(holder);
        instructionsController.setEngineHolder(holder);
        historyChainController.setEngineHolder(holder);
        executionwDebuggerController.setEngineHolder(holder);
        runHistoryController.setEngineHolder(holder);


        instructionsController.bindSelectedProgramName(programControlsController.selectedProgramNameProperty());
        historyChainController.bindSelectedProgramName(programControlsController.selectedProgramNameProperty());
        executionwDebuggerController.bindSelectedProgramName(programControlsController.selectedProgramNameProperty());
        runHistoryController.bindSelectedProgramName(programControlsController.selectedProgramNameProperty());

        instructionsController.bindDegree(programControlsController.currentDegreeProperty());
        executionwDebuggerController.setDegreeSupplier(() -> programControlsController.getCurrentDegree());
        instructionsController.bindHighlight(programControlsController.highlightSelectionProperty());
        historyChainController.bindHighlight(programControlsController.highlightSelectionProperty());
        historyChainController.attachHistoryChain(
                instructionsController,
                programControlsController.currentDegreeProperty(),
                programControlsController
        );

        runHistoryController.setOnSetDegree(deg -> programControlsController.currentDegreeProperty().set(deg));
        runHistoryController.setOnClearExecution(() -> executionwDebuggerController.clearAndReloadInputs());
        runHistoryController.setOnPrefillInputs(inputs -> executionwDebuggerController.prefillInputs(inputs));
        runHistoryController.setOnTriggerRun(() -> executionwDebuggerController.triggerRun());
        executionwDebuggerController.setOnHistoryChanged(() -> runHistoryController.refreshHistory());
        executionwDebuggerController.bindCurrentPc(currentPcProperty());
        instructionsController.bindCurrentPc(currentPcProperty());


        programControlsController.refreshHighlightList(0);
        instructionsController.setDegree(programControlsController.getCurrentDegree());
        executionwDebuggerController.clearExecutionResults();
    }


}
