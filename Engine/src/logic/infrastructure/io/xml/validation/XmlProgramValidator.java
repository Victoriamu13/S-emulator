package logic.infrastructure.io.xml.validation;

import logic.infrastructure.io.xml.dto.RawFunction;
import logic.system.programs.repository.ProgramRepository;
import logic.infrastructure.io.xml.dto.RawInstructions;
import java.util.*;

import static logic.infrastructure.io.xml.validation.ValidationUtils.*;

public final class XmlProgramValidator {

    public ValidateResult validateStructure(String programName, List<RawInstructions> raw,
                                   List<RawFunction> functions) {
        List<String> errors = new ArrayList<>();

        // 1) Program name
        if (isBlank(programName)) {
            errors.add("Missing 'name' attribute in S-Program element.");
        }else if (ProgramRepository.programExists(programName)) {
            errors.add("Program '" + programName + "' already exists in the system.");
        }

        // 2) Empty program
        if (raw == null || raw.isEmpty()) {
            errors.add("Program '" + programName + "' is empty — no instructions found.");
        }


        // 3) Collect labels & detect duplicates
        LabelIndexV labelIndex = LabelIndexV.build(raw);
        errors.addAll(labelIndex.errors());
        Set<String> definedLabelsUpper = labelIndex.definedLabelsUpper();

        // 4) Per-instruction validations
        InstructionValidator iv = new InstructionValidator(definedLabelsUpper);
        for (RawInstructions r : raw) {
            iv.validateInstruction(r, errors);
        }

        // 5) Build function index (name -> arity) + its errors
        FunctionIndexV fIndex = FunctionIndexV.build(functions);
        errors.addAll(fIndex.errors());

        // 6) QUOTE and JUMP_EQUAL_FUNCTION validations (top-level)
        FunctionValidator.validateQuoteCalls(raw, fIndex, errors);
        FunctionValidator.validateJumpEqualFunctionCalls(raw, fIndex, errors);

        // 7) Functions content validations (labels, instructions, QUOTE inside functions)
        FunctionValidator.validateFunctionBodies(functions, fIndex, errors);


        return new ValidateResult(raw, errors);
    }



}


