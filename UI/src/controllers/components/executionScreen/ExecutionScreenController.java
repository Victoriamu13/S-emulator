package controllers.components.executionScreen;

import com.google.gson.JsonElement;
import controllers.utils.server.ServerRequestUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class ExecutionScreenController {
    private static final double DIVIDER_POSITION = 0.5;
    @FXML private SplitPane mainSplitPane;

    @FXML
    private void initialize(){
        // checkAndInitExecution(); // Detect ReRun vs Normal
        lockDivider(mainSplitPane);
    }

    private void checkAndInitExecution() {
        new Thread(() -> {
            JsonElement resp = ServerRequestUtils.sendGet("/isReRun");
            boolean isReRun = resp != null && resp.isJsonObject() && resp.getAsJsonObject().get("reRun").getAsBoolean();

            Platform.runLater(() -> {
                if (isReRun) {
                    System.out.println("[ExecutionScreen] Detected ReRun mode → skipping initExecution.");
                } else {
                    System.out.println("[ExecutionScreen] Regular mode → resetting execution state.");
                    new Thread(() -> {
                       ServerRequestUtils.sendPost("/resetExecutionState", RequestBody.create(new byte[0]));
                    }).start();
                }
            });
        }).start();
    }


    private void lockDivider(SplitPane splitPane) {
        SplitPane.Divider divider = splitPane.getDividers().get(0);
        divider.setPosition(ExecutionScreenController.DIVIDER_POSITION);

        divider.positionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() != ExecutionScreenController.DIVIDER_POSITION) {
                divider.setPosition(ExecutionScreenController.DIVIDER_POSITION);
            }
        });
    }

}
