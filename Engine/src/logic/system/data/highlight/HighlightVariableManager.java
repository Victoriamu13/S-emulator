package logic.system.data.highlight;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HighlightVariableManager {
    private static final Map<String,String> userHighlights=new ConcurrentHashMap<>();

    private HighlightVariableManager() {}

    public static void setHighlight(String username,String variable){
        if(username==null) return;
        userHighlights.put(username,variable);
    }

    public static String getHighlight(String username) {
        return userHighlights.getOrDefault(username, "");
    }

    public static void clearHighlight(String username) {
        userHighlights.remove(username);
    }

}
