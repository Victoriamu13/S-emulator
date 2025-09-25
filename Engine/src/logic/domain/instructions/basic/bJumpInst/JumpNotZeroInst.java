package logic.domain.instructions.basic.bJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

import java.util.Map;

public class JumpNotZeroInst extends AbstractInstruction {

    private final SLabel jnzLabel;

    public JumpNotZeroInst(SVars variable, SLabel jnzLabel) {
        this(variable,jnzLabel,SpecialLabels.EMPTY);
    }

    public JumpNotZeroInst(SVars variable, SLabel jnzLabel, SLabel label) {
        super(InstructionData.JUMP_NOT_ZERO,variable,label);
        this.jnzLabel=jnzLabel;
    }

    @Override
    public SLabel executeOperation(CurrentContext context){
        return context.getVariableValue(getVariable()) != 0 ? jnzLabel : SpecialLabels.EMPTY;
    }

    @Override
    public SLabel getTargetLabel() {return jnzLabel;}

    @Override
    public SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap) {
        SVars newVar     = varMap.getOrDefault(getVariable(), getVariable());
        SLabel newLabel   = labelMap.getOrDefault(getLabel(), getLabel());
        SLabel newTarget  = labelMap.getOrDefault(getTargetLabel(), getTargetLabel());
        return new JumpNotZeroInst(newVar, newTarget, newLabel);
    }
}
