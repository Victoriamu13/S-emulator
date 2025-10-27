package controllers.components.executionScreen.execution;

import controllers.utils.server.ServerRequestUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import okhttp3.RequestBody;

public class ExecActionsController {
    @FXML private RadioButton normalMode;
    @FXML private RadioButton debugMode;
    @FXML private Button btnNewRun;
    @FXML private Button btnRun;
    @FXML private Button btnStop;
    @FXML private Button btnResume;
    @FXML private Button btnStepOver;
    @FXML private Button btnStepBack;

    @FXML
    public void initialize() {
        ToggleGroup modeGroup = new ToggleGroup();
        normalMode.setToggleGroup(modeGroup);
        debugMode.setToggleGroup(modeGroup);
        updateButtonsState("INIT");

        modeGroup.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == normalMode) updateButtonsState("NORMAL");
            else if (now == debugMode) updateButtonsState("DEBUG");
        });

        btnNewRun.setOnAction(e -> handleNewRun());
        btnRun.setOnAction(e -> handleRun());
        btnStop.setOnAction(e -> handleStop());
        btnResume.setOnAction(e -> handleResume());
        btnStepOver.setOnAction(e -> handleStepOver());
        btnStepBack.setOnAction(e -> handleStepBack());
    }

    private void updateButtonsState(String mode) {
        btnNewRun.setDisable(true);
        btnRun.setDisable(true);
        btnStop.setDisable(true);
        btnResume.setDisable(true);
        btnStepOver.setDisable(true);
        btnStepBack.setDisable(true);

        switch (mode) {
            case "NORMAL" -> {
                btnNewRun.setDisable(false);
                btnRun.setDisable(false);
            }
            case "DEBUG" -> {
                btnNewRun.setDisable(false);
                btnRun.setDisable(false);
                btnStop.setDisable(false);
                btnResume.setDisable(false);
                btnStepOver.setDisable(false);
                btnStepBack.setDisable(false);
            }
            default -> {}
        }
    }

    private void handleNewRun() {
        new Thread(() -> ServerRequestUtils.sendGet("/newRun")).start();
    }

    private void handleRun() {
        new Thread(() ->
                ServerRequestUtils.sendPost("/executeProgram", RequestBody.create(null, new byte[0]))
        ).start();
    }

    private void handleStop() {
        new Thread(() ->
                ServerRequestUtils.sendPost("/stopExecution", RequestBody.create(null, new byte[0]))
        ).start();
    }

    private void handleResume() {
        new Thread(() ->
                ServerRequestUtils.sendPost("/resumeExecution", RequestBody.create(null, new byte[0]))
        ).start();
    }

    private void handleStepOver() {
        new Thread(() ->
                ServerRequestUtils.sendPost("/stepOver", RequestBody.create(null, new byte[0]))
        ).start();
    }

    private void handleStepBack() {
        new Thread(() ->
                ServerRequestUtils.sendPost("/stepBack", RequestBody.create(null, new byte[0]))
        ).start();
    }
}
