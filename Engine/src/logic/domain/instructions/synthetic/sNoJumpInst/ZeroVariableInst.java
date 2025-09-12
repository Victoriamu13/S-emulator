package logic.domain.instructions.synthetic.sNoJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

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
