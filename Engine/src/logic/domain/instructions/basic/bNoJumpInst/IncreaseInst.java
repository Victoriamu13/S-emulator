package logic.domain.instructions.basic.bNoJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

public class IncreaseInst extends AbstractInstruction {

    public IncreaseInst(SVars variable) {
        super(InstructionData.INCREASE,variable);
    }

    public IncreaseInst(SVars variable, SLabel label) {
        super(InstructionData.INCREASE,variable,label);
    }

    @Override
    public SLabel executeOperation(CurrentContext context) {
        long variableValue=context.getVariableValue(getVariable());
        variableValue++;
        context.updateVariable(getVariable(),variableValue);
        return SpecialLabels.EMPTY;
    }
}
