package logic.domain.program.functions;

import logic.domain.instructions.SInstruction;

import java.util.*;

public final class GlobalFunctionRepository {
    private static final Map<String, List<SInstruction>> globalFunctions=new LinkedHashMap<>();

    private GlobalFunctionRepository(){}

    public static synchronized void addFunctions(Map<String,List<SInstruction>> functions){
        if(functions==null) return;

        for(var entry : functions.entrySet()){
            String fnName= entry.getKey().trim().toUpperCase(Locale.ROOT);
            List<SInstruction> body=entry.getValue();

            if(!globalFunctions.containsKey(fnName)){
                globalFunctions.put(fnName,body);
            }
        }
    }


    public static synchronized boolean functionExists(String fnName){
        if(fnName==null)return false;

        return globalFunctions.containsKey(fnName.trim().toUpperCase(Locale.ROOT));
    }


    public static synchronized List<SInstruction> getFunctionBody(String fnName){
        if(fnName==null) return List.of();

        return globalFunctions.get(fnName.trim().toUpperCase(Locale.ROOT));
    }


    public static synchronized boolean compareBodies(List<SInstruction> a,List<SInstruction> b){
        if(a.size() != b.size()) return false;

        for(int i=0;i<a.size();i++){
            SInstruction aInst=a.get(i);
            SInstruction bInst=b.get(i);

            if(!aInst.getName().equals(bInst.getName())) return false;
            if(!aInst.getLabel().getLabelRepresentation().equals(bInst.getLabel().getLabelRepresentation()))return false;
            if(!aInst.getVariable().getRepresentation().equals(bInst.getVariable().getRepresentation())) return false;
        }
        return true;
    }

    public static synchronized Set<String> getAllFunctionNames() {
        if (globalFunctions.isEmpty()) return Set.of();
        return new HashSet<>(globalFunctions.keySet());
    }


}
