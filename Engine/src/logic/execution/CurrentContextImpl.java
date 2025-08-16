package logic.execution;

import java.util.HashMap;
import java.util.Map;

import logic.variable.SVars;

public class CurrentContextImpl implements CurrentContext {

    private final Map<SVars,Long> variableState;

    public CurrentContextImpl() {
        this.variableState = new HashMap<>();
    }

    @Override
    public long getVariableValue(SVars variable){
        return variableState.getOrDefault(variable,0L);
    }

    @Override
    public void updateVariable(SVars variable,long value) {
        variableState.put(variable,value);
    }

}
