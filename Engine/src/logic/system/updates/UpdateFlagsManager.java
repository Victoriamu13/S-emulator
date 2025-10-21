package logic.system.updates;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpdateFlagsManager {
    private static final ConcurrentHashMap<String, AtomicBoolean>flags=new ConcurrentHashMap<>();

    public static void markUpdated(String key){
        flags.computeIfAbsent(key,k->new AtomicBoolean(false)).set(true);
    }

    public static boolean hasUpdated(String key){
        return flags.getOrDefault(key,new AtomicBoolean(false)).get();
    }

    public static void clearFlag(String key){
        flags.computeIfAbsent(key,k->new AtomicBoolean(false)).set(false);
    }
}
