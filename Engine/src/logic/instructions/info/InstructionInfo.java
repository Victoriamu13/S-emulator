package logic.instructions.info;

public interface InstructionInfo {
    int getIndex();
    boolean isSynthetic();
    String getLabelName();
    String getFullCommand();
    int getCycles();
}
