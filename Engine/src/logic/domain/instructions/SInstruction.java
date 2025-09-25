package logic.domain.instructions;

import logic.domain.execution.context.CurrentContext;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

import java.util.Map;

public interface SInstruction {
    String getName();
    int cycles();
    SLabel getLabel();
    SVars getVariable();
    SLabel executeOperation(CurrentContext context);
    default SLabel getTargetLabel() {
        return SpecialLabels.EMPTY;
    }
    SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap);
}
