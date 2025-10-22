package logic.system.programs.selectedProg;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SelectedProgramManager {
    // Map: username → { "type": "program" or "function", program / function name }
    private static final Map<String,Selection> selectedPrograms = new ConcurrentHashMap<>();;

    private record Selection(String type,String progName){}

    public static void setSelectedProgram(String username,String type, String progName){
        if (username == null || type == null || progName == null) return;
        selectedPrograms.put(username,new Selection(type,progName));
    }

    public static String getSelectedProgram(String username){
        Selection sel=selectedPrograms.get(username);
        return sel.progName();
    }

    public static String getSelectedType(String username){
        Selection sel=selectedPrograms.get(username);
        return sel.type();
    }

    public static void clear(String username){
        selectedPrograms.remove(username);
    }
}
