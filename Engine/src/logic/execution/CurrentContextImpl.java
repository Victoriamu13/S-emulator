package logic.execution;

import java.util.HashMap;
import java.util.Map;

import logic.variable.SVars;
import logic.variable.SVarsType;

import static logic.variable.SVars.RESULT;

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
