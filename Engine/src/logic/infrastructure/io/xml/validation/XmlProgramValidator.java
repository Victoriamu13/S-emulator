package logic.infrastructure.io.xml.validation;

import logic.infrastructure.io.xml.dto.RawInstructions;
import java.util.*;

import static logic.infrastructure.io.xml.validation.ValidationUtils.*;

public final class XmlProgramValidator {

    public ValidateResult validate(String programName, List<RawInstructions> raw) {
        List<String> errors = new ArrayList<>();

        // 1) Program name
        if (isBlank(programName)) {
            errors.add("Missing 'name' attribute in S-Program element.");
        }

        // 2) Collect labels & detect duplicates
        LabelIndexV labelIndex = LabelIndexV.build(raw);
        errors.addAll(labelIndex.errors()); // כפילויות / EXIT כתווית שורה / טווח לא חוקי בהגדרה
        Set<String> definedLabelsUpper = labelIndex.definedLabelsUpper();

        // 3) Per-instruction validations
        InstructionValidator iv = new InstructionValidator(definedLabelsUpper);
        for (RawInstructions r : raw) {
            iv.validateInstruction(r, errors);
        }

        return new ValidateResult(raw, errors);
    }
}


