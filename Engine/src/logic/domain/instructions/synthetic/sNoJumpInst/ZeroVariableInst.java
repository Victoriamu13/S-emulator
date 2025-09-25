package logic.domain.instructions.synthetic.sNoJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

import java.util.Map;

public class ZeroVariableInst extends AbstractInstruction {

    public ZeroVariableInst(SVars variable) {
        super(InstructionData.ZERO_VARIABLE,variable);
    }

    public ZeroVariableInst(SVars variable, SLabel label) {
        super(InstructionData.ZERO_VARIABLE,variable,label);
    }

    @Override
    public SLabel executeOperation(CurrentContext context){
        context.updateVariable(getVariable(), 0L);
        return SpecialLabels.EMPTY;
    }

    @Override
    public SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap) {
        SVars newVar   = varMap.getOrDefault(getVariable(), getVariable());
        SLabel newLabel = labelMap.getOrDefault(getLabel(), getLabel());
        return new ZeroVariableInst(newVar, newLabel);
    }
}
