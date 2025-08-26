package logic.instructions.synthetic.sNoJumpInst;

import logic.execution.context.CurrentContext;
import logic.instructions.AbstractInstruction;
import logic.instructions.data.InstructionData;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

public class ZeroVariableInst extends AbstractInstruction {

    public ZeroVariableInst(SVars variable) {
        super(InstructionData.ZERO_VARIABLE,variable);
    }

    public ZeroVariableInst(SVars variable, SLabel label) {
        super(InstructionData.ZERO_VARIABLE,variable,label);
    }

    @Override
    public SLabel executeOperation(CurrentContext context){
        long val=context.getVariableValue(getVariable());
        while(val!=0){
            val-=1;
        }
        context.updateVariable(getVariable(),val);
        return SpecialLabels.EMPTY;
    }

}
