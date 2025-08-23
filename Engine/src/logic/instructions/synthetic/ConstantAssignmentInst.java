package logic.instructions.synthetic;

import logic.execution.CurrentContext;
import logic.instructions.AbstractInstruction;
import logic.instructions.InstructionData;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

public class ConstantAssignmentInst extends AbstractInstruction {
    private final long constantValue;

    public ConstantAssignmentInst(SVars targetVar, long constantValue) {
        this(targetVar,constantValue,SpecialLabels.EMPTY);
    }

    public ConstantAssignmentInst(SVars targetVar, long constantValue, SLabel label) {
        super(InstructionData.CONSTANT_ASSIGNMENT,targetVar,label);
        this.constantValue = constantValue;
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
        return SpecialLabels.EMPTY;
    }

    public long getConstantValue() {return constantValue;}
}
