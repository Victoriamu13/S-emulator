package logic.instructions.basic;

import logic.execution.CurrentContext;
import logic.instructions.AbstractInstruction;
import logic.instructions.InstructionData;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

public class NeutralInst extends AbstractInstruction {

    public NeutralInst(SVars variable) {
        super(logic.instructions.InstructionData.NEUTRAL,variable);
    }

    public NeutralInst(SVars variable, SLabel label) {
        super(InstructionData.NEUTRAL,variable,label);
    }

    @Override
    public SLabel executeOperarion(CurrentContext context){
        return SpecialLabels.EMPTY;
    }
}
