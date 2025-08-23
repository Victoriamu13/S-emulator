package logic.instructions.basic;

import logic.execution.CurrentContext;
import logic.instructions.AbstractInstruction;
import logic.instructions.InstructionData;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

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
