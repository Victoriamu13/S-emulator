package logic.infrastructure.io.xml.validation;

import logic.infrastructure.io.xml.dto.RawFunction;
import logic.infrastructure.io.xml.dto.RawInstructions;
import java.util.*;

import static logic.infrastructure.io.xml.validation.ValidationUtils.*;

public final class XmlProgramValidator {

    public ValidateResult validate(String programName, List<RawInstructions> raw, List<RawFunction> functions) {
        List<String> errors = new ArrayList<>();

        // 1) Program name
        if (isBlank(programName)) {
            errors.add("Missing 'name' attribute in S-Program element.");
        }

        // 2) Collect labels & detect duplicates
        LabelIndexV labelIndex = LabelIndexV.build(raw);
        errors.addAll(labelIndex.errors());
        Set<String> definedLabelsUpper = labelIndex.definedLabelsUpper();

        // 3) Per-instruction validations
        InstructionValidator iv = new InstructionValidator(definedLabelsUpper);
        for (RawInstructions r : raw) {
            iv.validateInstruction(r, errors);
        }

        // 4) Build function index (name -> arity) + its errors
        FunctionIndexV fIndex = FunctionIndexV.build(functions);
        errors.addAll(fIndex.errors());

        // 5) QUOTE and JUMP_EQUAL_FUNCTION validations (top-level)
        FunctionValidator.validateQuoteCalls(raw, fIndex, errors);
        FunctionValidator.validateJumpEqualFunctionCalls(raw, fIndex, errors);

        // 6) Functions content validations (labels, instructions, QUOTE inside functions)
        FunctionValidator.validateFunctionBodies(functions, fIndex, errors);

        return new ValidateResult(raw, errors);
    }
}


