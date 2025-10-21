package controllers.utils.refreshers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;

import static controllers.utils.server.ServerResponseHandler.gson;

public class GenericRefresher<T> extends TimerTask {

    private final String updatedEndpoint;
    private final String dataEndpoint;
    private final BooleanProperty autoUpdate;
    private final TableView<T> table;
    private final Type listType;

    public GenericRefresher(BooleanProperty autoUpdate, String updatedEndpoint, String dataEndpoint,
                            TableView<T> table, Type listType) {
        this.autoUpdate = autoUpdate;
        this.updatedEndpoint = updatedEndpoint;
        this.dataEndpoint = dataEndpoint;
        this.table = table;
        this.listType = listType;
    }

    @Override
    public void run() {
        if (!autoUpdate.get()) return;

        JsonElement response = ServerRequestUtils.sendGet(updatedEndpoint); //check for updates
        if (response == null || !response.isJsonObject()) return;

        JsonObject obj = response.getAsJsonObject();
        if (obj.has("updated") && obj.get("updated").getAsBoolean()) {
            JsonElement dataResponse = ServerRequestUtils.sendGet(dataEndpoint); //get updated data
            if (dataResponse == null || !dataResponse.isJsonArray()) return;

            List<T> items = gson.fromJson(dataResponse, listType);

            Platform.runLater(() -> {
                ObservableList<T> observable = FXCollections.observableArrayList(items);
                table.setItems(observable);
            });
        }
    }
}