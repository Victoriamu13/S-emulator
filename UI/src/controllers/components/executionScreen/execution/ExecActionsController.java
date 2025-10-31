package controllers.components.executionScreen.execution;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;
import controllers.utils.server.ServerResponseHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
            if (now == null) return;
            String mode = (now == normalMode) ? "NORMAL" : "DEBUG";
            sendModeChangeToServer(mode);
            updateButtonsState(mode);
        });
    }

     //------------------------- BUTTON SETUP -------------------------

    private void setupButtons() {
        btnNewRun.setOnAction(e -> handleNewRun());
        btnRunNormal.setOnAction(e -> handleRunNormal());
        btnRunDebug.setOnAction(e -> handleRunDebug());
        btnStop.setOnAction(e -> handleStop());
        btnResume.setOnAction(e -> handleResume());
        btnStepOver.setOnAction(e -> handleStepOver());
    }


     //------------------------- BUTTON STATES -------------------------

    private void updateButtonsState(String mode) {
        // Disable all buttons initially
        btnNewRun.setDisable(true);
        btnRunNormal.setDisable(true);
        btnRunDebug.setDisable(true);
        btnStop.setDisable(true);
        btnResume.setDisable(true);
        btnStepOver.setDisable(true);

        // Reset radio availability
        normalMode.setDisable(false);
        debugMode.setDisable(false);


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
            default -> {
            }
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



     //------------------------- EXECUTION ACTIONS -------------------------

    private void handleNewRun() {
        // Detect ReRun mode before resetting inputs
        new Thread(() -> {
            JsonElement resp = ServerRequestUtils.sendGet("/isReRun");
            boolean isReRun = resp != null && resp.getAsJsonObject().get("reRun").getAsBoolean();

            if (isReRun) {   // In Re-Run mode-> fetch Re-Run inputs
                JsonElement data = ServerRequestUtils.sendGet("/reRunData");
                if (data != null && data.isJsonObject()) {
                    JsonObject obj = data.getAsJsonObject();

                    if ("SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) {
                        long[] inputs = new Gson().fromJson(obj.get("inputs"), long[].class);

                        String csv = Arrays.stream(inputs).mapToObj(String::valueOf).collect(Collectors.joining(","));
                        RequestBody body = new FormBody.Builder().add("inputs", csv).build();

                        // Save these inputs to the active EngineFacade on server
                        JsonElement setResp = ServerRequestUtils.sendPost("/setInputs", body);
                        boolean success = setResp != null && setResp.isJsonObject() && "SUCCESS".equalsIgnoreCase(
                                setResp.getAsJsonObject().get("state").getAsString());

                        if (success) {
                            // Trigger newRun flag
                            ServerRequestUtils.sendGet("/newRun");
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
        }).start();
    }

    private void handleRunDebug() {
        new Thread(() ->
                ServerRequestUtils.sendPost("/startDebug", RequestBody.create(null, new byte[0]))
        ).start();
    }


    private void handleStop() {
        new Thread(() -> {
            var body = new FormBody.Builder().build();
            var response = ServerRequestUtils.sendPost("/stopDebug", body);
            checkDebugError(response);
        }).start();
    }

    private void handleResume() {
        new Thread(() -> {
            var body = new FormBody.Builder().build();
            var response = ServerRequestUtils.sendPost("/resumeDebug", body);
            checkDebugError(response);
        }).start();
    }

    private void handleStepOver() {
        new Thread(() -> {
            var body = new FormBody.Builder().build();
            var response = ServerRequestUtils.sendPost("/stepOver", body);
           checkDebugError(response);
        }).start();
    }



     //------------------------- DEBUG ERROR HANDLER -------------------------

    private void checkDebugError(JsonElement response) {
        if (response != null && response.isJsonObject()) {
            var obj = response.getAsJsonObject();
            if (obj.has("state") && "ERROR".equalsIgnoreCase(obj.get("state").getAsString())) {
                String msg = obj.has("message") ? obj.get("message").getAsString() : "";
                Platform.runLater(() -> ServerResponseHandler.showAlert("Debug Error", msg, Alert.AlertType.ERROR));
            }
        }
    }
}



