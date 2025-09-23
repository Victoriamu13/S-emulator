package logic.domain.instructions.info;

public interface InstructionInfo {
    String getName();
    int getIndex();
    boolean isSynthetic();
    String getVariableName();
    String getLabelName();
    String getFullCommand();
    int getCycles();
}
