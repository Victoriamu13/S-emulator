package logic.execution.context;

import logic.variable.SVars;

public interface CurrentContext {
    void updateVariable(SVars variable,long value);
    long getVariableValue(SVars variable);
}
