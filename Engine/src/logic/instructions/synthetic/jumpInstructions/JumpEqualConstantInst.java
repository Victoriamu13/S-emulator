package logic.instructions.synthetic.jumpInstructions;

import logic.execution.CurrentContext;
import logic.instructions.AbstractInstruction;
import logic.instructions.InstructionData;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

public class JumpEqualConstantInst extends AbstractInstruction {
    private final long constantVal;
    private final SLabel jeLabel;

    public JumpEqualConstantInst(SVars variable,SLabel jeLabel,long constantVal) {
        this(variable,jeLabel,constantVal, SpecialLabels.EMPTY);
    }

    public JumpEqualConstantInst(SVars variable,SLabel jeLabel,long constantVal,SLabel label) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, variable, label);
        this.constantVal = Math.max(0, constantVal);
        this.jeLabel = jeLabel;
    }

    @Override
    public SLabel executeOperation(CurrentContext context) {
        long val=context.getVariableValue(getVariable());
        long constantVal=Math.max(0, this.constantVal);

        for(int i=0;i<constantVal;i++) {
            if(val==0){
                return SpecialLabels.EMPTY;
            }
            val--;
        }
        return (val==0) ? this.jeLabel : SpecialLabels.EMPTY;
    }
}
