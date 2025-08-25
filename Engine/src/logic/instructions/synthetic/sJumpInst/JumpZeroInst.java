package logic.instructions.synthetic.sJumpInst;

import logic.execution.CurrentContext;
import logic.instructions.AbstractInstruction;
import logic.instructions.InstructionData;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

public class JumpZeroInst extends AbstractInstruction {
    private final SLabel jzLabel;

    public JumpZeroInst(SVars variable,SLabel jzLabel) {
        this(variable,jzLabel, SpecialLabels.EMPTY);
    }

    public JumpZeroInst(SVars variable,SLabel jzLabel,SLabel label) {
        super(InstructionData.JUMP_ZERO, variable, label);
        this.jzLabel = jzLabel;
    }

    @Override
    public SLabel executeOperation(CurrentContext context) {
        long val= context.getVariableValue(getVariable());
        return (val==0) ? jzLabel : SpecialLabels.EMPTY;
    }

    @Override
    public SLabel getTargetLabel() { return jzLabel; }
}
