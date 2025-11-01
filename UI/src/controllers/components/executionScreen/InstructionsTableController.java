package controllers.components.executionScreen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import controllers.utils.refreshers.GenericActionsRefresher;
import controllers.utils.refreshers.GenericRefresher;
import controllers.utils.refreshers.TimerManager;
import controllers.utils.server.ServerRequestUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import logic.domain.architecture.ArchitectureGen;
import logic.engineFacade.model.InstructionDTO;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import static controllers.screens.ScreenManager.EXECUTION;

public class InstructionsTableController {
    private final Timer timer = new Timer(true);

    private GenericRefresher<InstructionDTO> refresher;
    private final BooleanProperty autoUpdate=new SimpleBooleanProperty(true);
    private String lastHighlight = "";  // From highlight combo-box (yellow)
    private int currentDebugIndex = -1;       // From active debug session (blue)

    @FXML private TableView<InstructionDTO> instructionsTable;
    @FXML private TableColumn<InstructionDTO, Number> colIndex;
    @FXML private TableColumn<InstructionDTO, String> colType;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colCommand;
    @FXML private TableColumn<InstructionDTO, String> colCycles;
    @FXML private Label lblArchI;
    @FXML private Label lblArchII;
    @FXML private Label lblArchIII;
    @FXML private Label lblArchIV;

    @FXML
    public void initialize() {
        TimerManager.register(EXECUTION, timer);

        setupColumns();
        setupSelectionListener();     // handles selections on table rows
        setupUnifiedRowFactory();

        startInstructionsRefresher();  // updates instruction table
        startVariableHighlightRefresher();     // listens for highlight changes
        startHighlightClearRefresher(); //clears highlighted instructions
        startDebugInstructionClearRefresher();
        startDebugModeWatcher();
        refreshArchitectureSummaryFromServer(); //Init architecture summary line
        startArchitectureSummaryRefresher();
        startArchitectureLabelClearRefresher();

    }

