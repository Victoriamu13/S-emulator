package logic.instructions;

import logic.execution.CurrentContext;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

public class IncreaseInstruction extends AbstractInstruction {

    public IncreaseInstruction(SVars variable) {
        super(InstructionData.INCREASE,variable);
    }

    public IncreaseInstruction(SVars variable,SLabel label) {
        super(InstructionData.INCREASE,variable,label);
    }

    @Override
    public SLabel executeOperarion(CurrentContext context) {
        long variableValue=context.getVariableValue(getVariable());
        variableValue++;
        context.updateVariable(getVariable(),variableValue);
        return SpecialLabels.EMPTY;
    }
}
