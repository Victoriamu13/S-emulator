package controllers.utils.refreshers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

import static controllers.utils.server.ServerResponseHandler.gson;

public class RefresherUtils{

    public static <T> List<T> getListFromServer(String dataEndpoint, String arrayField, Type listType) {
        JsonElement response = ServerRequestUtils.sendGet(dataEndpoint);
        if (response == null) return null;

        JsonElement arr = null;
        if (arrayField == null && response.isJsonArray()) {
            arr = response;
        } else if (response.isJsonObject()) {
            JsonObject obj = response.getAsJsonObject();
            if (obj.has(arrayField))
                arr = obj.get(arrayField);
        }

        if (arr == null || !arr.isJsonArray()) return null;
        return gson.fromJson(arr, listType);
    }

    public static <T> void updateListView(javafx.scene.control.ListView<T> list, List<T> items) {
        if (items == null) return;
        Platform.runLater(() -> list.setItems(FXCollections.observableArrayList(items)));
    }

    public static <T> void updateTableView(javafx.scene.control.TableView<T> table, List<T> items) {
        if (items == null) return;
        Platform.runLater(() -> table.setItems(FXCollections.observableArrayList(items)));
    }

    public static <T, R> List<R> map(List<T> source, Function<T, R> mapper) {
        return source.stream().map(mapper).toList();
    }
}