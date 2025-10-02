package logic.domain.program.functions;

import logic.infrastructure.io.xml.parser.composition.ComposeArgument;
import logic.infrastructure.io.xml.parser.composition.FuncCallArgument;
import logic.infrastructure.io.xml.parser.composition.VarArgument;

import java.util.List;
import java.util.stream.Collectors;

public class  FunctionsUtils {

    // Convert args to string representation
    public static String argsToString(List<ComposeArgument> args) {
        if (args == null || args.isEmpty()) return "";
        return args.stream()
                .map(FunctionsUtils::argToString)
                .collect(Collectors.joining(","));
    }

    // One arg to string
    private static String argToString(ComposeArgument a) {
        if (a instanceof VarArgument v) {
            return v.getName();
        }
        if (a instanceof FuncCallArgument f) {
            String inner = argsToString(f.getArguments());
            return "(" + f.getFunctionName() + (inner.isEmpty() ? "" : "," + inner) + ")";
        }
        return a.toString();
    }
}
