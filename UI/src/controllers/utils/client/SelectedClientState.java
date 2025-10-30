package controllers.utils.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;

public class SelectedClientState { //Synchronizing the current selection (program or function) with server.
    private static String selectedProgram = null;

    // Returns the name of the currently selected program.
    public static String getName() {
        return selectedProgram;
    }

     //Synchronize the current selection from the server-> Returns true if sync succeeded.
    public static boolean syncFromServer() {
        JsonElement response = ServerRequestUtils.sendGet("/selected");
        if (response == null || !response.isJsonObject()) return false;

        JsonObject obj = response.getAsJsonObject();
        if (!"SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) return false;

        selectedProgram = obj.get("name").getAsString();

        System.out.println("[SelectedClientState] synced with server → program=" + selectedProgram);
        return true;
    }


    public static void setProgram(String name) {
        selectedProgram = name;
        System.out.println("[SelectedClientState] locally set program=" + selectedProgram);
    }


    public static void clear() {
        selectedProgram = null;
    }
}
