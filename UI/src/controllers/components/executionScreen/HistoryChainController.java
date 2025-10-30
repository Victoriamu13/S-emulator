package controllers.components.executionScreen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import controllers.utils.refreshers.GenericActionsRefresher;
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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class HistoryChainController {
    private final Timer timer = new Timer(true);
    private final BooleanProperty autoUpdate=new SimpleBooleanProperty(true);

    @FXML private TableView<InstructionDTO> historyTable;
    @FXML private TableColumn<InstructionDTO, Number> colIndex;
    @FXML private TableColumn<InstructionDTO, String> colType;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colCommand;
    @FXML private TableColumn<InstructionDTO, String> colCycles;

    @FXML
    public void initialize() {
        setupColumns();
        startRefresher();
    }

    private void setupColumns() {
        colIndex.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().index()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type()));
        colLabel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().label()));
        colCommand.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().command()));
        colCycles.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cyclesText()));
    }

    private void startRefresher(){
        Type listType = new TypeToken<List<InstructionDTO>>(){}.getType();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!autoUpdate.get()) return;

                JsonElement resp = ServerRequestUtils.sendGet("/historyChain");
                if (resp == null || !resp.isJsonObject()) return;
                JsonObject obj = resp.getAsJsonObject();

                // history chain
                if (obj.has("chain")) {
                    List<InstructionDTO> chain = new Gson().fromJson(obj.get("chain"), listType);
                    Platform.runLater(() -> historyTable.getItems().setAll(chain));
                }

                // highlight
                if (obj.has("highlight")) {
                    String var = obj.get("highlight").getAsString();
                    applyHighlight(var);
                }
            }
        }, 0, 1500);
        System.out.println("[HistoryChain] Refresher started (updates every 1.5s)");

    }


    private void applyHighlight(String var) {
        Platform.runLater(() -> {
            if (var == null || var.isBlank()) {
                historyTable.setRowFactory(null);
                historyTable.refresh();
                return;
            }

            historyTable.setRowFactory(tv -> new TableRow<>() {
                @Override
                protected void updateItem(InstructionDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("");
                        return;
                    }

                    boolean match = (item.command() != null && item.command().contains(var))
                            || (item.label() != null && item.label().equals(var));
                    setStyle(match
                            ? "-fx-background-color: yellow; -fx-font-weight: bold; -fx-text-fill: black;" : "");
                }
            });
            historyTable.refresh();
        });
    }
}
