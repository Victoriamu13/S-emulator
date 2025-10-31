package logic.system.data.expansion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DegreeManager {
    // Map: username → (programName → degree)
    private static final Map<String, Map<String, Integer>> userProgramDegrees = new ConcurrentHashMap<>();

    private DegreeManager() {}

    public static void setDegree(String username, String programName, int degree) {
        if (username == null || programName == null) return;
        userProgramDegrees.computeIfAbsent(username, u -> new ConcurrentHashMap<>()).put(programName, degree);
    }

    public static int getDegree(String username, String programName) {
        if (username == null || programName == null) return 0;
        Map<String, Integer> programMap = userProgramDegrees.get(username);
        return (programMap != null) ? programMap.get(programName) : 0;
    }

    public static void resetDegree(String username, String programName) {
        if (username == null || programName == null) return;
        Map<String, Integer> programMap = userProgramDegrees.get(username);
        if (programMap != null) programMap.put(programName, 0);
    }

    public static void clearUser(String username) {
        if (username != null)
            userProgramDegrees.remove(username);
    }
}
