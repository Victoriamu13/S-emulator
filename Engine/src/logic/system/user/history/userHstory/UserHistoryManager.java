package logic.system.user.history.userHstory;

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
    public static synchronized void clear(String user) {
        usersHistories.remove(user);
    }

}
