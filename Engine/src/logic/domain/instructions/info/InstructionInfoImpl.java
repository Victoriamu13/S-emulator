package logic.domain.instructions.info;

public class InstructionInfoImpl implements InstructionInfo {
    private final int index;
    private final int originIndex;
    private final boolean synthetic;
    private final String labelText;
    private final String commandText;
    private final int cycles;

    public InstructionInfoImpl(int index,int originIndex, boolean synthetic, String labelText, String commandText, int cycles) {
        this.index = index;
        this.originIndex=originIndex;
        this.synthetic = synthetic;
        this.labelText = labelText;
        this.commandText = commandText;
        this.cycles = cycles;
    }

    @Override public int getIndex() { return index; }
    @Override public int getOriginIndex() { return originIndex; }
    @Override public boolean isSynthetic() { return synthetic; }
    @Override public String getLabelName() { return labelText; }
    @Override public String getFullCommand() { return commandText; }
    @Override public int getCycles() { return cycles; }
}
