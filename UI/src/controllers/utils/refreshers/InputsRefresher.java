package controllers.utils.refreshers;

import controllers.components.executionScreen.execution.InputRow;
import controllers.utils.server.ServerRequestUtils;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.ListView;

import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class InputsRefresher extends TimerTask {
    private final BooleanProperty autoUpdate;
    private final ListView<InputRow> listView;
    private final Type listType;

    public InputsRefresher(BooleanProperty autoUpdate, ListView<InputRow> listView, Type listType) {
        this.autoUpdate = autoUpdate;
        this.listView = listView;
        this.listType = listType;
    }

    @Override
    public void run() {
        if (!autoUpdate.get()) return;

        var updated = ServerRequestUtils.sendGet("/inputsUpdated");
        if (updated == null || !updated.getAsJsonObject().get("updated").getAsBoolean()) return;

        List<String> names = RefresherUtils.getListFromServer("/newRun", "inputs", listType);
        if (names == null) return;

        List<InputRow> rows = names.stream().map(InputRow::new).collect(Collectors.toList());
        RefresherUtils.updateListView(listView, rows);
    }
}
