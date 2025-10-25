package logic.system.data.expansion;

import logic.engineFacade.model.InstructionDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class HistoryChainManager {
    private static final Map<String, List<InstructionDTO>> userHistoryChains=new ConcurrentHashMap<>();

    private HistoryChainManager(){}

    public static synchronized void setHistoryChain(String username,List<InstructionDTO> chain){
        if(username==null || chain==null) return;
        userHistoryChains.put(username,chain);
    }

    public static synchronized List<InstructionDTO> getChain(String username) {
        if (username == null) return List.of();
        return userHistoryChains.getOrDefault(username, List.of());
    }

    public static synchronized void clearHistoryChain(String username){
        if(username!=null){
            userHistoryChains.remove(username);
        }
    }
}