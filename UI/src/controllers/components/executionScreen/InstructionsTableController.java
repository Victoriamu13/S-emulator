package controllers.components.executionScreen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import controllers.utils.refreshers.GenericRefresher;
import controllers.utils.server.ServerRequestUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import logic.engineFacade.model.InstructionDTO;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class InstructionsTableController {
    Timer timer;
    private GenericRefresher<InstructionDTO> refresher;
    private final BooleanProperty autoUpdate=new SimpleBooleanProperty(true);

    @FXML private TableView<InstructionDTO> instructionsTable;
    @FXML private TableColumn<InstructionDTO, Number> colIndex;
    @FXML private TableColumn<InstructionDTO, String> colType;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colCommand;
    @FXML private TableColumn<InstructionDTO, String> colCycles;

    @FXML
    public void initialize() {
        colIndex.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().index()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type()));
        colLabel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().label()));
        colCommand.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().command()));
        colCycles.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cyclesText()));

        startInstructionsRefresher();  // updates instruction table
        startHighlightRefresher();     // listens for highlight changes
        setupSelectionListener();     // handles selections on table rows
    }

    // Refreshes the instructions table every 2 seconds if degree updated
    private void startInstructionsRefresher() {
        Type listType = new TypeToken<List<InstructionDTO>>(){}.getType();
        refresher = new GenericRefresher<>(autoUpdate,
                "/degreeUpdated", "/programData",
                instructionsTable, listType, "instructions");

        timer = new Timer(true);
        timer.schedule(refresher, 0, 2000);
    }

     // Checks if highlight changed
    private void startHighlightRefresher() {
        Timer highlightTimer = new Timer(true);

        highlightTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                JsonElement resp = ServerRequestUtils.sendGet("/highlightUpdated");
                if (resp == null || !resp.isJsonObject()) return;

                boolean updated = resp.getAsJsonObject()
                        .get("updated")
                        .getAsBoolean();

                if (updated) applyHighlight();
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
    private void applyHighlight() {
        JsonElement resp = ServerRequestUtils.sendGet("/highlightVariable");
        if (resp == null || !resp.isJsonObject()) return;

        JsonObject obj = resp.getAsJsonObject();
        if (!"SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) return;

        String var = obj.get("highlight").isJsonNull() ? null : obj.get("highlight").getAsString();

        Platform.runLater(() -> {
            if (var == null || var.isBlank()) {
                instructionsTable.setRowFactory(null);
                instructionsTable.refresh();
                return;
            }

            instructionsTable.setRowFactory(tv -> new TableRow<>() {
                @Override
                protected void updateItem(InstructionDTO item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setStyle("");
                        return;
                    }

                    boolean match = (item.command() != null && item.command().contains(var)) ||
                                    (item.label() != null && item.label().equals(var));
                    if (match) {
                        setStyle("-fx-background-color: yellow; -fx-font-weight: bold; -fx-text-fill: black;");
                    } else {
                        setStyle("");
                    }
                }
            });
            instructionsTable.refresh();
        });
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
}

