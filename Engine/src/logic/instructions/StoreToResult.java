package logic.instructions;

import logic.execution.CurrentContext;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

public class StoreToResult extends AbstractInstruction {

    public StoreToResult(SVars variable) {
        super(InstructionData.NO_OP,variable);
    }

    public StoreToResult(SVars variable, SLabel label) {
        super(InstructionData.NO_OP,variable,label);
    }

    @Override
    public SLabel executeOperarion(CurrentContext context){
        long val = context.getVariableValue(getVariable());
        context.updateVariable(SVars.RESULT, val);
        return SpecialLabels.EMPTY;
    }
}
