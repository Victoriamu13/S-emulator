package logic.domain.instructions.basic.bJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

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
        long variableValue=context.getVariableValue(getVariable());
        if(variableValue!=0){
              return jnzLabel;
        }
        return SpecialLabels.EMPTY;
    }

    @Override
    public SLabel getTargetLabel() {return jnzLabel;}
}
