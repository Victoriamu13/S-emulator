package logic.infrastructure.io.xml.validation;

import logic.infrastructure.io.xml.dto.RawFunction;
import logic.infrastructure.io.xml.dto.RawInstructions;

import java.util.*;

import static logic.infrastructure.io.xml.validation.ValidationUtils.*;

public class FunctionIndexV {

    //Arity = number of parameters received in function
    private final Map<String, Integer> funcNameToArity = new LinkedHashMap<>();
    private final List<String> errors = new ArrayList<>();

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


    private static int computeArity(List<RawInstructions> body) {
        if (body == null) return 0;

        Set<Integer> xs = new java.util.HashSet<>();

        for (RawInstructions r : body) {
            String v = safeString(r.varText());
            if (isValidInputVariable(v)) {
                xs.add(parseXIndex(v));
            }

            var args = r.args();
            if (args != null) {
                String assigned = getArgIgnoreCase(args, "assignedVariable");
                if (isValidInputVariable(assigned)) {
                    xs.add(parseXIndex(assigned));
                }

                String varName = getArgIgnoreCase(args, "variableName");
                if (isValidInputVariable(varName)) {
                    xs.add(parseXIndex(varName));
                }
            }
        }
        return xs.size();
    }
}
