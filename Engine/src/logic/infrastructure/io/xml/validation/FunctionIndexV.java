package logic.infrastructure.io.xml.validation;

import logic.infrastructure.io.xml.dto.RawFunction;
import logic.infrastructure.io.xml.dto.RawInstructions;
import logic.infrastructure.io.xml.parser.composition.*;

import java.util.*;

import static logic.infrastructure.io.xml.validation.ValidationUtils.*;

public class FunctionIndexV {

    // Map from uppercase function name → arity (numer of input parameters)
    private final Map<String, Integer> funcNameToArity = new LinkedHashMap<>();
    private final List<String> errors = new ArrayList<>();

    // ==== build an index of functions ====
    static FunctionIndexV build(List<RawFunction> functions) {
        FunctionIndexV idx = new FunctionIndexV();
        if (functions == null) return idx;

        Set<String> seen = new HashSet<>();
        for (RawFunction rf : functions) {
            String name = (rf.name() == null) ? "" : rf.name().trim();
            if (name.isEmpty()) {
                idx.errors.add("Function with empty name is not allowed.");
                continue;
            }

            String key = name.toUpperCase(Locale.ROOT);
            if (!seen.add(key)) {
                idx.errors.add("Function '" + name + "' defined more than once.");
                continue;
            }

            int arity = computeArity(rf.body());
            idx.funcNameToArity.put(key, arity);
        }
        return idx;
    }

    boolean exists(String name) {
        if (name == null) return false;
        return funcNameToArity.containsKey(name.trim().toUpperCase(Locale.ROOT));
    }

    int arityOf(String name) {
        return funcNameToArity.get(name.trim().toUpperCase(Locale.ROOT));
    }

    List<String> errors() { return errors; }


    // ==== Calculate arity by scanning function body for xN variables ====
    private static int computeArity(List<RawInstructions> body) {
        if (body == null) return 0;

        Set<Integer> inputs = new java.util.HashSet<>();

        for (RawInstructions r : body) {
            String v = safeString(r.varText());
            if (isValidInputVariable(v)) inputs.add(parseXIndex(v));

            var args = r.args();
            if (args != null) {
                String assigned = getArgIgnoreCase(args, "assignedVariable");
                if (isValidInputVariable(assigned)) inputs.add(parseXIndex(assigned));

                String varName = getArgIgnoreCase(args, "variableName");
                if (isValidInputVariable(varName)) inputs.add(parseXIndex(varName));

                String fnArgs = getArgIgnoreCase(args, "functionArguments");
                if (!fnArgs.isEmpty()) {
                    CompositionParseResult parsed = CompositionParser.parseTopLevel(fnArgs);
                    if (parsed.isOk()) collectVarsFromArgs(parsed.args(), inputs);
                }
            }
        }
            return inputs.size();
    }


    // ==== Collect xN variables recursively from ComposeArguments ====
        private static void collectVarsFromArgs (List<ComposeArgument>args, Set<Integer>inputs){
            for (ComposeArgument a : args) {
                if (a instanceof VarArgument v) {
                    String name = v.getName();
                    if (isValidInputVariable(name)) {
                        inputs.add(parseXIndex(name));
                    }
                } else if (a instanceof FuncCallArgument f) {
                    collectVarsFromArgs(f.getArguments(), inputs);
                }
            }
        }

        public Set<String> functionNames(){
             return funcNameToArity.keySet();
        }
}
