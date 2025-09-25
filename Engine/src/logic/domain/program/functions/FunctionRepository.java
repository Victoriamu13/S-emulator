package logic.domain.program.functions;

import logic.domain.instructions.SInstruction;
import logic.domain.variable.SVars;

import java.util.*;

public class FunctionRepository implements FunctionLookup {
    private final Map<String, List<SInstruction>> functions = new HashMap<>();
    private final Map<String, List<SVars>> functionArgs = new HashMap<>();

    private static String key(String name) {
        return name == null ? "" : name.trim().toUpperCase(Locale.ROOT);
    }

    public void register(String name, List<SVars> args, List<SInstruction> body){
        functions.put(key(name), body);
        functionArgs.put(key(name), args);
    }

    @Override
    public List<SInstruction> bodyOf(String functionName){
        return functions.get(key(functionName));
    }

    @Override
    public List<SVars> argsOf(String functionName) {
        return functionArgs.getOrDefault(key(functionName), List.of());
    }

    public Set<String> names() {
        return Collections.unmodifiableSet(functionArgs.keySet());
    }
}
