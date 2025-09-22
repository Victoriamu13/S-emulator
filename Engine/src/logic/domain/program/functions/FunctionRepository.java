package logic.domain.program.functions;

import logic.domain.instructions.SInstruction;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FunctionRepository implements FunctionLookup {
    private final Map<String, List<SInstruction>> functions=new HashMap<>();

    private static String key(String name) {
        return name == null ? "" : name.trim().toUpperCase(Locale.ROOT);
    }

    public void register(String name,List<SInstruction> body){
        functions.put(name,body);
    }

    @Override
    public List<SInstruction> bodyOf(String functionName){
        return functions.get(key(functionName));
    }

    @Override
    public boolean exists(String functionName) {
        return functions.containsKey(key(functionName));
    }
}
