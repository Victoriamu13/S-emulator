package logic.instructions;

import logic.execution.CurrentContext;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

public class NoOpInstruction extends AbstractInstruction {

    public NoOpInstruction(SVars variable) {
        super(InstructionData.NO_OP,variable);
    }

    public NoOpInstruction(SVars variable,SLabel label) {
        super(InstructionData.NO_OP,variable,label);
    }

    @Override
    public SLabel executeOperarion(CurrentContext context){
        return SpecialLabels.EMPTY;
    }
}
