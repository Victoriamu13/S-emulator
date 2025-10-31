package logic.system.user.engine;

import logic.engineFacade.api.EngineFacade;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EngineFacadeManager {
    //Username -> {progName -> engine}
    private static final Map<String, Map<String, EngineFacade>> userEngines = new ConcurrentHashMap<>();

    private EngineFacadeManager(){}

    public static synchronized void registerEngine(String username,String name, EngineFacade engine){
        if(username==null || name==null || engine ==null) return;
        userEngines.computeIfAbsent(username,u->new ConcurrentHashMap<>()).putIfAbsent(name,engine);
        System.out.println("[EngineFacadeManager] Registering new engine for user=" + username + ", program=" + name);

    }

    public static synchronized EngineFacade getEngine(String username,String name){
        if(username==null || name==null) return null;
        Map<String,EngineFacade> map=userEngines.get(username);
        return (map!=null) ? map.get(name) : null;
    }

    public static synchronized boolean hasEngine(String username, String name) {
        if (username == null || name == null) return false;
        Map<String, EngineFacade> map = userEngines.get(username);
        return map != null && map.containsKey(name);
    }

    public static synchronized void removeEngine(String username, String name) {
        if (username == null || name == null) return;
        Map<String, EngineFacade> map = userEngines.get(username);
        if (map != null) {
            map.remove(name);
            if (map.isEmpty()) userEngines.remove(username);
        }
        System.out.println("[EngineFacadeManager] Removed engine for user=" + username + ", program=" + name);
    }

    public static synchronized void clearEnginesForUser(String username) {
        userEngines.remove(username);
    }
}
