package logic.system.data.expansion;

import java.util.concurrent.ConcurrentHashMap;

public class FinalIndexManager {
    private static final ConcurrentHashMap<String, Integer> userFinalIndex = new ConcurrentHashMap<>();

    public static void set(String user, Integer index) {
        if (user != null) {
            if (index == null) userFinalIndex.remove(user);
            else userFinalIndex.put(user, index);
        }
    }

    public static Integer get(String user) {
        return userFinalIndex.get(user);
    }

    public static void clear(String user) {
        userFinalIndex.remove(user);
    }
}
