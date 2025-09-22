package logic.domain.program.functions;

import logic.domain.instructions.SInstruction;

import java.util.List;

public enum EmptyFunctionLookup implements FunctionLookup{
    EMPTY;

    @Override public List<SInstruction>bodyOf(String functionName){return List.of();}
    @Override public boolean exists(String functionName){return false;}
}
