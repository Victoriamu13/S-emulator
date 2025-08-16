package logic.instructions;

import logic.execution.CurrentContext;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

public class DecreaseInstruction extends AbstractInstruction {

    public DecreaseInstruction(SVars variable) {
        super(InstructionData.DECREASE,variable);
    }

    public DecreaseInstruction(SVars variable,SLabel label) {
        super(InstructionData.DECREASE,variable,label);
    }

    @Override
    public SLabel executeOperarion(CurrentContext context) {
        long variableValue=context.getVariableValue(getVariable());
        variableValue = Math.max(0, variableValue - 1);
        context.updateVariable(getVariable(),variableValue);
        return SpecialLabels.EMPTY;
    }
}
