package logic.domain.program;

import logic.domain.instructions.SInstruction;
import logic.domain.program.functions.EmptyFunctionLookup;
import logic.domain.program.functions.FunctionLookup;

import java.util.ArrayList;
import java.util.List;

public class SProgramImpl implements SProgram {
    private final String name;
    private final List<SInstruction> instructions= new ArrayList<>();
    private FunctionLookup functions=EmptyFunctionLookup.EMPTY;;

    public SProgramImpl(String name) {this.name = name;}

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

    @Override public FunctionLookup getFunctionLookup() { return functions; }

    @Override public void setFunctionLookup(FunctionLookup functions) {
        this.functions = (functions != null) ? functions : EmptyFunctionLookup.EMPTY;
    }

}