    private void setupColumns() {
        colIndex.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().index()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type()));
        colLabel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().label()));
        colCommand.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().command()));
        colCycles.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cyclesText()));
    }

    private void setupUnifiedRowFactory() {
        instructionsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(InstructionDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    return;
                }

                boolean isDebugRow = item.index() == currentDebugIndex;
                boolean containsVar = lastHighlight != null && !lastHighlight.isBlank() &&
                        ((item.command() != null && item.command().contains(lastHighlight))
                                || (item.label() != null && item.label().equals(lastHighlight)));

                // Apply combined styling rules
                if (isDebugRow && containsVar) {
                    // Both debug and variable highlight → green
                    setStyle("-fx-background-color: #81C784; -fx-font-weight: bold;");
                } else if (isDebugRow) {
                    // Debug only → blue
                    setStyle("-fx-background-color: #90CAF9; -fx-font-weight: bold;");
                } else if (containsVar) {
                    // Variable highlight only → yellow
                    setStyle("-fx-background-color: yellow; -fx-font-weight: bold; -fx-text-fill: black;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    // Refreshes the instructions table every 2 seconds if degree updated
    private void startInstructionsRefresher() {
        Type listType = new TypeToken<List<InstructionDTO>>(){}.getType();
        refresher = new GenericRefresher<>(autoUpdate,
                "/degreeUpdated", "/programData",
                instructionsTable, listType,"instructions");

        timer.schedule(refresher, 0, 2000);
    }

    // ======= ARCHITECTURE SUMMARY LINE ======
    private void startArchitectureSummaryRefresher() {
        timer.schedule(
                new GenericActionsRefresher("/architectureSummaryUpdated", this::refreshArchitectureSummaryFromServer),
                0, 1500);
    }

    private void refreshArchitectureSummaryFromServer() {
        JsonElement resp = ServerRequestUtils.sendGet("/architectureSummary");
        if (resp == null || !resp.isJsonObject()) return;

        JsonObject obj = resp.getAsJsonObject();
        if (!"SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) return;

        JsonObject architectures = obj.getAsJsonObject("architectures");
        if (architectures == null) return;

        String selectedArch = getSelectedArchitectureFromServer();

        Platform.runLater(() -> {
            updateArchLabel(lblArchI, architectures.getAsJsonObject("I"), selectedArch);
            updateArchLabel(lblArchII, architectures.getAsJsonObject("II"), selectedArch);
            updateArchLabel(lblArchIII, architectures.getAsJsonObject("III"), selectedArch);
            updateArchLabel(lblArchIV, architectures.getAsJsonObject("IV"), selectedArch);
        });
    }

    private String getSelectedArchitectureFromServer() {
        JsonElement selEl = ServerRequestUtils.sendGet("/getSelectedArchitecture");
        if (selEl == null || !selEl.isJsonObject()) return null;

        JsonObject selObj = selEl.getAsJsonObject();
        if (!"SUCCESS".equalsIgnoreCase(selObj.get("state").getAsString())) return null;

        return selObj.has("selected") ? selObj.get("selected").getAsString() : null;
    }

    private void updateArchLabel(Label label, JsonObject info,String selectedArch) {
        if (info == null) return;

        final String RED = "-fx-text-fill: red; -fx-font-weight: bold;";
        final String BLACK = "-fx-text-fill: black; -fx-font-weight: normal;";

        int required  = info.get("supported").getAsInt();
        String archName = info.get("archName").getAsString();

        label.setText(archName + ": " + required );

        if (selectedArch == null || selectedArch.isBlank()) {
            label.setStyle(BLACK);
            return;
        }
        int currentLevel  = ArchitectureGen.valueOf(archName).ordinal();
        int selectedLevel = ArchitectureGen.valueOf(selectedArch).ordinal();
        if (currentLevel > selectedLevel && required > 0) {
            label.setStyle(RED);
        } else {
            label.setStyle(BLACK);
        }
    }

    // ======= DEBUG HIGHLIGHT =======
    private void startDebugModeWatcher() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement resp = ServerRequestUtils.sendGet("/getExecutionMode");
                if (resp == null || !resp.isJsonObject()) return;

                JsonObject obj = resp.getAsJsonObject();
                if (!obj.has("mode")) return;

                String mode = obj.get("mode").getAsString();
                boolean isDebug = "DEBUG".equalsIgnoreCase(mode);

                if (isDebug) {
                  startInDebugHighlightRefresher();
                    cancel();
                }
            }
        }, 0, 1000);
    }


    // When user choose instruction → update history chain + variable list
    private void setupSelectionListener(){
        instructionsTable.setOnMouseClicked(event->{
            InstructionDTO selected=instructionsTable.getSelectionModel().getSelectedItem();
            if(selected==null)return;

            int index=selected.index();
            int currentDegree = getCurrentDegreeFromServer();

            RequestBody body=new FormBody.Builder()
                    .add("index",String.valueOf(index))
                    .build();
            new Thread(()->ServerRequestUtils.sendPost("/historyChain",body)).start();

            RequestBody varsBody = new FormBody.Builder()
                    .add("degree", String.valueOf(currentDegree))
                    .add("finalIndex", String.valueOf(index))
                    .build();
            new Thread(() ->ServerRequestUtils.sendPost("/programVariables", varsBody)).start();
        });
    }

    // Applies highlight color to matching rows
    private void startVariableHighlightRefresher() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement resp = ServerRequestUtils.sendGet("/highlightVariable");
                if (resp == null || !resp.isJsonObject()) return;

                JsonObject obj = resp.getAsJsonObject();
                if (!"SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) return;

                String var = obj.get("highlight").isJsonNull() ? "" : obj.get("highlight").getAsString();
                if (Objects.equals(var, lastHighlight)) return;
                lastHighlight = var;

                Platform.runLater(() -> instructionsTable.refresh());

            }
        }, 0, 1000);
    }

    private void startInDebugHighlightRefresher() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement resp = ServerRequestUtils.sendGet("/currentInstruction");
                if (resp == null || !resp.isJsonObject()) return;

                JsonObject obj = resp.getAsJsonObject();
                if (!"SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) return;

                int index = obj.get("index").getAsInt();

                currentDebugIndex = index;
                Platform.runLater(() -> instructionsTable.refresh());
            }
        }, 0, 1000);
    }

  private void startHighlightClearRefresher() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement resp = ServerRequestUtils.sendGet("/highlightInstructionsClearUpdated");
                if (resp == null || !resp.isJsonObject()) return;

                boolean updated = resp.getAsJsonObject().get("updated").getAsBoolean();
                if (!updated) return;

                Platform.runLater(() -> {
                    lastHighlight = "";
                    currentDebugIndex = -1;
                    instructionsTable.refresh();
                });
            }
        }, 0, 1000);
    }

    private void startArchitectureLabelClearRefresher(){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement resp = ServerRequestUtils.sendGet("/architectureLabelClearUpdated");
                if (resp == null || !resp.isJsonObject()) return;

                boolean updated = resp.getAsJsonObject().get("updated").getAsBoolean();
                if (!updated) return;

                Platform.runLater(() -> {
                    List<Label> allLabels = List.of(lblArchI, lblArchII, lblArchIII, lblArchIV);
                    allLabels.forEach(lbl -> lbl.setStyle("-fx-text-fill: black; -fx-font-weight: normal;"));
                });
            }
        }, 0, 1000);
    }


    // Retrieves current degree from server to send correct requests
    private int getCurrentDegreeFromServer() {
        JsonElement response = ServerRequestUtils.sendGet("/programData");
        if (response == null || !response.isJsonObject()) return 0;

        JsonObject obj = response.getAsJsonObject();
        if (!obj.has("state") || !"SUCCESS".equalsIgnoreCase(obj.get("state").getAsString()))
            return 0;

        return obj.get("currentDegree").getAsInt();
    }

    private void startDebugInstructionClearRefresher() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement resp = ServerRequestUtils.sendGet("/debugInstructionClearUpdated");
                if (resp == null || !resp.isJsonObject()) return;

                boolean updated = resp.getAsJsonObject().get("updated").getAsBoolean();
                if (!updated) return;

                Platform.runLater(() -> {
                    currentDebugIndex = -1; // remove blue highlight
                    instructionsTable.refresh();
                });
            }
        }, 0, 1000);
    }


}