package logic.instructions.basic.bJumpInst;

import logic.execution.context.CurrentContext;
import logic.instructions.AbstractInstruction;
import logic.instructions.data.InstructionData;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

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
