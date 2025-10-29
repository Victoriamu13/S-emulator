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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class HistoryChainController {
    Timer timer;
    private GenericRefresher<InstructionDTO> refresher;
    private final BooleanProperty autoUpdate=new SimpleBooleanProperty(true);
    private String lastHighlight = "";

    @FXML private TableView<InstructionDTO> historyTable;
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

        startRefresher();
        startHighlightRefresher();
    }

    private void startRefresher(){
        Type listType=new TypeToken<List<InstructionDTO>>(){}.getType();
        refresher=new GenericRefresher<>(autoUpdate,"/historyChainUpdated","/historyChain",
                historyTable,listType,"chain");

        timer=new Timer(true);
        timer.schedule(refresher,0,2000);
    }

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

                if (updated) {
                    applyHighlight();
                }
            }
        }, 0, 1000);
    }


    private void applyHighlight() {
        JsonElement resp = ServerRequestUtils.sendGet("/highlightVariable");
        if (resp == null || !resp.isJsonObject()) return;

        JsonObject obj = resp.getAsJsonObject();
        if (!"SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) return;

        String var = obj.get("highlight").isJsonNull() ? null : obj.get("highlight").getAsString();
        if (Objects.equals(var, lastHighlight)) return;
        lastHighlight = var;

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
                            ? "-fx-background-color: yellow; -fx-font-weight: bold; -fx-text-fill: black;"
                            : "");
                }
            });
            historyTable.refresh();
        });
    }
}


