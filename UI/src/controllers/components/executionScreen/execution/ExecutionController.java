package controllers.components.executionScreen.execution;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import controllers.screens.ScreenManager;
import controllers.utils.client.SelectedClientState;
import controllers.utils.refreshers.GenericActionsRefresher;
import controllers.utils.refreshers.TimerManager;
import controllers.utils.server.ServerRequestUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import logic.engineFacade.model.ExecutionReport;
import logic.system.data.variables.VariableRow;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static controllers.screens.ScreenManager.EXECUTION;

public class ExecutionController {
    @FXML private TableView<VariableRow> variablesTable;
    @FXML private TableColumn<VariableRow, String> colVar;
    @FXML private TableColumn<VariableRow, String> colValue;
    @FXML private Label lblCycles;
    @FXML private ListView<InputRow> inputsList;
    @FXML private Button btnBackToDashboard;

    private final Timer timer = new Timer(true);
    private boolean newRunHandled = false;

    @FXML
    private void initialize() {
        System.out.println("===== [Execution] INITIALIZE screen =====");
        TimerManager.register(EXECUTION, timer);

        // Sync selection at screen load
        if (!SelectedClientState.syncFromServer()) {
            System.out.println("[Execution] Failed to sync current selection from server");
        }else {
            System.out.println("[Execution] Synced selection from server → " +
                    "program=" + SelectedClientState.getName());
        }

       startInitExecutionRefresher();
        setupTable();
        btnBackToDashboard.setOnAction(e -> {
            new Thread(() ->
                ServerRequestUtils.sendPost("/resetExecutionState", RequestBody.create(new byte[0]))).start();
            ScreenManager.showDashboardScreen();
        });

        // On init: clear view + load ReRun data if exists
        new Thread(() -> Platform.runLater(this::startNewRunWatcher)).start();
    }

    // ======== SETUP =======
    private void setupTable() {
        colVar.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().variable()));
        colValue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().value()));
    }


    private void startNewRunWatcher() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Ask the server whether a new run was triggered
                JsonElement resp = ServerRequestUtils.sendGet("/startNewRun");
                if (resp == null || !resp.isJsonObject()) return;

                boolean updated = resp.getAsJsonObject().get("updated").getAsBoolean();

                // A new run has just started
                if (updated && !newRunHandled) {
                    newRunHandled = true;

                    Platform.runLater(() -> {
                        refreshInputsFromServer();

                        // Check execution mode to determine refresher
                        JsonElement response = ServerRequestUtils.sendGet("/getExecutionMode");
                        if (response == null || !response.isJsonObject()) return;

                        String mode = response.getAsJsonObject().get("mode").getAsString();

                        if ("DEBUG".equalsIgnoreCase(mode)) {
                            System.out.println("[Execution] 🟣 Debug flag raised → start debugResultsRefresher()");
                            Platform.runLater(() -> startDebugResultsRefresher());

                        } else {
                            System.out.println("[Execution] 🟢 Normal flag raised → start resultsRefresher()");
                            Platform.runLater(() -> startResultsRefresher());
                        }
                    });

                    // Run flag has been cleared by the server
                } else if (!updated && newRunHandled) {
                    newRunHandled = false;
                }
            }
           }, 0, 1000);
        }


    // ======= REFRESHERS =======
