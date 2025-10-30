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
        setupInputs();
        setupTable();
        btnBackToDashboard.setOnAction(e -> {
            new Thread(() -> {
                ServerRequestUtils.sendPost("/resetExecutionState", RequestBody.create(new byte[0]));
            }).start();

            ScreenManager.showDashboardScreen();
        });

        // On init: clear view + load ReRun data if exists
        new Thread(() -> {
            var resp = ServerRequestUtils.sendGet("/isReRun");
            boolean isReRun = resp != null && resp.getAsJsonObject().get("reRun").getAsBoolean();
            System.out.println("[Execution] isReRun=" + isReRun);

            Platform.runLater(() -> {
                variablesTable.getItems().clear();
                inputsList.getItems().clear();
                lblCycles.setText("Cycles: 0");

                if (isReRun){
                    System.out.println("[Execution] Loading ReRun data...");
                    loadReRunData();
                }
                System.out.println("[Execution] Starting new run watcher");

                startNewRunWatcher();

            });
        }).start();
    }

    // ======== SETUP =======
    private void setupTable() {
        colVar.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().variable()));
        colValue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().value()));
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


    private void startNewRunWatcher() {
        // Watches for new runs triggered by /startNewRun
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement resp = ServerRequestUtils.sendGet("/startNewRun");
                if (resp == null || !resp.isJsonObject()) {
                    System.out.println("[Execution] /startNewRun returned null or invalid");
                    return;
                }

                boolean updated = resp.getAsJsonObject().get("updated").getAsBoolean();
                if (updated) {
                    System.out.println("[Execution] startNewRun flag detected → refreshing inputs/results");
                    startInputsRefresher();
                    startResultsRefresher();
                    startDebugResultsRefresher();
                } else {
                    System.out.println("[Execution] No new run flag detected → skipping refresh setup");
                }
            }
        }, 0, 1000);
    }

    // ======= REFRESHERS =======
    private void startInputsRefresher() {
        timer.schedule(new GenericActionsRefresher("/inputsUpdated", this::refreshInputsFromServer), 0, 1000);
    }

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
                        System.out.println("[Execution] initExecution flag detected → clearing inputs and variables");

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
        System.out.println("[Execution] ===== refreshInputsFromServer() called =====");

        new Thread(() -> {
            // Sync selection first
            if (!SelectedClientState.syncFromServer()) {
                System.out.println("[Execution] Failed to sync /selected → aborting inputs refresh");
                return;
            }

            JsonElement response = ServerRequestUtils.sendGet("/inputs");
            if (response == null || !response.isJsonObject()) {
                System.out.println("[Execution] /inputs response is null or invalid");

                return;
            }

            var obj = response.getAsJsonObject();
            var inputsEl  = obj.get("inputs");
            var valuesEl = obj.get("values");

            if (inputsEl  == null || !inputsEl .isJsonArray()) {
                System.out.println("[Execution] No inputs received -> clearing table");

                Platform.runLater(() -> {
                    inputsList.getItems().clear();
                    lblCycles.setText("Cycles: 0");
                });
                return;
            }

            System.out.println("[Execution] Received " + inputsEl.getAsJsonArray().size() +
                    " inputs from server → updating UI");
            // Update UI
            var gson = new Gson();
            var listType = new TypeToken<List<String>>(){}.getType();
            List<String> names = gson.fromJson(inputsEl, listType);
            List<String> values = (valuesEl != null && valuesEl.isJsonArray())
                    ? gson.fromJson(valuesEl, listType)
                    : null;
            System.out.println("[Execution] Updating inputs on screen with values: " + values);
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
                variablesTable.getItems().clear();
                lblCycles.setText("Cycles: 0");
            });
        }).start();
    }

    private void refreshResultsFromServer() {
        new Thread(() -> {
            if (!SelectedClientState.syncFromServer()) {
                System.out.println("[Execution] /selected sync failed → skipping results refresh");
                return;
            }

            System.out.println("[Execution] Checking results for selection=" +
                    SelectedClientState.getName());

            JsonElement response = ServerRequestUtils.sendGet("/results");
            if (response == null || !response.isJsonObject()) {
                System.out.println("[Execution] /results response invalid");

                return;
            }

            JsonObject obj = response.getAsJsonObject();
            JsonElement reportJson = obj.get("report");

            if (reportJson == null || reportJson.isJsonNull()) {
                System.out.println("[Execution] No results yet -> clearing variables table");

                Platform.runLater(() -> {
                    variablesTable.getItems().clear();
                    lblCycles.setText("Cycles: 0");
                });
                return;
            }

            ExecutionReport report = new Gson().fromJson(reportJson, ExecutionReport.class);
            System.out.println("[Execution] Got results: " + report.finalVars().size() + " variables");

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
            System.out.println("[Execution] Refreshing results for selection=" + SelectedClientState.getName());

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
        System.out.println("[Execution] Sending inputs to server: " + csv); // ✅ NEW

        RequestBody body = new FormBody.Builder().add("inputs", csv).build();
        new Thread(() -> {
            JsonElement response = ServerRequestUtils.sendPost("/setInputs", body);
            System.out.println("[Execution] Server responded to setInputs: " + response); // ✅ NEW

        }).start();
    }


    // ======= RE-RUN MODE =======
    public void loadReRunData() {
        // Load ReRun data (degree + inputs array)
        new Thread(() -> {
            JsonElement response = ServerRequestUtils.sendGet("/reRunData");
            if (response == null || !response.isJsonObject()) return;

            JsonObject obj = response.getAsJsonObject();
            if (!"SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) return;

            int degree = obj.get("degree").getAsInt();
            var gson = new Gson();
            long[] inputs = gson.fromJson(obj.get("inputs"), long[].class);

            Platform.runLater(() -> {
                lblCycles.setText("Cycles: 0");
                inputsList.getItems().clear();

                for (int i = 0; i < inputs.length; i++) {
                    InputRow row = new InputRow("x" + (i + 1));
                    row.setValue(String.valueOf(inputs[i]));
                    inputsList.getItems().add(row);
                }
            });
        }).start();
    }

}