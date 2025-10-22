package logic.domain.program.functions;

import logic.domain.instructions.SInstruction;
import logic.domain.variable.SVars;
import logic.system.programs.functions.repository.GlobalFunctionRepository;

import java.util.*;


public class FunctionRepository implements FunctionLookup {
    // functionName → body
    private final Map<String, List<SInstruction>> functions = new LinkedHashMap<>();

    // functionName → formal args (x1..xK)
    private final Map<String, List<SVars>> functionArgs = new LinkedHashMap<>();

    // functionName → user-facing display string
    private final Map<String, String> functionUserStrings = new LinkedHashMap();

    // userString → functionName
    private final Map<String, String> userStringToName = new LinkedHashMap<>();

    private static String key(String name) {
        return name == null ? "" : name.trim().toUpperCase(Locale.ROOT);
}  // normalize

    public void register(String name,String userString, List<SVars> args, List<SInstruction> body){
        String internalKey = key(name);   // normalized internal
        String userKey = key(userString);  // normalized user-facing

        functions.put(internalKey, body);      // store function body
        functionArgs.put(internalKey, args);  // store formal args
        functionUserStrings.put(internalKey, userString);  // store UI name
        userStringToName.put(userKey, internalKey);        // reverse lookup
    }

    @Override
    public List<SInstruction> bodyOf(String functionName){
        String key= key(functionName);
        List<SInstruction> localBody= functions.get(key);

        if(localBody != null && !localBody.isEmpty())return localBody;

        if (GlobalFunctionRepository.functionExists(functionName)) {
            List<SInstruction> globalBody = GlobalFunctionRepository.getFunctionBody(functionName);
            if (globalBody != null && !globalBody.isEmpty()) {
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

        if(GlobalFunctionRepository.functionExists(functionName)){
            return GlobalFunctionRepository
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

    @Override
    public String internalNameOf(String userString) {
        return userStringToName.getOrDefault(userString, userString);
    }

    @Override
    public Map<String, List<SInstruction>> allFunctionsBodies() {
        return new LinkedHashMap<>(functions);
    }

}
