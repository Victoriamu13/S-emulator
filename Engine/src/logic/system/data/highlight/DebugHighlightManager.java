package logic.system.data.highlight;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DebugHighlightManager {
    // key = username, value = map<programName, currentInstructionIndex>
    private static final Map<String, Map<String, Integer>> userProgramInstructions = new ConcurrentHashMap<>();

    private DebugHighlightManager() {}

    public static void setCurrentInstruction(String username, String program, int index) {
        if (username == null || program == null) return;
        userProgramInstructions.computeIfAbsent(username, u -> new ConcurrentHashMap<>()).put(program, index);
    }


    public static int getCurrentInstruction(String username, String program) {
        if (username == null || program == null) return -1;
        Map<String, Integer> programMap = userProgramInstructions.get(username);
        if (programMap == null) return -1;
        return programMap.get(program);
    }


    public static void clearCurrentInstruction(String username, String program) {
        if (username == null || program == null) return;

        Map<String, Integer> programMap = userProgramInstructions.get(username);
        if (programMap != null) {
            programMap.remove(program);
            if (programMap.isEmpty()) userProgramInstructions.remove(username);
        }
    }


    public static void clearHighlight(String username) {
        if (username == null) return;
        userProgramInstructions.remove(username);
    }
}
