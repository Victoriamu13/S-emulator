package logic.system.data.execution.runHistory;

import logic.engineFacade.model.RunRecord;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReRunHistoryManager {
    private static final Map<String, RunRecord> reRunHistory = new ConcurrentHashMap<>();

    public static synchronized void setRecord(String user, RunRecord record) {
        reRunHistory.put(user, record);
    }

    public static synchronized RunRecord getRecord(String user) {
        return reRunHistory.get(user);
    }

    public static synchronized void clear(String user) {
        reRunHistory.remove(user);
    }
}
