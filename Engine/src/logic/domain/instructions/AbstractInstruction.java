package logic.domain.instructions;

import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

public abstract class AbstractInstruction implements SInstruction {

    private final InstructionData InstructionData;
    private final SLabel label;
    private final SVars variable;

    //without label attached
    public AbstractInstruction(InstructionData InstructionData,SVars variable) {
        this(InstructionData, variable,SpecialLabels.EMPTY);
    }

    //with label attached
    public AbstractInstruction(InstructionData InstructionData, SVars variable,SLabel label) {
        this.InstructionData = InstructionData;
        this.label = label;
        this.variable=variable;
    }

    @Override
    public String getName(){
        return InstructionData.getName();
    }

    @Override
    public int cycles() {
        return InstructionData.cycles();
    }


    @Override
    public SLabel getLabel() {
        return label;
    }

    @Override
    public SVars getVariable() {
        return variable;
    }

}
