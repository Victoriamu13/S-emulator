package logic.system.data.execution.execMode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutionModeManager {

    //user -> mode
    private static final Map<String, String> userModes = new ConcurrentHashMap<>();

    public static void setMode(String username, String mode) {
        if (username == null || mode == null) return;
        userModes.put(username, mode.toUpperCase());
    }

    public static String getMode(String username) {
        if (username == null) return "NORMAL";
        return userModes.getOrDefault(username, "NORMAL");
    }

    public static void clearMode(String username) {
        if (username != null)
            userModes.remove(username);
    }
}
