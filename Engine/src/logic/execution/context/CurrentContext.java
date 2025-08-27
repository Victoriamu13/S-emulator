package logic.execution.context;

import logic.variable.SVars;

import java.util.Map;

public interface CurrentContext {
    void updateVariable(SVars variable,long value);
    long getVariableValue(SVars variable);
    Map<SVars, Long> snapshot(); //variables status after run
}
