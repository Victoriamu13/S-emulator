package logic.domain.instructions.synthetic.sJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

public class GoToLabelInst extends AbstractInstruction {
    private final SLabel gotoLabel;

    public GoToLabelInst(SVars dummyVariable, SLabel gotoLabel) {
        this(dummyVariable,gotoLabel, SpecialLabels.EMPTY);
    }

    public GoToLabelInst(SVars dummyVariable,SLabel gotoLabel,SLabel label) {
        super(InstructionData.GOTO_LABEL,dummyVariable,label);
        this.gotoLabel = gotoLabel;
    }

    @Override
    public SLabel executeOperation(CurrentContext context){
        SVars workVar=getVariable();
        if(workVar!=null){
            long val= context.getVariableValue(workVar);
            context.updateVariable(workVar,val+1);
        }
        return gotoLabel;
    }

    @Override
    public SLabel getTargetLabel() { return gotoLabel; }

}
