package logic.domain.instructions.synthetic.sNoJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

public class ConstantAssignmentInst extends AbstractInstruction {
    private final long constantValue;

    public ConstantAssignmentInst(SVars targetVar, long constantValue) {
        this(targetVar,constantValue,SpecialLabels.EMPTY);
    }

    public ConstantAssignmentInst(SVars targetVar, long constantValue, SLabel label) {
        super(InstructionData.CONSTANT_ASSIGNMENT,targetVar,label);
        this.constantValue = Math.max(0,constantValue);
    }

    @Override
    public SLabel executeOperation(CurrentContext context){
        SVars targetVar=getVariable();
        long targetVal=0;
        long constantVal=this.constantValue;

        while(constantVal!=0){
            targetVal++;
            constantVal--;
        }
        context.updateVariable(targetVar, targetVal);
        return SpecialLabels.EMPTY;
    }

    public long getConstantValue() {return constantValue;}
}
