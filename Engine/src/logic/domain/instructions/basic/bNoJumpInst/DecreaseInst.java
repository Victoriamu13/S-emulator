package logic.domain.instructions.basic.bNoJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

public class DecreaseInst extends AbstractInstruction {

    public DecreaseInst(SVars variable) {
        super(InstructionData.DECREASE,variable);
    }

    public DecreaseInst(SVars variable, SLabel label) {
        super(InstructionData.DECREASE,variable,label);
    }

    @Override
    public SLabel executeOperation(CurrentContext context) {
        long variableValue=context.getVariableValue(getVariable());
        variableValue = Math.max(0, variableValue - 1);
        context.updateVariable(getVariable(),variableValue);
        return SpecialLabels.EMPTY;
    }
}
