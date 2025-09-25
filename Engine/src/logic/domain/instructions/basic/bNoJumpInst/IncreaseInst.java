package logic.domain.instructions.basic.bNoJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

import java.util.Map;

public class IncreaseInst extends AbstractInstruction {

    public IncreaseInst(SVars variable) {
        super(InstructionData.INCREASE,variable);
    }

    public IncreaseInst(SVars variable, SLabel label) {
        super(InstructionData.INCREASE,variable,label);
    }

    @Override
    public SLabel executeOperation(CurrentContext context) {
        context.updateVariable(getVariable(), context.getVariableValue(getVariable()) + 1);
        return SpecialLabels.EMPTY;
    }

    @Override
    public SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap) {
        SVars newVar = varMap.getOrDefault(getVariable(), getVariable());
        SLabel newLabel = labelMap.getOrDefault(getLabel(), getLabel());
        return new IncreaseInst(newVar, newLabel);
    }
}
