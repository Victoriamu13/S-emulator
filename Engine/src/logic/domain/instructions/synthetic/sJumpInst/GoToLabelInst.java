package logic.domain.instructions.synthetic.sJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

import java.util.Map;

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
        return gotoLabel;
    }

    @Override
    public SLabel getTargetLabel() { return gotoLabel; }

    @Override
    public SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap) {
        SVars newVar     = varMap.getOrDefault(getVariable(), getVariable());
        SLabel newLabel   = labelMap.getOrDefault(getLabel(), getLabel());
        SLabel newTarget  = labelMap.getOrDefault(getTargetLabel(), getTargetLabel());
        return new GoToLabelInst(newVar, newTarget, newLabel);
    }

}
