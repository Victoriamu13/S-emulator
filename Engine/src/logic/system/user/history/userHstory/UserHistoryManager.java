package logic.system.user.history.userHstory;

import logic.engineFacade.model.RunRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserHistoryManager {
    private static final Map<String, List<UserHistory>> usersHistories = new ConcurrentHashMap<>();

    public static synchronized void addRun(String username, UserHistory history) {
        usersHistories.computeIfAbsent(username, k -> new ArrayList<>()).add(history);
    }

    public static synchronized List<UserHistory> getUserExecHistories(String username) {
        return usersHistories.getOrDefault(username, List.of());
    }

    public static synchronized RunRecord getRunRecord(String username, int runId) {
        List<UserHistory> histories = usersHistories.get(username);
        if (histories == null) return null;

        return histories.stream()
                .filter(h -> h.runID() == runId)
                .map(UserHistory::runRecord)
                .findFirst()
                .orElse(null);
    }

    public static synchronized void clearHistory(String user) {
        usersHistories.remove(user);
    }

}
