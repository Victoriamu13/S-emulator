package logic.domain.instructions.synthetic.sJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

import java.util.Map;

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
        return (context.getVariableValue(getVariable()) == constantVal) ? jeLabel : SpecialLabels.EMPTY;
    }

    public long getConstantValue() { return constantVal; }

    @Override
    public SLabel getTargetLabel() { return jeLabel; }

    @Override
    public SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap) {
        SVars newVar = varMap.getOrDefault(getVariable(), getVariable());
        SLabel newLabel = labelMap.getOrDefault(getLabel(), getLabel());
        SLabel newTarget = labelMap.getOrDefault(getTargetLabel(), getTargetLabel());
        return new JumpEqualConstantInst(newVar, newTarget, constantVal, newLabel);
    }
}
