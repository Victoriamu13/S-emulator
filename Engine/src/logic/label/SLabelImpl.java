package logic.label;

public class SLabelImpl implements SLabel {
    private final String label;

    public SLabelImpl(int number) {
        label="L"+number;
    }

    public String getLabelRepresentation() {
        return label;
    }
}
