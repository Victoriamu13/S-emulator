package logic.domain.execution.context;

import java.util.HashMap;
import java.util.Map;

import logic.domain.program.functions.FunctionLookup;
import logic.domain.program.functions.FunctionRepository;
import logic.domain.variable.SVars;
import logic.domain.variable.SVarsImpl;
import logic.domain.variable.SVarsType;

public class CurrentContextImpl implements CurrentContext {

    // Holds current variable values (x_i, z_i, y) → value
    private final Map<SVars,Long> variableState=new HashMap<>();

    // Function repository for nested calls
    private final FunctionLookup functionLookup;

    private long cycles = 0;

    public CurrentContextImpl(long[] inputs,FunctionLookup functionLookup) {
        for (int i = 0; i < inputs.length; i++) {
            SVars xi = new SVarsImpl(SVarsType.INPUT, i + 1); // "x" + (i+1)
            variableState.put(xi, inputs[i]);  // set xi = input[i]
        }
        this.functionLookup = functionLookup;
    }

    @Override
    public long getVariableValue(SVars variable){
        return variableState.getOrDefault(variable,0L);
    }

    @Override
    public void updateVariable(SVars variable,long value) {
        variableState.put(variable,value);
    }

    @Override
    public Map<SVars, Long> snapshot() {
        return new HashMap<>(variableState);
    }  // defensive copy for reports

    @Override
    public void restoreSnapshot(Map<SVars, Long> snapshot) {
        variableState.clear();
        variableState.putAll(snapshot);
    }

    @Override
    public FunctionLookup getFunctionLookup() {
        return functionLookup;
    }

    @Override
    public void addCycles(long c) {cycles += c;}

    @Override
    public long getCycles() {return cycles;}
}
