package logic.system.user.info;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private static final Set<String> activeUsers = ConcurrentHashMap.newKeySet();

    public static boolean userExists(String username) {
        return activeUsers.contains(username);
    }

    public static boolean addUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return activeUsers.add(username.trim());
    }

    public static void removeUser(String username) {
        if (username != null) {
            activeUsers.remove(username);
        }
    }

    public static Set<String> getAllActiveUsers() {
        return activeUsers;
    }
}
