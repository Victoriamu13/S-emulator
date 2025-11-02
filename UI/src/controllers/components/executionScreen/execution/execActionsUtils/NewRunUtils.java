package controllers.components.executionScreen.execution.execActionsUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;
import controllers.utils.server.ServerResponseHandler;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import logic.system.api.SelectedClientState;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import java.util.Arrays;
import java.util.stream.Collectors;

public class NewRunUtils {
    //Checks with the server if the program is fully supported by the selected architecture
    public static String checkArchitectureCompatibility() {
        JsonElement resp = ServerRequestUtils.sendGet("/checkArchitectureCompatibility");
        if (resp == null || !resp.isJsonObject()) return null;

        JsonObject obj = resp.getAsJsonObject();
        String state = obj.has("state") ? obj.get("state").getAsString() : "";

        if ("NO_ARCH".equalsIgnoreCase(state)) {
            return "Please select an architecture before starting a new run.";
        }

        if ("ERROR".equalsIgnoreCase(state)) {
            return obj.has("message")
                    ? obj.get("message").getAsString()
                    : "Some instructions are not supported for the selected architecture.";
        }
        return null;
    }

    //Fetch chosen architecture and her cost
    public static String getSelectedArchitecture() {
        JsonElement archResp = ServerRequestUtils.sendGet("/getSelectedArchitecture");
        if (archResp == null || !archResp.isJsonObject()) return null;
        JsonObject obj = archResp.getAsJsonObject();
        if (!"SUCCESS".equalsIgnoreCase(obj.get("state").getAsString()) || !obj.has("selected")) {
            return null;
        }
        return obj.get("selected").getAsString();
    }


    //Charge for chosen architecture
    public static boolean chargeArchitecture(String arch) {
        RequestBody body = new FormBody.Builder().add("architecture", arch).build();

        JsonElement resp = ServerRequestUtils.sendPost("/architectureCharge", body);
        if (resp == null || !resp.isJsonObject()) {
            Platform.runLater(() -> ServerResponseHandler.showAlert("Error", "No response from server.", Alert.AlertType.ERROR));
            return false;
        }

        JsonObject obj = resp.getAsJsonObject();
        String state = obj.has("state") ? obj.get("state").getAsString() : "";

        if ("ERROR".equalsIgnoreCase(state)) {
            String msg = obj.has("message") ? obj.get("message").getAsString() : "Not enough credits.";
            Platform.runLater(() -> ServerResponseHandler.showAlert("Payment Error", msg, Alert.AlertType.ERROR));
            return false;
        }

        if ("SUCCESS".equalsIgnoreCase(state)) {
            int newCredits = obj.has("credits") ? obj.get("credits").getAsInt() : 0;
            Platform.runLater(() -> ServerResponseHandler.showAlert(
                    "Payment Success",
                    "Architecture " + arch + " charged successfully.\nRemaining credits: " + newCredits,
                    Alert.AlertType.INFORMATION
            ));
            refreshCreditsFromServer();
            return true;
        }
        return false;
    }

    public static void refreshCreditsFromServer() {
        new Thread(() -> {
            JsonElement resp = ServerRequestUtils.sendGet("/currentUser");
            if (resp == null || !resp.isJsonObject()) return;

            JsonObject obj = resp.getAsJsonObject();
            if (!"SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) return;

            int newCredits = obj.has("credits") ? obj.get("credits").getAsInt() : 0;

            SelectedClientState.setCurrentCredits(newCredits);
        }).start();
    }

    public static boolean hasEnoughCredits(String architecture) {
        RequestBody body = new FormBody.Builder().add("architecture", architecture).build();

        JsonElement resp = ServerRequestUtils.sendPost("/checkCreditsForArchitecture", body);
        if (resp == null || !resp.isJsonObject()) {
            Platform.runLater(() ->
                    ServerResponseHandler.showAlert("Error", "No response from server.", Alert.AlertType.ERROR));
            return false;
        }

        JsonObject obj = resp.getAsJsonObject();
        String state = obj.has("state") ? obj.get("state").getAsString() : "";

        if ("ERROR".equalsIgnoreCase(state)) {
            String msg = obj.has("message")
                    ? obj.get("message").getAsString()
                    : "Not enough credits for this architecture.";
            Platform.runLater(() ->
                    ServerResponseHandler.showAlert("Insufficient Credits", msg, Alert.AlertType.ERROR));
            return false;
        }

        // There are enough credits to pay for architecture
        return "SUCCESS".equalsIgnoreCase(state);
    }

    public static boolean showPaymentWindow(String architecture, int cost) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Run");
            confirm.setHeaderText("Start New Run");
            confirm.setContentText("Run with architecture " + architecture +
                    " will cost " + cost + " credits.\nContinue?");
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            var result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }


    //Start new run after payment
    public static void startRunAfterPayment() {
        new Thread(() -> {
            JsonElement resp = ServerRequestUtils.sendGet("/isReRun");
            boolean isReRun = resp != null && resp.getAsJsonObject().get("reRun").getAsBoolean();

            if (isReRun) {
                boolean ok = handleReRunFlow();
                if (ok) ServerRequestUtils.sendGet("/newRun");
            } else {
                ServerRequestUtils.sendGet("/newRun");
            }
        }).start();
    }

     //Handles the Re-Run mode data
    public static boolean handleReRunFlow() {
        JsonElement data = ServerRequestUtils.sendGet("/reRunData");
        if (data == null || !data.isJsonObject()) return false;

        JsonObject obj = data.getAsJsonObject();
        if (!"SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) return false;

        long[] inputs = new Gson().fromJson(obj.get("inputs"), long[].class);
        String csv = Arrays.stream(inputs).mapToObj(String::valueOf).collect(Collectors.joining(","));
        RequestBody body = new FormBody.Builder().add("inputs", csv).build();

        JsonElement setResp = ServerRequestUtils.sendPost("/setInputs", body);
        return setResp != null && setResp.isJsonObject()
                && "SUCCESS".equalsIgnoreCase(setResp.getAsJsonObject().get("state").getAsString());
    }


    //Handles server debug error messages
    public static void checkDebugError(JsonElement response) {
        if (response != null && response.isJsonObject()) {
            var obj = response.getAsJsonObject();
            if (obj.has("state") && "ERROR".equalsIgnoreCase(obj.get("state").getAsString())) {
                String msg = obj.has("message") ? obj.get("message").getAsString() : "";
                Platform.runLater(() -> ServerResponseHandler.showAlert("Debug Error", msg, Alert.AlertType.ERROR));
            }
        }
    }

    public static void showOutOfCreditsAlert() {
        Platform.runLater(() -> {
            NewRunUtils.refreshCreditsFromServer();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Run Out Of Credits");
            alert.setHeaderText("There are not enough credits to run the program.");
            alert.setContentText("Add more credits before running again.");
            alert.showAndWait();
        });
    }



}
