package logic.domain.instructions.synthetic.sJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

import java.util.Map;

public class JumpEqualVariableInst extends AbstractInstruction {
    private final SVars otherVariable;
    private final SLabel jeLabel;

    public JumpEqualVariableInst(SVars variable,SVars otherVariable, SLabel jeLabel) {
        this(variable,otherVariable,jeLabel, SpecialLabels.EMPTY);
    }

    public JumpEqualVariableInst(SVars variable,SVars otherVariable,SLabel jeLabel,SLabel label) {
        super(InstructionData.JUMP_EQUAL_VARIABLE,variable,label);
        this.otherVariable = otherVariable;
        this.jeLabel = jeLabel;
    }

    @Override
    public SLabel executeOperation(CurrentContext context){
        return (context.getVariableValue(getVariable()) == context.getVariableValue(otherVariable))
                ? jeLabel : SpecialLabels.EMPTY;
    }

    public SVars getOtherVar() { return otherVariable; }

    @Override
    public SLabel getTargetLabel() { return jeLabel; }

    @Override
    public SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap) {
        SVars newVar     = varMap.getOrDefault(getVariable(), getVariable());
        SVars newOther   = varMap.getOrDefault(this.otherVariable, this.otherVariable);
        SLabel newLabel   = labelMap.getOrDefault(getLabel(), getLabel());
        SLabel newTarget  = labelMap.getOrDefault(getTargetLabel(), getTargetLabel());
        return new JumpEqualVariableInst(newVar, newOther, newTarget, newLabel);
    }
}
