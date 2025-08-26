package logic.instructions.synthetic.sNoJumpInst;

import logic.execution.context.CurrentContext;
import logic.instructions.AbstractInstruction;
import logic.instructions.data.InstructionData;
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
