package logic.system.user.history.userHstory;

import logic.engineFacade.model.RunRecord;
import logic.system.user.history.selectedUser.SelectedUserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserHistoryManager {
    private static final Map<String, List<UserHistory>> usersHistories = new ConcurrentHashMap<>();

    public static synchronized void addRun(String username, UserHistory history) {
        usersHistories.computeIfAbsent(username, k -> new ArrayList<>()).add(history);
        System.out.println("[UserHistoryManager] Added run #" + history.runID() + " for user " + username);

    }

    public static synchronized List<UserHistory> getUserExecHistories(String requesterUsername) {
        //Check if selected other user
        String selectedUser = SelectedUserManager.getSelectedUser(requesterUsername);
        if (selectedUser != null && usersHistories.containsKey(selectedUser)) {
            return usersHistories.get(selectedUser);
        }
        //IF not -> select himself
        return usersHistories.getOrDefault(requesterUsername, List.of());
    }

    public static synchronized List<UserHistory> getOwnHistories(String username) {
        return usersHistories.getOrDefault(username, List.of());
    }

    public static synchronized RunRecord getRunRecord(String username, int runId) {
        List<UserHistory> histories = getOwnHistories(username);
        if (histories == null || histories.isEmpty()) {
            System.out.println("[UserHistoryManager] No history found for user: " + username);
            return null;
        }

        return histories.stream()
                .filter(h -> h.runID() == runId)
                .map(UserHistory::runRecord)
                .findFirst()
                .orElse(null);
    }

}
