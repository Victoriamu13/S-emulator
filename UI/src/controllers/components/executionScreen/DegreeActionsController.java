package controllers.components.executionScreen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import controllers.utils.server.ServerRequestUtils;
import controllers.utils.server.ServerResponseHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class DegreeActionsController {
    @FXML private Button btnExpand;
    @FXML private Button btnCollapse;
    @FXML private Label lblDegree;
    @FXML private ComboBox<String> cmbHighlight;

    private final Timer timer = new Timer(true);
    private int currentDegree = 0;
    private int maxDegree = 0;
    private String lastHighlight = null;

    @FXML
    private void initialize() {
        loadDegreeFromServer();
        setupButtons();
        setupHighlightComboBox();
        startProgramVarsRefresher();
        startHighlightRefresher();

    }

    //  LOAD DEGREE DATA
    // Loads the current and max degree from the server
    private void loadDegreeFromServer() {
        new Thread(() -> {
            JsonElement response = ServerRequestUtils.sendGet("/programData");
            Platform.runLater(() -> {
                if (response != null && response.isJsonObject()) {
                    JsonObject obj = response.getAsJsonObject();

                    if ("SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) {
                        currentDegree = obj.get("currentDegree").getAsInt();
                        maxDegree = obj.get("maxDegree").getAsInt();
                        refreshDegree();
                        loadInitialVariables();
                    }
                } else {
                    ServerResponseHandler.showAlert("Error", "Failed to load degree info.", Alert.AlertType.ERROR);
                }
            });
        }).start();
    }

    //  BUTTONS (EXPAND/COLLAPSE)
    private void setupButtons() {
        btnExpand.setOnAction(e -> updateDegree(currentDegree + 1));
        btnCollapse.setOnAction(e -> updateDegree(currentDegree - 1));
    }

    // Sends new degree to the server (expand/collapse)
    private void updateDegree(int newDegree) {
        if (newDegree < 0 || newDegree > maxDegree) return;

        RequestBody body = new FormBody.Builder()
                .add("degree", String.valueOf(newDegree))
                .build();

        new Thread(() -> {
            JsonElement response = ServerRequestUtils.sendPost("/degreeAction", body);
            if (response == null || !response.isJsonObject()) {
                Platform.runLater(() -> ServerResponseHandler.showAlert(
                        "Error", "Server error updating degree.", Alert.AlertType.ERROR));
                return;
            }

            JsonObject obj = response.getAsJsonObject();
            if (!"SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) {
                Platform.runLater(() -> ServerResponseHandler.showAlert(
                        "Error", obj.get("message").getAsString(), Alert.AlertType.ERROR));
                return;
            }

            currentDegree = obj.get("degree").getAsInt();

            Platform.runLater(this::refreshDegree);
            clearHistoryChain();

            RequestBody varsBody = new FormBody.Builder()
                    .add("degree", String.valueOf(currentDegree))
                    .build();

            JsonElement varsResponse = ServerRequestUtils.sendPost("/programVariables", varsBody);
            if (varsResponse != null && varsResponse.isJsonObject()) {
                JsonObject varsObj = varsResponse.getAsJsonObject();
                if (varsObj.has("variables")) {
                    Type listType = new TypeToken<List<String>>() {
                    }.getType();
                    List<String> newVars = new Gson().fromJson(varsObj.get("variables"), listType);

                    Platform.runLater(() -> {
                        String selectedBefore = cmbHighlight.getSelectionModel().getSelectedItem();
                        cmbHighlight.getItems().setAll(newVars);

                        if (selectedBefore != null && newVars.contains(selectedBefore)) {
                            cmbHighlight.getSelectionModel().select(selectedBefore);
                        } else {
                            cmbHighlight.getSelectionModel().clearSelection();
                        }
                    });
                }
            }
            ServerRequestUtils.sendGet("/programVariablesUpdated");
            ServerRequestUtils.sendGet("/historyUpdated");
        }).start();
    }

    //  DEGREE LABEL
    // Updates the degree label text
    private void refreshDegree() {
        lblDegree.setText((currentDegree + " / " + maxDegree));
    }

    //  HIGHLIGHT COMBOBOX
    private void setupHighlightComboBox() {
        cmbHighlight.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.isBlank()) {
                    setText("Highlight");
                    setStyle("-fx-text-fill: -fx-text-inner-color; -fx-opacity: 0.6;");
                } else {
                    setText(item);
                    setStyle("");
                }
            }
        });
        cmbHighlight.setOnAction(e -> {
            String selected = cmbHighlight.getValue();
            if (selected == null || selected.equals(lastHighlight)) return;
            lastHighlight = selected;
            updateHighlight(selected);
        });
    }

    // Sends selected variable name to the server to mark highlight
    private void updateHighlight(String variable) {
        new Thread(() -> {
            RequestBody body = new FormBody.Builder()
                    .add("variable", variable == null ? "" : variable)
                    .build();
            ServerRequestUtils.sendPost("/highlightVariable", body);
        }).start();
    }


    //  REFRESHERS
    private void startProgramVarsRefresher() {
        Timer varsTimer = new Timer(true);

        varsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement resp = ServerRequestUtils.sendGet("/programVariablesUpdated");
                if (resp == null || !resp.isJsonObject()) return;

                boolean updated = resp.getAsJsonObject().get("updated").getAsBoolean();
                if (!updated) return;

                RequestBody body = new FormBody.Builder()
                        .add("degree", String.valueOf(currentDegree))
                        .build();

                JsonElement varsResponse = ServerRequestUtils.sendPost("/programVariables", body);
                if (varsResponse == null || !varsResponse.isJsonObject()) return;

                var obj = varsResponse.getAsJsonObject();
                if (!obj.has("variables")) return;

                Type listType = new TypeToken<List<String>>() {
                }.getType();
                List<String> newVars = new Gson().fromJson(obj.get("variables"), listType);

                Platform.runLater(() -> {
                    String selectedBefore = cmbHighlight.getSelectionModel().getSelectedItem();

                    List<String> currentItems = cmbHighlight.getItems();
                    if (currentItems.size() == newVars.size() &&
                            currentItems.containsAll(newVars) &&
                            newVars.containsAll(currentItems)) {
                        return;
                    }
                    cmbHighlight.getItems().setAll(newVars);

                    if (selectedBefore != null && newVars.contains(selectedBefore)) {
                        cmbHighlight.getSelectionModel().select(selectedBefore);
                    } else {
                        cmbHighlight.getSelectionModel().clearSelection();
                    }

                    if (cmbHighlight.getSelectionModel().isEmpty() || cmbHighlight.getValue() == null) {
                        cmbHighlight.getSelectionModel().clearSelection();
                        cmbHighlight.setValue(null);
                    }
                });
            }
        }, 0, 1000);
    }


    private void startHighlightRefresher() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement resp = ServerRequestUtils.sendGet("/highlightVariable");
                if (resp == null || !resp.isJsonObject()) return;

                JsonObject obj = resp.getAsJsonObject();
                if (!"SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) return;

                String serverHighlight = obj.get("highlight").isJsonNull()
                        ? null : obj.get("highlight").getAsString();

                if (Objects.equals(serverHighlight, lastHighlight)) return;

                lastHighlight = serverHighlight;
                Platform.runLater(() -> cmbHighlight.setValue(serverHighlight));
            }
        }, 0, 1000);
    }


    //  HISTORY CHAIN CLEAR
// Clears the history chain on the previous chosen instruction
    private void clearHistoryChain() {
        new Thread(() -> {
            RequestBody body = new FormBody.Builder()
                    .add("clear", "true")
                    .build();
            ServerRequestUtils.sendPost("/historyChain", body);
        }).start();
    }


    // LOAD INITIAL VARIABLES
    private void loadInitialVariables() {
        RequestBody body = new FormBody.Builder()
                .add("degree", String.valueOf(currentDegree))
                .build();
        new Thread(() -> {
            JsonElement response = ServerRequestUtils.sendPost("/programVariables", body);
            if (response == null || !response.isJsonObject()) return;
            var obj = response.getAsJsonObject();

            if (!obj.has("variables")) return;

            Type listType = new TypeToken<List<String>>() {
            }.getType();
            List<String> vars = new Gson().fromJson(obj.get("variables"), listType);
            Platform.runLater(() -> {
                cmbHighlight.getItems().setAll(vars);

                if (cmbHighlight.getSelectionModel().isEmpty() || cmbHighlight.getValue() == null) {
                    cmbHighlight.getSelectionModel().clearSelection();
                    cmbHighlight.setValue(null);
                }
            });
        }).start();
    }
}



