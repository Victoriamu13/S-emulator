package logic.system.updates;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class UpdateFlagsManager {
    // Global flags (shared across all users)
    private static final Map<String, AtomicBoolean> globalFlags = new ConcurrentHashMap<>();

    // Per-user flags
    private static final Map<String, Map<String, AtomicBoolean>> userFlags = new ConcurrentHashMap<>();

    // ====== GLOBAL ======
    public static void markUpdated(String key) {
        if (key == null) return;
        globalFlags.computeIfAbsent(key, k -> new AtomicBoolean()).set(true);
        System.out.println("[FLAGS] Global flag marked: " + key);

    }

    public static boolean hasUpdated(String key) {
        if (key == null) return false;
        return globalFlags.getOrDefault(key, new AtomicBoolean(false)).get();
    }

    public static void clearFlag(String key) {
        if (key == null) return;
        globalFlags.computeIfAbsent(key, k -> new AtomicBoolean()).set(false);
        System.out.println("[FLAGS] Global flag cleared: " + key);

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
        AtomicBoolean flag = map.get(username);
        return flag != null && flag.get();
    }


    public static void clearFlag(String key, String username) {
        if (key == null || username == null) return;
        Map<String, AtomicBoolean> map = userFlags.get(key);
        if (map == null) return;
        AtomicBoolean flag = map.get(username);
        if (flag != null) flag.set(false);
        System.out.printf("[FLAGS] Cleared user=%s key=%s%n", username, key);
    }

    public static String debugFlags() {
        String global = globalFlags.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue().get())
                .collect(Collectors.joining(", ", "{", "}"));

        String users = userFlags.entrySet().stream()
                .map(u -> u.getKey() + "=" +
                        u.getValue().entrySet().stream()
                                .map(e -> e.getKey() + "=" + e.getValue().get())
                                .collect(Collectors.joining(", ", "{", "}")))
                .collect(Collectors.joining(", ", "{", "}"));

        return "GLOBAL=" + global + " | USERS=" + users;
    }
}
