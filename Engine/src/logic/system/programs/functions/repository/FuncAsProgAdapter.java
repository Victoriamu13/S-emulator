package logic.system.programs.functions.repository;

import logic.domain.instructions.SInstruction;
import logic.domain.program.SProgram;
import logic.domain.program.SProgramImpl;
import logic.domain.program.functions.FunctionLookup;

import java.util.List;
import java.util.Locale;

public class FuncAsProgAdapter {
    private final String functionName;
    private final FunctionLookup lookup;

    public FuncAsProgAdapter(String functionName, FunctionLookup lookup) {
        this.functionName = functionName.trim();
        this.lookup = lookup;
    }

    public SProgram asProgram() {
        //build a program
        SProgramImpl program = new SProgramImpl(functionName);
        program.setFunctionLookup(lookup);

        //copy body
        List<SInstruction> body = lookup.bodyOf(functionName);
        if (body != null) {
            body.forEach(program::addInstruction);
        }

        return program;
    }

}
