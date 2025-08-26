package logic.instructions.basic.bNoJumpInst;

import logic.execution.context.CurrentContext;
import logic.instructions.AbstractInstruction;
import logic.instructions.data.InstructionData;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

public class NeutralInst extends AbstractInstruction {

    public NeutralInst(SVars variable) {
        super(InstructionData.NEUTRAL,variable);
    }

    public NeutralInst(SVars variable, SLabel label) {
        super(InstructionData.NEUTRAL,variable,label);
    }

    @Override
    public SLabel executeOperation(CurrentContext context){
        return SpecialLabels.EMPTY;
    }
}
