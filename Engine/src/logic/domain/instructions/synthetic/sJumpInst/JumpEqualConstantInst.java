package logic.domain.instructions.synthetic.sJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

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

    public long getConstantValue() { return constantVal; }

    @Override
    public SLabel getTargetLabel() { return jeLabel; }
}
