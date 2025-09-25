package logic.domain.instructions.basic.bNoJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

import java.util.Map;

public class NeutralInst extends AbstractInstruction {

    public NeutralInst(SVars variable) {
        super(InstructionData.NEUTRAL,variable);
    }

    public NeutralInst(SVars variable, SLabel label) {
        super(InstructionData.NEUTRAL,variable,label);
    }

    @Override
    public SLabel executeOperation(CurrentContext context){
        return SpecialLabels.EMPTY;
    }

    @Override
    public SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap) {
        SVars newVar = varMap.getOrDefault(getVariable(), getVariable());
        SLabel newLabel = labelMap.getOrDefault(getLabel(), getLabel());
        return new NeutralInst(newVar, newLabel);
    }

}
