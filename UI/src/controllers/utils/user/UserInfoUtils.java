package controllers.utils.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;
import logic.system.api.SelectedClientState;

public class UserInfoUtils {
    public static void refreshUserCreditsFromServer() {
        JsonElement response = ServerRequestUtils.sendGet("/currentUser");
        if (response == null || !response.isJsonObject()) return;

        JsonObject obj = response.getAsJsonObject();
        if (!"SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) return;

        int credits = obj.has("credits") ? obj.get("credits").getAsInt() : 0;
        SelectedClientState.setCurrentCredits(credits);
    }

}
