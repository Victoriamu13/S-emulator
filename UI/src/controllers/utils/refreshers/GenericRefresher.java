package controllers.utils.refreshers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableView;

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
    private final boolean userSelection;
    private boolean firstLoad = true;

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
        this(autoUpdate, updateEndpoint, dataEndpoint, table, listType, arrayField, false);
    }

    public GenericRefresher(BooleanProperty autoUpdate,
                            String updateEndpoint,
                            String dataEndpoint,
                            TableView<T> table,
                            Type listType,
                            String arrayField,
                            boolean userSelection) {
        this.autoUpdate = autoUpdate;
        this.updateEndpoint = updateEndpoint;
        this.dataEndpoint = dataEndpoint;
        this.table = table;
        this.listType = listType;
        this.arrayField = arrayField;
        this.userSelection = userSelection;
    }

    @Override
    public void run() {
        if (!autoUpdate.get()) return;

        boolean shouldFetch = firstLoad;

        JsonElement updateResponse = ServerRequestUtils.sendGet(updateEndpoint);
        if (updateResponse != null && updateResponse.isJsonObject()) {
            JsonObject obj = updateResponse.getAsJsonObject();
            if (obj.has("updated") && obj.get("updated").getAsBoolean()) {
                System.out.println("[Refresher] Update detected for " + updateEndpoint);

                shouldFetch = true;
            }
        }

        if (!shouldFetch) return;
        firstLoad = false;

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
        List<T> newItems = gson.fromJson(arrayElement, listType);
        System.out.println("[Refresher] Loaded " + newItems.size() + " items from " + dataEndpoint);

        Platform.runLater(() -> {
            List<T> currentItems = table.getItems();
            boolean sameContent = currentItems.size() == newItems.size()
                    && currentItems.containsAll(newItems)
                    && newItems.containsAll(currentItems);

            if (sameContent) return;
            System.out.println("[Refresher] Applying " + newItems.size() + " new items to table.");

            table.setItems(FXCollections.observableArrayList(newItems));
        });
    }
}