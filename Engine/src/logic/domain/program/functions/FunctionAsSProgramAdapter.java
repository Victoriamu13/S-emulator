package logic.domain.program.functions;

import logic.domain.instructions.SInstruction;
import logic.domain.program.SProgram;
import logic.domain.program.SProgramImpl;

import java.util.List;

public class FunctionAsSProgramAdapter {
    private final String functionName;
    private final FunctionLookup lookup;

    public FunctionAsSProgramAdapter(String functionName, FunctionLookup lookup) {
        this.functionName = functionName;
        this.lookup = lookup;
    }

    public SProgram asProgram() {
        //build a program
        String progName =lookup.userStringOf(functionName);

        SProgramImpl program = new SProgramImpl(progName);
        program.setFunctionLookup(lookup);

        //copy body
        List<SInstruction> body = lookup.bodyOf(functionName);
        if (body != null) {
            body.forEach(program::addInstruction);
        }

        return program;
    }

}
