package logic.system.updates;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpdateFlagsManager {
    // Global flags (shared across all users)
    private static final Map<String, AtomicBoolean> globalFlags = new ConcurrentHashMap<>();

    // Per-user flags
    private static final Map<String, Map<String, AtomicBoolean>> userFlags = new ConcurrentHashMap<>();

    // ====== GLOBAL ======
    public static void markUpdated(String key) {
        if (key == null) return;
        globalFlags.computeIfAbsent(key, k -> new AtomicBoolean(true)).set(true);
    }

    public static boolean hasUpdated(String key) {
        if (key == null) return false;
        return globalFlags.getOrDefault(key, new AtomicBoolean(false)).get();
    }

    public static void clearFlag(String key) {
        if (key == null) return;
        globalFlags.computeIfAbsent(key, k -> new AtomicBoolean(false)).set(false);
    }



    // ====== PER USER ======
    public static void markUpdated(String key, String username) {
        if (key == null || username == null) return;
        userFlags.computeIfAbsent(key, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(username, u -> new AtomicBoolean())
                .set(true);
    }

    public static boolean hasUpdated(String key, String username) {
        if (key == null || username == null) return false;
        Map<String, AtomicBoolean> map = userFlags.get(key);
        if (map == null) return false;
        AtomicBoolean flag = map.computeIfAbsent(username, u -> new AtomicBoolean(true));
        return flag.get();
    }


    public static void clearFlag(String key, String username) {
        if (key == null || username == null) return;
        Map<String, AtomicBoolean> map = userFlags.get(key);
        if (map == null) return;
        map.computeIfAbsent(username, u -> new AtomicBoolean(true)).set(false);
    }
}