//    private void startInputsRefresher() {
//        timer.schedule(new GenericActionsRefresher("/inputsUpdated", this::refreshInputsFromServer), 0, 1000);
//    }

    private void startResultsRefresher() {
        timer.schedule(new GenericActionsRefresher("/resultsUpdated", this::refreshResultsFromServer), 0, 1000);
    }

    private void startDebugResultsRefresher() {
        timer.schedule(new GenericActionsRefresher("/debugResultsUpdated", this::refreshDebugResultsFromServer), 0, 1000);
    }

    private void startInitExecutionRefresher() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement resp = ServerRequestUtils.sendGet("/initExecutionUpdated");
                if (resp != null && resp.getAsJsonObject().get("updated").getAsBoolean()) {
                    Platform.runLater(() -> {
                        inputsList.getItems().clear();
                        variablesTable.getItems().clear();
                        lblCycles.setText("Cycles: 0");
                    });
                }
            }
        }, 0, 1000);
    }


    // ======= DATA REFRESH =======
    private void refreshInputsFromServer() {
        new Thread(() -> {
            // Sync selection first
            if (!SelectedClientState.syncFromServer()) return;

            JsonElement response = ServerRequestUtils.sendGet("/inputs");
            if (response == null || !response.isJsonObject()) return;

            var obj = response.getAsJsonObject();
            var inputsEl  = obj.get("inputs");
            var valuesEl = obj.get("values");

            if (inputsEl  == null || !inputsEl .isJsonArray()) {
                Platform.runLater(() -> {
                    inputsList.getItems().clear();
                    lblCycles.setText("Cycles: 0");
                });
                return;
            }

            // Update UI
            var gson = new Gson();
            var listType = new TypeToken<List<String>>(){}.getType();
            List<String> names = gson.fromJson(inputsEl, listType);
            List<String> values = (valuesEl != null && valuesEl.isJsonArray()) ? gson.fromJson(valuesEl, listType) : null;

            Platform.runLater(() -> {
                inputsList.getItems().setAll(
                        IntStream.range(0, names.size())
                                .mapToObj(i -> {
                                    InputRow row = new InputRow(names.get(i));
                                    String val = (values != null && values.size() > i) ? values.get(i) : "0";
                                    row.setValue(val);
                                    return row;
                                })
                                .toList()
                );
                setupInputs();
                lblCycles.setText("Cycles: 0");
            });
        }).start();
    }

    private void setupInputs() {
        // Create editable text fields for input values
        inputsList.setCellFactory(listView -> new ListCell<>() {
            private Timer tempTimer;

            @Override
            protected void updateItem(InputRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                }
                else {
                    Label lbl = new Label(item.getName());
                    TextField text = new TextField();
                    text.textProperty().bindBidirectional(item.valueProperty());
                    text.setPrefWidth(80);

                    // Send inputs to server with small delay after typing
                    text.textProperty().addListener((obs, oldVal, newVal) -> {

                        if (tempTimer != null) tempTimer.cancel();
                        tempTimer = new Timer(true);
                        tempTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                sendInputsToServer();
                            }
                        }, 500);
                    });

                    HBox row = new HBox(10, lbl, text);
                    setGraphic(row);
                }
            }
        });
    }

    private void refreshResultsFromServer() {
        new Thread(() -> {
            if (!SelectedClientState.syncFromServer()) return;

            JsonElement response = ServerRequestUtils.sendGet("/results");
            if (response == null || !response.isJsonObject()) return;

            JsonObject obj = response.getAsJsonObject();
            JsonElement reportJson = obj.get("report");

            if (reportJson == null || reportJson.isJsonNull()) {
                Platform.runLater(() -> {
                    variablesTable.getItems().clear();
                    lblCycles.setText("Cycles: 0");
                });
                return;
            }

            ExecutionReport report = new Gson().fromJson(reportJson, ExecutionReport.class);

            Platform.runLater(() -> {
                lblCycles.setText("Cycles: " + report.totalCycles());
                variablesTable.getItems().setAll(
                        report.finalVars().entrySet().stream()
                                .map(e -> new VariableRow(e.getKey(), String.valueOf(e.getValue())))
                                .toList()
                );
            });
        }).start();
    }

    private void refreshDebugResultsFromServer() {
        new Thread(() -> {
            if (!SelectedClientState.syncFromServer()) return;

            JsonElement response = ServerRequestUtils.sendGet("/debugResults");
            if (response == null || !response.isJsonObject()) return;

            JsonObject obj = response.getAsJsonObject();
            JsonElement reportJson = obj.get("report");
            if (reportJson == null) return;

            ExecutionReport report = new Gson().fromJson(reportJson, ExecutionReport.class);

            Platform.runLater(() -> {
                lblCycles.setText("Cycles: " + report.totalCycles());
                variablesTable.getItems().setAll(
                        report.finalVars().entrySet().stream()
                                .map(e -> new VariableRow(e.getKey(), String.valueOf(e.getValue())))
                                .toList()
                );
            });
        }).start();
    }


    // ======= WITH SERVER ACTIONS =======
    private void sendInputsToServer() {
        String csv = inputsList.getItems().stream()
                .map(InputRow::getValue)
                .map(v -> v.isEmpty() ? "0" : v)
                .collect(Collectors.joining(","));

        RequestBody body = new FormBody.Builder().add("inputs", csv).build();
        new Thread(() -> ServerRequestUtils.sendPost("/setInputs", body)).start();
    }
}