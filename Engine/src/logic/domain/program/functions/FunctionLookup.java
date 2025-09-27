package logic.domain.program.functions;

import logic.domain.instructions.SInstruction;
import logic.domain.instructions.info.InstructionInfo;
import logic.domain.variable.SVars;

import java.util.List;
import java.util.Set;

public interface FunctionLookup {
    List<SInstruction> bodyOf(String functionName);
    List<SVars> argsOf(String functionName);
    String userStringOf(String functionName);
    Set<String> allFunctionNames();
    String internalNameOf(String userString);
}
