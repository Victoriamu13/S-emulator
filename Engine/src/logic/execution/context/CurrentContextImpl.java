package logic.execution.context;

import java.util.HashMap;
import java.util.Map;

import logic.variable.SVars;
import logic.variable.SVarsImpl;
import logic.variable.SVarsType;

public class CurrentContextImpl implements CurrentContext {

    private final Map<SVars,Long> variableState=new HashMap<>();

    public CurrentContextImpl(long[] inputs) {

        for (int i = 0; i < inputs.length; i++) {
            SVars xi = new SVarsImpl(SVarsType.INPUT, i + 1); // "x" + (i+1)
            variableState.put(xi, inputs[i]);
        }

    }

    @Override
    public long getVariableValue(SVars variable){
        return variableState.getOrDefault(variable,0L);
    }

    @Override
    public void updateVariable(SVars variable,long value) {
        variableState.put(variable,value);
    }

    public Map<SVars, Long> snapshot() {
        return new HashMap<>(variableState);
    }

}
