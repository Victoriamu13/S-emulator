package logic.domain.program.functions;

import logic.domain.instructions.SInstruction;
import logic.domain.variable.SVars;

import java.util.*;


public class FunctionLookupImpl implements FunctionLookup {
    // functionName → body
    private final Map<String, List<SInstruction>> functions = new LinkedHashMap<>();

    // functionName → formal args (x1..xK)
    private final Map<String, List<SVars>> functionArgs = new LinkedHashMap<>();

    // functionName → user-facing display string
    private final Map<String, String> functionUserStrings = new LinkedHashMap();

    private static String key(String name) {
        return name == null ? "" : name.trim();
}  // normalize

    public void register(String name,String userString, List<SVars> args, List<SInstruction> body){
        String internalKey = key(name);   // normalized internal

        functions.put(internalKey, body);      // store function body
        functionArgs.put(internalKey, args);  // store formal args
        if (userString != null && !userString.isBlank()) {
            functionUserStrings.put(internalKey, userString);  // store UI name
        }
    }

    @Override
    public List<SInstruction> bodyOf(String functionName){
        String key= key(functionName);
        List<SInstruction> localBody= functions.get(key);

        if(localBody != null && !localBody.isEmpty())return localBody;

        if (logic.system.programs.functions.repository.FunctionRepository.functionExists(functionName)) {
            List<SInstruction> globalBody = logic.system.programs.functions.repository.FunctionRepository.getFunctionBody(functionName);
            if (!globalBody.isEmpty()) {
                return globalBody;
            }
        }
        return List.of();
    }

    @Override
    public List<SVars> argsOf(String functionName) {
        return functionArgs.getOrDefault(key(functionName), List.of());
    }

    @Override
    public String userStringOf(String functionName) {
        String key= key(functionName);
        String localUserString=functionUserStrings.get(key);
        if(localUserString!=null) return localUserString;

        if(logic.system.programs.functions.repository.FunctionRepository.functionExists(functionName)){
            return logic.system.programs.functions.repository.FunctionRepository
                    .allFunctions()
                    .stream()
                    .filter(f->f.funcName().equalsIgnoreCase(functionName))
                    .findFirst()
                    .map(f->f.userString())
                    .orElse(functionName);
        }
        return functionName;
    }


    @Override
    public Set<String> allFunctionNames() {
        return new LinkedHashSet<>(functions.keySet());
    }

}
