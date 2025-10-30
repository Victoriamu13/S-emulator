package controllers.components.executionScreen.execution;

import com.google.gson.JsonElement;
import controllers.utils.server.ServerRequestUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class ExecActionsController {
    @FXML private RadioButton normalMode;
    @FXML private RadioButton debugMode;
    @FXML private Button btnNewRun;
    @FXML private Button btnRunNormal;
    @FXML private Button btnRunDebug;
    @FXML private Button btnStop;
    @FXML private Button btnResume;
    @FXML private Button btnStepOver;
    @FXML private Button btnStepBack;

    @FXML
    public void initialize() {
        setupExecutionMode();
        setupButtons();
    }

    private void setupExecutionMode(){
        ToggleGroup modeGroup = new ToggleGroup();
        normalMode.setToggleGroup(modeGroup);
        debugMode.setToggleGroup(modeGroup);
        updateButtonsState("INIT");

        modeGroup.selectedToggleProperty().addListener((obs, old, now) -> {
            String mode = (now == normalMode) ? "NORMAL" : "DEBUG";
            sendModeChangeToServer(mode);
            updateButtonsState(mode);
        });

    }

    private void setupButtons(){
        btnNewRun.setOnAction(e -> handleNewRun());
        btnRunNormal.setOnAction(e -> handleRunNormal());
        btnRunDebug.setOnAction(e -> handleRunDebug());
        btnStop.setOnAction(e -> handleStop());
        btnResume.setOnAction(e -> handleResume());
        btnStepOver.setOnAction(e -> handleStepOver());
        btnStepBack.setOnAction(e -> handleStepBack());
    }

    private void updateButtonsState(String mode) {
        // Disable all buttons initially
        btnNewRun.setDisable(true);
        btnRunNormal.setDisable(true);
        btnRunDebug.setDisable(true);
        btnStop.setDisable(true);
        btnResume.setDisable(true);
        btnStepOver.setDisable(true);
        btnStepBack.setDisable(true);

        // Enable based on mode
        switch (mode) {
            case "NORMAL" -> {
                btnNewRun.setDisable(false);
                btnRunNormal.setDisable(false);
            }
            case "DEBUG" -> {
                btnNewRun.setDisable(false);
                btnRunDebug.setDisable(false);
                btnStop.setDisable(false);
                btnResume.setDisable(false);
                btnStepOver.setDisable(false);
                btnStepBack.setDisable(false);
            }
            default -> {}
        }
    }

    private void sendModeChangeToServer(String mode) {
        new Thread(() -> {
            RequestBody body = new FormBody.Builder()
                    .add("mode", mode)
                    .build();
            ServerRequestUtils.sendPost("/changeMode", body);
        }).start();
    }

    private void handleNewRun() {
    // Detect ReRun mode before resetting inputs
        new Thread(() -> {
            JsonElement resp = ServerRequestUtils.sendGet("/isReRun");
            boolean isReRun = resp != null && resp.getAsJsonObject().get("reRun").getAsBoolean();

            if (isReRun) {
                // In ReRun mode-> do not clear inputs
                ServerRequestUtils.sendGet("/startNewRun");
            } else {
                // In normal mode-> reset everything
                ServerRequestUtils.sendGet("/newRun");
            }
        }).start();
    }

    private void handleRunNormal() {
        new Thread(() -> {
            ServerRequestUtils.sendPost("/runProgram", RequestBody.create(null, new byte[0]));
            System.out.println("[Execution] >>> RUN button clicked, expecting server to use last sent inputs");

        } ).start();
    }

    private void handleRunDebug() {
        new Thread(() ->
                ServerRequestUtils.sendPost("/startDebug", RequestBody.create(null, new byte[0]))
        ).start();
    }


    private void handleStop() {
        new Thread(() ->
                ServerRequestUtils.sendPost("/stopDebug", RequestBody.create(null, new byte[0]))
        ).start();
    }

    private void handleResume() {
        new Thread(() ->
                ServerRequestUtils.sendPost("/resumeDebug", RequestBody.create(null, new byte[0]))
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
