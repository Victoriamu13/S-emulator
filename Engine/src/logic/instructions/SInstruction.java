package logic.instructions;

import logic.execution.CurrentContext;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

public interface SInstruction {
    String getName();
    int cycles();
    SLabel getLabel();
    SVars getVariable();
    SLabel executeOperation(CurrentContext context);

    default SLabel getTargetLabel() {
        return SpecialLabels.EMPTY;
    }
}
