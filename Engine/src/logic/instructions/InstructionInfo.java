package logic.instructions;

public interface InstructionInfo {
    int getIndex();
    boolean isSynthetic();
    String getLabelName();
    String getFullCommand();
    int getCycles();
}
