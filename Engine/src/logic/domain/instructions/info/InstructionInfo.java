package logic.domain.instructions.info;

public interface InstructionInfo {
    int getIndex();
    boolean isSynthetic();
    String getVariableName();
    String getLabelName();
    String getFullCommand();
    int getCycles();
}
