package logic.domain.execution.context;

import logic.domain.variable.SVars;

import java.util.Map;

public interface CurrentContext {
    void updateVariable(SVars variable,long value);
    long getVariableValue(SVars variable);
    Map<SVars, Long> snapshot(); //variables status after run
}
