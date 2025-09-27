package logic.domain.program.functions;

import logic.domain.instructions.SInstruction;
import logic.domain.variable.SVars;
import java.util.*;

import static java.util.LinkedHashMap.newLinkedHashMap;

public class FunctionRepository implements FunctionLookup {
    private final Map<String, List<SInstruction>> functions = new LinkedHashMap<>();
    private final Map<String, List<SVars>> functionArgs = new LinkedHashMap<>();
    private final Map<String, String> functionUserStrings = new LinkedHashMap();
    private final Map<String, String> userStringToName = new LinkedHashMap<>();

    private static String key(String name) {
        return name == null ? "" : name.trim().toUpperCase(Locale.ROOT);
    }

    public void register(String name,String userString, List<SVars> args, List<SInstruction> body){
        String internalKey = key(name);
        String userKey = key(userString);

        functions.put(internalKey, body);
        functionArgs.put(internalKey, args);
        functionUserStrings.put(internalKey, userString);
        userStringToName.put(userKey, internalKey);
    }

    @Override
    public List<SInstruction> bodyOf(String functionName){
        return functions.get(key(functionName));
    }

    @Override
    public List<SVars> argsOf(String functionName) {
        return functionArgs.getOrDefault(key(functionName), List.of());
    }

    @Override
    public String userStringOf(String functionName) {
        return functionUserStrings.getOrDefault(key(functionName), functionName);
    }

    @Override
    public Set<String> allFunctionNames() {
        return new LinkedHashSet<>(functionUserStrings.values());
    }

    @Override
    public String internalNameOf(String userString) {
        return userStringToName.getOrDefault(userString, userString);
    }
}
