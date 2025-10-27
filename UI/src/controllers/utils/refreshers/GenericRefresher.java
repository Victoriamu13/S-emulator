package controllers.utils.refreshers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableView;
import logic.engineFacade.model.InstructionDTO;

import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;

import static controllers.utils.server.ServerResponseHandler.gson;

public class GenericRefresher<T> extends TimerTask {

    private final BooleanProperty autoUpdate;
    private final String updateEndpoint;
    private final String dataEndpoint;
    private final TableView<T> table;
    private final Type listType;
    private final String arrayField;

    public GenericRefresher(BooleanProperty autoUpdate,
                            String updateEndpoint,
                            String dataEndpoint,
                            TableView<T> table,
                            Type listType) {
        this(autoUpdate, updateEndpoint, dataEndpoint, table, listType, null);
    }

    public GenericRefresher(BooleanProperty autoUpdate,
                            String updateEndpoint,
                            String dataEndpoint,
                            TableView<T> table,
                            Type listType,
                            String arrayField) {
        this.autoUpdate = autoUpdate;
        this.updateEndpoint = updateEndpoint;
        this.dataEndpoint = dataEndpoint;
        this.table = table;
        this.listType = listType;
        this.arrayField = arrayField;
    }

    @Override
    public void run() {
        if (!autoUpdate.get()) return;
        JsonElement updateResponse = ServerRequestUtils.sendGet(updateEndpoint);
        if (updateResponse == null) return;

        JsonObject obj = updateResponse.getAsJsonObject();
        if (!obj.has("updated") || !obj.get("updated").getAsBoolean()) return;

        JsonElement dataResponse = ServerRequestUtils.sendGet(dataEndpoint);
        if (dataResponse == null) return;

        JsonElement arrayElement = null;
        if (arrayField != null && dataResponse.isJsonObject()) {
            JsonObject dataObj = dataResponse.getAsJsonObject();
            arrayElement = dataObj.get(arrayField);
        } else if (dataResponse.isJsonArray()) {
            arrayElement = dataResponse;
        }

        if (arrayElement == null || !arrayElement.isJsonArray()) return;
        List<T> items = gson.fromJson(arrayElement, listType);
        Platform.runLater(() -> table.setItems(FXCollections.observableArrayList(items)));
    }
}
