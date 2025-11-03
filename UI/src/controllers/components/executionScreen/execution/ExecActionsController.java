package controllers.components.executionScreen.execution;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.components.executionScreen.execution.execActionsUtils.NewRunUtils;
import controllers.utils.server.ServerRequestUtils;
import controllers.utils.server.ServerResponseHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import logic.domain.architecture.ArchitectureGen;
import logic.system.api.SelectedClientState;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import static controllers.components.executionScreen.execution.execActionsUtils.NewRunUtils.*;

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
        new Thread(() -> {
            System.out.println("[CLIENT] === New Run sequence started ===");

            // Check architecture compatibility
            String archError = checkArchitectureCompatibility();
            if (archError != null) {
                System.out.println("[CLIENT] ❌ Architecture error: " + archError);

                Platform.runLater(() -> {
                    ServerResponseHandler.showAlert("Architecture Error", archError, Alert.AlertType.WARNING);
                });
                return;
            }
            // Receive chosen architecture from user
            String arch = NewRunUtils.getSelectedArchitecture();
            System.out.println("[CLIENT] Selected architecture = " + arch);

            if (arch == null) {
                System.out.println("[CLIENT] ❌ No architecture selected");

                Platform.runLater(() ->
                        ServerResponseHandler.showAlert("Error", "No architecture selected.", Alert.AlertType.ERROR));
                return;
            }

            // Check average run cost + chosen architecture cost
            boolean canRun = NewRunUtils.checkAvgRunCostBeforeRun();
            System.out.println("[CLIENT] Check avg run cost: " + canRun);

            if (!canRun) return;

            //Check if user has enough credits for payment
            int cost = ArchitectureGen.valueOf(arch).getBaseCost();
            boolean enough = NewRunUtils.hasEnoughCredits(arch);
            System.out.println("[CLIENT] Has enough credits: " + enough);

            if (!enough) return;

            //Ask for permission to charge
            final boolean[] confirmed = {false};
            final Object lock = new Object();
            Platform.runLater(() -> {
                confirmed[0] = NewRunUtils.showPaymentWindow(arch, cost);
                synchronized (lock) { lock.notify(); }
            });
            synchronized (lock) {
                try { lock.wait(); } catch (InterruptedException ignored) {}
            }
            System.out.println("[CLIENT] User confirmation: " + confirmed[0]);

            if (!confirmed[0]) return;

            // Charge for architecture
            boolean paid = NewRunUtils.chargeArchitecture(arch);
            System.out.println("[CLIENT] Payment status: " + paid);

            if (paid) {
                System.out.println("[CLIENT] Payment confirmed — calling /newRun");
                NewRunUtils.startRunAfterPayment();
            } else {
                System.out.println("[CLIENT] ❌ Payment failed — not starting new run");
            }
        }).start();
    }


    private void handleRunNormal() {
        new Thread(() -> {
            JsonElement resp = ServerRequestUtils.sendPost("/runProgram", RequestBody.create(null, new byte[0]));
            if (resp == null || !resp.isJsonObject()) return;

            JsonObject obj = resp.getAsJsonObject();
            String state = obj.has("state") ? obj.get("state").getAsString() : "";
            String msg = obj.has("message") ? obj.get("message").getAsString() : "";

            if ("SUCCESS".equalsIgnoreCase(state) && "OUT_OF_CREDITS".equalsIgnoreCase(msg)) {
                showOutOfCreditsAlert();
            }
            else if ("ERROR".equalsIgnoreCase(state)) {
                Platform.runLater(() ->
                        ServerResponseHandler.showAlert("Error", msg, Alert.AlertType.ERROR)
                );
            }
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
            if (response == null || !response.isJsonObject()) return;

            JsonObject obj = response.getAsJsonObject();
            String state = obj.has("state") ? obj.get("state").getAsString() : "";
            String msg = obj.has("message") ? obj.get("message").getAsString() : "";

            if ("ERROR".equalsIgnoreCase(state)) {
                if ("OUT_OF_CREDITS".equalsIgnoreCase(msg)) {
                    showOutOfCreditsAlert();
                    return;
                } else {
                    checkDebugError(response);
                }
            }
        }).start();
    }

    private void handleStepOver() {
        new Thread(() -> {
            var body = new FormBody.Builder().build();
            var response = ServerRequestUtils.sendPost("/stepOver", body);
            if (response == null || !response.isJsonObject()) return;

            JsonObject obj = response.getAsJsonObject();
            String state = obj.has("state") ? obj.get("state").getAsString() : "";
            String msg = obj.has("message") ? obj.get("message").getAsString() : "";

            if ("ERROR".equalsIgnoreCase(state)) {
                if ("OUT_OF_CREDITS".equalsIgnoreCase(msg)) {
                    showOutOfCreditsAlert();
                    return;
                }else{
                    checkDebugError(response);
                }
            }
        }).start();
    }
}



