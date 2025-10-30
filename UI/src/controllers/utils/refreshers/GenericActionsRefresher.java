package controllers.utils.refreshers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;

import java.util.TimerTask;

public class GenericActionsRefresher extends TimerTask {
    private final String updateEndpoint;
    private final Runnable action;

    public GenericActionsRefresher(String updateEndpoint, Runnable action) {
        this.updateEndpoint = updateEndpoint;
        this.action = action;
    }

    @Override
    public void run() {
        JsonElement resp = ServerRequestUtils.sendGet(updateEndpoint);
        if (resp == null || !resp.isJsonObject()) return;

        JsonObject obj = resp.getAsJsonObject();
        if (!obj.has("updated")) return;

        boolean updated = obj.get("updated").getAsBoolean();
        if (updated) action.run();
    }
}
