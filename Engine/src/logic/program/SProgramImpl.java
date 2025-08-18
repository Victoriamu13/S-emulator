package logic.program;

import logic.instructions.SInstruction;
import logic.variable.SVars;

import java.util.ArrayList;
import java.util.List;

public class SProgramImpl implements SProgram {
    private final String name;
    private final List<SInstruction> instructions;

    public SProgramImpl(String name) {
        this.name = name;
       instructions = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addInstruction(SInstruction instruction) {
        instructions.add(instruction);
    }

    @Override
    public List<SInstruction> getInstructions() {
        return instructions;
    }
}
