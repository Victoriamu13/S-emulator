package controllers.components.executionScreen.execution;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

public class ExecutionController {
    @FXML private TableView<VariableRow> variablesTable;
    @FXML private TableColumn<VariableRow, String> colVar;
    @FXML private TableColumn<VariableRow, String> colValue;
    @FXML private Label lblCycles;
    @FXML private ListView<InputRow> inputsList;

    private final Timer timer = new Timer(true);

    @FXML
    private void initialize() {
        setupInputsUI();
        setupTable();
        startInputsRefresher();
        startInitExecutionRefresher();
        startResultsRefresher();
        startDebugResultsRefresher();
    }

    // ======== SETUP =======
    private void setupTable() {
        colVar.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().variable()));
        colValue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().value()));
    }

    private void setupInputsUI() {
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

                    text.textProperty().addListener((obs, oldVal, newVal) -> {
                        if (tempTimer != null) {
                            tempTimer.cancel();
                        }
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

    // ======= REFRESHERS =======
    private void startInputsRefresher() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement flagResponse = ServerRequestUtils.sendGet("/inputsUpdated");
                if (flagResponse == null || !flagResponse.isJsonObject()) return;
                boolean updated = flagResponse.getAsJsonObject().get("updated").getAsBoolean();
                if (updated) refreshInputsFromServer();
            }
        }, 0, 1000);
    }

    private void startResultsRefresher() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement flagResponse = ServerRequestUtils.sendGet("/resultsUpdated");
                if (flagResponse == null || !flagResponse.isJsonObject()) return;
                boolean updated = flagResponse.getAsJsonObject().get("updated").getAsBoolean();
                if (updated) refreshResultsFromServer();
            }
        }, 0, 1000);
    }

    private void startDebugResultsRefresher() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement flagResponse = ServerRequestUtils.sendGet("/debugResultsUpdated");
                if (flagResponse == null || !flagResponse.isJsonObject()) return;
                boolean updated = flagResponse.getAsJsonObject().get("updated").getAsBoolean();
                if (updated) refreshDebugResultsFromServer();
            }
        }, 0, 1000);
    }

    private void startInitExecutionRefresher() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement flagResponse = ServerRequestUtils.sendGet("/initExecutionUpdated");
                if (flagResponse == null || !flagResponse.isJsonObject()) return;
                boolean updated = flagResponse.getAsJsonObject().get("updated").getAsBoolean();
                if (updated) initExecutionTables();
            }
        }, 0, 1000);
    }

    // ======= DATA REFRESH =======
    private void refreshInputsFromServer() {
        new Thread(() -> {
            JsonElement response = ServerRequestUtils.sendGet("/inputs");
            if (response == null || !response.isJsonObject()) return;

            var obj = response.getAsJsonObject();
            var varsEl = obj.get("inputs");
            var valuesEl = obj.get("values");
            if (varsEl == null || valuesEl == null) return;

            var gson = new Gson();
            var listType = new com.google.gson.reflect.TypeToken<List<String>>(){}.getType();
            List<String> names = gson.fromJson(varsEl, listType);
            List<String> values = gson.fromJson(valuesEl, listType);

            Platform.runLater(() -> {
                inputsList.getItems().setAll(names.stream().map(InputRow::new).toList());
                variablesTable.getItems().clear();
                lblCycles.setText("Cycles: 0");
            });
        }).start();
    }

    private void refreshResultsFromServer() {
        new Thread(() -> {
            JsonElement response = ServerRequestUtils.sendGet("/results");
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

    private void refreshDebugResultsFromServer() {
        new Thread(() -> {
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

    // ======= INIT =======
    private void initExecutionTables() {
        Platform.runLater(() -> {
            variablesTable.getItems().clear();
            inputsList.getItems().clear();
            lblCycles.setText("Cycles: 0");
        });
    }
}