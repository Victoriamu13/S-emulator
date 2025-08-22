package logic.instructions.basic;

import logic.execution.CurrentContext;
import logic.instructions.AbstractInstruction;
import logic.instructions.InstructionData;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

public class IncreaseInst extends AbstractInstruction {

    public IncreaseInst(SVars variable) {
        super(logic.instructions.InstructionData.INCREASE,variable);
    }

    public IncreaseInst(SVars variable, SLabel label) {
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
