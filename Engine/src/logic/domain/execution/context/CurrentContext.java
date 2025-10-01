package logic.domain.execution.context;

import logic.domain.program.functions.FunctionLookup;
import logic.domain.program.functions.FunctionRepository;
import logic.domain.variable.SVars;

import java.util.Map;

public interface CurrentContext {
    void updateVariable(SVars variable,long value);
    long getVariableValue(SVars variable);
    Map<SVars, Long> snapshot(); //variables status after run
    void restoreSnapshot(Map<SVars, Long> snap);
    FunctionLookup getFunctionLookup();
    void addCycles(long cycles);
    long getCycles();
}
