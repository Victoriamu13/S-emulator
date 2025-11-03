package logic.system.data.execution.runHistory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReRunStateManager {
    private static final Map<String, Boolean> reRunStates = new ConcurrentHashMap<>();


    public static synchronized void setReRun(String user, boolean state) {
        if(user==null) return;
        reRunStates.put(user, state);
    }

    public static synchronized boolean isReRun(String user) {
        if(user==null) return false;
        return reRunStates.getOrDefault(user, false);
    }

    public static synchronized void clear(String user) {
        if(user==null) return;
        reRunStates.remove(user);
    }
}
