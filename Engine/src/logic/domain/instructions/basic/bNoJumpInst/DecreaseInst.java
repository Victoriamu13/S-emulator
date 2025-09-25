package logic.domain.instructions.basic.bNoJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

import java.util.Map;

public class DecreaseInst extends AbstractInstruction {

    public DecreaseInst(SVars variable) {
        super(InstructionData.DECREASE,variable);
    }

    public DecreaseInst(SVars variable, SLabel label) {
        super(InstructionData.DECREASE,variable,label);
    }

    @Override
    public SLabel executeOperation(CurrentContext context) {
        long val = context.getVariableValue(getVariable());
        context.updateVariable(getVariable(), Math.max(0, val - 1));
        return SpecialLabels.EMPTY;
    }

    @Override
    public SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap) {
        SVars newVar = varMap.getOrDefault(getVariable(), getVariable());
        SLabel newLabel = labelMap.getOrDefault(getLabel(), getLabel());
        return new DecreaseInst(newVar, newLabel);
    }

}
