package logic.instructions.synthetic.sJumpInst;

import logic.execution.CurrentContext;
import logic.instructions.AbstractInstruction;
import logic.instructions.InstructionData;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

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
