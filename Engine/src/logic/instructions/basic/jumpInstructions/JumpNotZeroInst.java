package logic.instructions.basic.jumpInstructions;

import logic.execution.CurrentContext;
import logic.instructions.AbstractInstruction;
import logic.instructions.InstructionData;
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

    public SLabel getJumpLabel() {
        return jnzLabel;
    }
}
