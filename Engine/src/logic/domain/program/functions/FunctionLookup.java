package logic.domain.program.functions;

import logic.domain.instructions.SInstruction;
import logic.domain.variable.SVars;

import java.util.List;

public interface FunctionLookup {
    List<SInstruction> bodyOf(String functionName);
    List<SVars> argsOf(String functionName);
}
