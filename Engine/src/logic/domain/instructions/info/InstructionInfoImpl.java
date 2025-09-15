package logic.domain.instructions.info;

public class InstructionInfoImpl implements InstructionInfo {
    private final int index;
    private final boolean synthetic;
    private final String variableName;
    private final String labelText;
    private final String commandText;
    private final int cycles;

    public InstructionInfoImpl(int index, boolean synthetic,String variableName, String labelText, String commandText, int cycles) {
        this.index = index;
        this.synthetic = synthetic;
        this.variableName = variableName;
        this.labelText = labelText;
        this.commandText = commandText;
        this.cycles = cycles;
    }

    @Override public int getIndex() { return index; }
    @Override public boolean isSynthetic() { return synthetic; }
    @Override public String getVariableName() { return variableName; }
    @Override public String getLabelName() { return labelText; }
    @Override public String getFullCommand() { return commandText; }
    @Override public int getCycles() { return cycles; }
}
