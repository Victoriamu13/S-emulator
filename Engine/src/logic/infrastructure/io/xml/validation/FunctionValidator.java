package logic.infrastructure.io.xml.validation;

import logic.infrastructure.io.xml.dto.RawFunction;
import logic.infrastructure.io.xml.dto.RawInstructions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static logic.infrastructure.io.xml.validation.ValidationUtils.*;

public final class FunctionValidator {

    private FunctionValidator() {}

    static void validateQuoteCalls(List<RawInstructions> raw, FunctionIndexV fIndex, List<String> errors) {
        if (raw == null) return;

        for (RawInstructions r : raw) {
            String name = safeString(r.name());
            if (!"QUOTE".equalsIgnoreCase(name)) continue;

            Map<String, String> args = (r.args() == null) ? Map.of() : r.args();
            String fnName = safeString(args.get("functionName"));
            String fnArgs = safeString(args.get("functionArguments"));

            if (fnName.isEmpty()) {
                errors.add(msg(r, "QUOTE requires 'functionName' argument."));
                continue;
            }
            if (!fIndex.exists(fnName)) {
                errors.add(msg(r, "Function '" + fnName + "' is not defined in <S-Functions>."));
                continue;
            }

            int provided = countParenGroups(fnArgs);
            int expected = fIndex.arityOf(fnName);

            if (provided < 0) {
                errors.add(msg(r, "Unbalanced parentheses in 'functionArguments'."));
                continue;
            }

            if (expected >= 0 && expected != provided) {
                errors.add(msg(r, "Function '" + fnName + "' expects " + expected +
                        " argument(s) but got " + provided + "."));
            }
        }
    }

    public static void validateFunctionBodies(List<RawFunction> functions, FunctionIndexV fIndex, List<String> errors) {
        if (functions == null) return;

        for (RawFunction fn : functions) {
            String fnName = safeString(fn.name());
            List<RawInstructions> body = safeList(fn.body());

            // a) Label duplicates / range inside the function
            LabelIndexV flabels = LabelIndexV.build(body);
            addPrefixed(flabels.errors(),
                    "Function '" + fnName + "': ",
                    errors);

            // b) Instruction validations inside the function (using its own labels)
            InstructionValidator fiv = new InstructionValidator(flabels.definedLabelsUpper());
            List<String> local = new ArrayList<>();
            for (RawInstructions r : body) {
                fiv.validateInstruction(r, local);
            }
            addPrefixed(local, "Function '" + fnName + "': ", errors);

            // c) QUOTE validations inside the function
            List<String> localQuote = new ArrayList<>();
            FunctionValidator.validateQuoteCalls(body, fIndex, localQuote);
            addPrefixed(localQuote, "Function '" + fnName + "': ", errors);
        }
    }
}






