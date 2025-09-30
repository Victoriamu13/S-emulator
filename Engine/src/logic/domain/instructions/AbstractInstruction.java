package logic.domain.instructions;

import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

import java.util.Map;

public abstract class AbstractInstruction implements SInstruction {

    private final InstructionData data;
    private final SLabel label;
    private final SVars variable;

    //without label attached
    public AbstractInstruction(InstructionData InstructionData,SVars variable) {
        this(InstructionData, variable,SpecialLabels.EMPTY);
    }

    //with label attached
    public AbstractInstruction(InstructionData instructionData, SVars variable,SLabel label) {
        this.data = instructionData;
        this.label = label;
        this.variable=variable;
    }

    @Override
    public String getName(){
        return data.getName();
    }

    @Override
    public int cycles() {
        return data.cycles();
    }


    @Override
    public SLabel getLabel() {return (label != null) ? label : SpecialLabels.EMPTY;}

    @Override
    public SVars getVariable() {
        return variable;
    }

    @Override
    public InstructionData getData() {
        return data;
    }

    public abstract SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap);
}
