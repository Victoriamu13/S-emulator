package logic.domain.instructions.synthetic.sJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

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
