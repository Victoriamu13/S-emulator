package logic.system.data.architecture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ArchitectureManager {
    // user → (program → architecture)
    private static final Map<String, Map<String, String>> userPrograms = new ConcurrentHashMap<>();

    public static void setArchitecture(String username, String programName, String architecture) {
        if (username == null || programName == null || architecture == null) return;
        userPrograms.computeIfAbsent(username, u -> new ConcurrentHashMap<>()).put(programName, architecture);
    }

    public static String getArchitecture(String username, String programName) {
        if (username == null || programName == null) return null;
        Map<String, String> programs = userPrograms.get(username);
        if (programs == null) return null;
        return programs.getOrDefault(programName, "I");
    }

}
