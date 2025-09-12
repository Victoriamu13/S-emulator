package logic.domain.instructions.info;

public interface InstructionInfo {
    int getIndex();
    int getOriginIndex();
    boolean isSynthetic();
    String getLabelName();
    String getFullCommand();
    int getCycles();
}
