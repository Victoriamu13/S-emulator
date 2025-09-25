package logic.domain.instructions.synthetic.sJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

import java.util.Map;

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
        return context.getVariableValue(getVariable()) == 0 ? jzLabel : SpecialLabels.EMPTY;
    }

    @Override
    public SLabel getTargetLabel() { return jzLabel; }

    @Override
    public SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap) {
        SVars newVar     = varMap.getOrDefault(getVariable(), getVariable());
        SLabel newLabel   = labelMap.getOrDefault(getLabel(), getLabel());
        SLabel newTarget  = labelMap.getOrDefault(getTargetLabel(), getTargetLabel());
        return new JumpZeroInst(newVar, newTarget, newLabel);
    }
}
