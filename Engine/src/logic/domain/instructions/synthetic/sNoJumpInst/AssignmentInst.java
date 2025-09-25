package logic.domain.instructions.synthetic.sNoJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;
import logic.domain.variable.SVarsImpl;
import logic.domain.variable.SVarsType;

import java.util.Map;

public class AssignmentInst extends AbstractInstruction {
    private final SVars sourceVar;

    public AssignmentInst(SVars targetVar, SVars sourceVar) {
        this(targetVar,sourceVar, SpecialLabels.EMPTY);
    }

    public AssignmentInst(SVars targetVar, SVars sourceVar, SLabel label) {
        super(InstructionData.ASSIGNMENT,targetVar,label);
        this.sourceVar = sourceVar;
    }

    @Override
    public SLabel executeOperation(CurrentContext context){
        long value = context.getVariableValue(sourceVar);
        context.updateVariable(getVariable(), value);
        return SpecialLabels.EMPTY;
    }

    public SVars getSourceVar() { return sourceVar; }

    @Override
    public SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap) {
        SVars newTarget = varMap.getOrDefault(getVariable(), getVariable());
        SVars newSource = varMap.getOrDefault(this.sourceVar, this.sourceVar);
        SLabel newLabel = labelMap.getOrDefault(getLabel(), getLabel());
        return new AssignmentInst(newTarget, newSource, newLabel);
    }
}
