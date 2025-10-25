package logic.domain.program.functions;

import logic.domain.instructions.SInstruction;
import logic.domain.instructions.info.InstructionInfo;
import logic.domain.variable.SVars;

import java.util.List;
import java.util.Map;
import java.util.Set;

public enum EmptyFunctionLookup implements FunctionLookup{
    EMPTY;
    @Override public List<SInstruction>bodyOf(String functionName){return List.of();}

    @Override public  List<SVars> argsOf(String functionName){return List.of();}

    @Override public String userStringOf(String functionName) { return functionName; }

    @Override public Set<String> allFunctionNames() { return Set.of(); }
}
