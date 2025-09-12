package logic.domain.instructions.synthetic.sJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

public class JumpEqualVariableInst extends AbstractInstruction {
    private final SVars otherVariable;
    private final SLabel jeLabel;

    public JumpEqualVariableInst(SVars variable,SVars otherVariable, SLabel jeLabel) {
        this(variable,otherVariable,jeLabel, SpecialLabels.EMPTY);
    }

    public JumpEqualVariableInst(SVars variable,SVars otherVariable,SLabel jeLabel,SLabel label) {
        super(InstructionData.JUMP_EQUAL_VARIABLE,variable,label);
        this.otherVariable = otherVariable;
        this.jeLabel = jeLabel;
    }

    @Override
    public SLabel executeOperation(CurrentContext context){
        long val= context.getVariableValue(getVariable());
        long otherVal= context.getVariableValue(this.otherVariable);

        while(otherVal>0 && val>0){
            otherVal--;
            val--;
        }
        return (val==otherVal) ? this.jeLabel : SpecialLabels.EMPTY;
    }

    public SVars getOtherVar() { return otherVariable; }

    @Override
    public SLabel getTargetLabel() { return jeLabel; }
}
