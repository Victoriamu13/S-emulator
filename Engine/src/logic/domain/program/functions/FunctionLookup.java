package logic.domain.program.functions;

import logic.domain.instructions.SInstruction;

import java.util.List;

public interface FunctionLookup {
    List<SInstruction> bodyOf(String functionName);

    boolean exists(String functionName);
}
