package logic.system.data.expansion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DegreeManager {
    // Map: username → current degree
    private static final Map<String,Integer> usersDegrees=new ConcurrentHashMap<>();

    private DegreeManager(){}

    public static void setDegree(String username,int degree){
        if(username==null) return;
        usersDegrees.put(username,degree);
    }

    public static int getDegree(String username){
        if(username==null) return 0;
        return usersDegrees.getOrDefault(username, 0);
    }

    public static void resetDegree(String username) {
        if (username == null) return;
        usersDegrees.put(username, 0);
    }
}
