package logic.Instructions;

import logic.Instructions.SInstruction;

public class AbstractInstruction implements SInstruction {

    private final InstructionData InstructionData;

    public AbstractInstruction(InstructionData InstructionData) {
        this.InstructionData = InstructionData;
    }

    @Override
    public String getName(){
        return InstructionData.getName();
    }

    @Override
    public int cycles() {
        return InstructionData.cycles();
    }
}
