package logic.instructions;

import logic.execution.CurrentContext;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

public class JumpNotZeroInstruction extends AbstractInstruction{

    private final SLabel jnzLabel;

    public JumpNotZeroInstruction(SVars variable, SLabel jnzLabel) {
        this(variable,jnzLabel,SpecialLabels.EMPTY);
    }

    public JumpNotZeroInstruction(SVars variable,SLabel jnzLabel,SLabel label) {
        super(InstructionData.JUMP_NOT_ZERO,variable,label);
        this.jnzLabel=jnzLabel;
    }

    @Override
    public SLabel executeOperarion(CurrentContext context){
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
