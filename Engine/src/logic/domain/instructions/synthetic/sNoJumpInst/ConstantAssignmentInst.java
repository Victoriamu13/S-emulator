package logic.domain.instructions.synthetic.sNoJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

import java.util.Map;

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
        context.updateVariable(getVariable(), constantValue);
        return SpecialLabels.EMPTY;
    }

    public long getConstantValue() {return constantValue;}

    @Override
    public SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap) {
        SVars newTarget = varMap.getOrDefault(getVariable(), getVariable());
        SLabel newLabel  = labelMap.getOrDefault(getLabel(), getLabel());
        return new ConstantAssignmentInst(newTarget, constantValue, newLabel);
    }
}
