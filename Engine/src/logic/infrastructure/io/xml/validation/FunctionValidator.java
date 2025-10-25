package logic.infrastructure.io.xml.validation;

import logic.infrastructure.io.xml.dto.RawFunction;
import logic.infrastructure.io.xml.dto.RawInstructions;
import logic.infrastructure.io.xml.parser.composition.*;
import logic.system.programs.functions.repository.FunctionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static logic.infrastructure.io.xml.validation.ValidationUtils.*;

public final class FunctionValidator {

    private FunctionValidator() {}

    private static final String ARG_JEF_LABEL = "JEFunctionLabel";
    private static final String ARG_FN_NAME = "functionName";
    private static final String ARG_FN_ARGS = "functionArguments";

    // ==== Validate function bodies ====
    static void validateFunctionBodies(List<RawFunction> functions, FunctionIndexV fIndex, List<String> errors) {
        if (functions == null) return;

        for (RawFunction fn : functions) {
            String fnName = safeString(fn.name());
            List<RawInstructions> body = safeList(fn.body());

            if (body.isEmpty()) { // can be a global body
                continue;
            }

            // validate labels
            LabelIndexV lblIdx = LabelIndexV.build(body);
            addPrefixed(errors, fnName, lblIdx.errors());

            // validate instructions
            InstructionValidator iv = new InstructionValidator(lblIdx.definedLabelsUpper());
            List<String> local = new ArrayList<>();
            for (RawInstructions r : body) iv.validateInstruction(r, local);

            addPrefixed(errors, fnName, local);

            // validate calls inside body
            List<String> callErrors = new ArrayList<>();
            validateQuoteCalls(body, fIndex, callErrors);
            validateJumpEqualFunctionCalls(body, fIndex, callErrors);
            addPrefixed(errors, fnName, callErrors);
        }
    }


    // ==== Validate QUOTE calls ====
    static void validateQuoteCalls(List<RawInstructions> raw, FunctionIndexV fIndex, List<String> errors) {
        if (raw == null) return;

        for (RawInstructions r : raw) {
            String name = safeString(r.name());
            if (!"QUOTE".equalsIgnoreCase(name)) continue;
            validateFunctionCall(r, fIndex, errors,  "QUOTE requires 'functionName' argument.");
        }
    }

    // ==== Validate JUMP_EQUAL_FUNCTION calls ====
    static void validateJumpEqualFunctionCalls(List<RawInstructions> raw, FunctionIndexV fIndex, List<String> errors) {
        if (raw == null) return;

        for (RawInstructions r : raw) {
            String name = safeString(r.name());
            if (!"JUMP_EQUAL_FUNCTION".equalsIgnoreCase(name)) continue;
            validateFunctionCall(r, fIndex, errors,
                    "JUMP_EQUAL_FUNCTION requires 'functionName' argument.");
        }
    }

    // ==== function call validator ====
    private static void validateFunctionCall(RawInstructions r, FunctionIndexV fIndex, List<String> errors,
                                             String missingFnMessage) {
        Map<String, String> args = safeArgs(r.args());
        String fnName = safeString(args.get(ARG_FN_NAME));
        String fnArgs = safeString(args.get(ARG_FN_ARGS));

        if (fnName.isEmpty()) {
            errors.add(msg(r, missingFnMessage));
            return;
        }

        boolean existsLocal = fIndex.exists(fnName);
        boolean existsGlobal = FunctionRepository.functionExists(fnName);

        if (!existsLocal && !existsGlobal) {
                errors.add(msg(r, "Function '" + fnName + "' is not defined in <S-Functions> or in Function Repository."));
                return;
        }

        int expected = fIndex.arityOf(fnName);
        CompositionParseResult parsed = CompositionParser.parseTopLevel(fnArgs);
        if (!parsed.isOk()) {
            addParseErrors(errors, r, parsed);
            return;
        }

        int provided = parsed.args().size();
        if (expected >= 0 && expected != provided) {
            errors.add(msg(r, "Function '" + fnName + "' expects " + expected +
                    " argument(s) but got " + provided + "."));
        }
        for (ComposeArgument a : parsed.args()) validateArgsRecursively(r, a, fIndex, errors);
    }

    // ==== Validate arguments recursively (VarArgument or FuncCallArgument) ====
    private static void validateArgsRecursively(
            RawInstructions r, ComposeArgument arg, FunctionIndexV fIndex, List<String> errors) {

        if (arg instanceof VarArgument v) {
            String token = v.getName();
            if (!isValidVariable(token)) {
                errors.add(msg(r, "Invalid variable in functionArguments: '" + token + "'."));
            }
            return;
        }
        if (arg instanceof FuncCallArgument f) {
            String fn = f.getFunctionName();

            boolean existsLocal = fIndex.exists(fn);
            boolean existsGlobal = FunctionRepository.functionExists(fn);

            if (!existsLocal && !existsGlobal) {
                errors.add(msg(r, "Function '" + fn + "' used in functionArguments is not defined in <S-Functions>."));
            } else {
                int expected = fIndex.arityOf(fn);
                int provided = f.getArguments().size();

                if (expected >= 0 && expected != provided) {
                    errors.add(msg(r, "Function '" + fn + "' expects " + expected +
                            " argument(s) but got " + provided + "."));
                }
            }
            for (ComposeArgument child : f.getArguments()) validateArgsRecursively(r, child, fIndex, errors);
        }
    }
}






