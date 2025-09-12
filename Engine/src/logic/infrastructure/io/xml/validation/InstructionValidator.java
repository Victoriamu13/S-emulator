package logic.infrastructure.io.xml.validation;

import logic.domain.instructions.data.ArgumentData;
import logic.domain.instructions.data.InstructionData;
import logic.infrastructure.io.xml.dto.RawInstructions;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static logic.infrastructure.io.xml.validation.ValidationUtils.*;

final class InstructionValidator {
    private final Set<String> definedLabelsUpper;

    InstructionValidator(Set<String> definedLabelsUpper) {
        this.definedLabelsUpper = definedLabelsUpper;
    }

    void validateInstruction(RawInstructions r, List<String> errors) {
        // type
        String type = makeValidString(r.typeAttr());
        if (type.isEmpty()) {
            errors.add(msg(r, "Missing 'type' attribute."));
        } else if (!type.equalsIgnoreCase("basic") && !type.equalsIgnoreCase("synthetic")) {
            errors.add(msg(r, "Invalid type '" + r.typeAttr() + "'. Expected 'basic' or 'synthetic'."));
        }

        // instruction name
        String name = makeValidString(r.name());
        InstructionData op = tryInstruction(name);
        if (op == null) {
            errors.add(msg(r, "Unsupported instruction name '" + r.name() + "'."));
        }

        // variable
        String var = makeValidString(r.varText());
        if (!isValidVariable(var)) {
            errors.add(msg(r, "Invalid S-Variable '" + r.varText() + "' (expected y, x<number>, or z<number>)."));
        }

        // arguments (if any)
        Map<String, String> args = (r.args() == null) ? Map.of() : r.args();
        boolean hasLabelTarget = false;

        for (Map.Entry<String, String> e : args.entrySet()) {
            String argNameRaw = e.getKey();
            String argValRaw = e.getValue();
            ArgumentData arg = ArgumentData.fromString(argNameRaw);

            if (arg == null) {
                errors.add(msg(r, "Unknown argument name '" + argNameRaw + "'."));
                continue;
            }

            switch (arg) {
                case ASSIGNED_VARIABLE, VARIABLE_NAME -> {
                    if (!isValidVariable(argValRaw)) {
                        errors.add(msg(r, "Invalid variable value for '" + arg.name() + "': '" + argValRaw + "'."));
                    }
                }
                case JNZ_LABEL, GOTO_LABEL, JZ_LABEL, JE_CONSTANT_LABEL, JE_VARIABLE_LABEL -> {
                    String val = makeValidString(argValRaw);
                    if (val.isEmpty()) {
                        errors.add(msg(r, "Missing label value for '" + arg.name() + "'."));
                    } else if (val.equalsIgnoreCase("EXIT")) {
                        hasLabelTarget = true;
                    } else if (!isLabelInRange(val)) {
                        errors.add(msg(r, "Invalid label value for '" + arg.name() + "': '" + argValRaw + "' (must be EXIT or L1..L99)."));
                    } else {
                        String key = val.toUpperCase(Locale.ROOT);
                        if (!definedLabelsUpper.contains(key)) {
                            errors.add(msg(r, "Reference to undefined label '" + argValRaw + "' via '" + arg.name() + "'."));
                        } else {
                            hasLabelTarget = true;
                        }
                    }
                }
            }
        }

        // Specific rule for JUMP_NOT_ZERO
        if (op == InstructionData.JUMP_NOT_ZERO && !hasLabelTarget) {
            errors.add(msg(r, "JUMP_NOT_ZERO requires a label target argument."));
        }
    }
}
