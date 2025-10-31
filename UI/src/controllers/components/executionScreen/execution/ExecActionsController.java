package controllers.components.executionScreen.execution;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.util.Arrays;
import java.util.stream.Collectors;

import static controllers.utils.server.ServerResponseHandler.gson;

public class ExecActionsController {
    @FXML private RadioButton normalMode;
    @FXML private RadioButton debugMode;
    @FXML private Button btnNewRun;
    @FXML private Button btnRunNormal;
    @FXML private Button btnRunDebug;
    @FXML private Button btnStop;
    @FXML private Button btnResume;
    @FXML private Button btnStepOver;

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
    }

    private void updateButtonsState(String mode) {
        // Disable all buttons initially
        btnNewRun.setDisable(true);
        btnRunNormal.setDisable(true);
        btnRunDebug.setDisable(true);
        btnStop.setDisable(true);
        btnResume.setDisable(true);
        btnStepOver.setDisable(true);

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
            }
            default -> {}
        }
    }

    private void sendModeChangeToServer(String mode) {
        new Thread(() -> {
            RequestBody body = new FormBody.Builder()
                    .add("mode", mode)
                    .build();
            JsonElement resp = ServerRequestUtils.sendPost("/changeExecutionMode", body);
            System.out.println("[ExecActions] Mode changed to " + mode + ", response = " + resp);
        }).start();
    }

    private void handleNewRun() {
        System.out.println("[ExecActions] >>> handleNewRun() triggered");
    // Detect ReRun mode before resetting inputs
        new Thread(() -> {
            JsonElement resp = ServerRequestUtils.sendGet("/isReRun");
            boolean isReRun = resp != null && resp.getAsJsonObject().get("reRun").getAsBoolean();

            if (isReRun) {
                System.out.println("[ExecActions][DEBUG] isReRun = " + isReRun);
                // In Re-Run mode-> fetch Re-Run inputs
                System.out.println("[ExecActions] Calling /reRunData to fetch previous inputs...");
                JsonElement data = ServerRequestUtils.sendGet("/reRunData");
                System.out.println("[ExecActions][DEBUG] /reRunData raw response = " + data);
                if (data != null && data.isJsonObject()) {
                    JsonObject obj = data.getAsJsonObject();

                    if ("SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) {
                        int degree = obj.get("degree").getAsInt();
                        long[] inputs = new Gson().fromJson(obj.get("inputs"), long[].class);

                        String csv = Arrays.stream(inputs).mapToObj(String::valueOf).collect(Collectors.joining(","));
                        RequestBody body = new FormBody.Builder().add("inputs", csv).build();

                        // Save these inputs to the active EngineFacade on server
                        JsonElement setResp = ServerRequestUtils.sendPost("/setInputs", body);
                        System.out.println("[ExecActions] Sent inputs to server → response: " + setResp);
                        boolean success = setResp != null && setResp.isJsonObject() && "SUCCESS".equalsIgnoreCase(
                                setResp.getAsJsonObject().get("state").getAsString());

                        if(success) {
                            // Trigger newRun flag
                           ServerRequestUtils.sendGet("/newRun");
                            System.out.println("[ExecActions] ReRun inputs sent → triggered /newRun with existing inputs");
                        }else {
                            System.out.println("[ExecActions] Failed to set inputs before starting run!");
                        }
                    }
                }
            } else {
                // In normal mode-> behave as usual
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
}
