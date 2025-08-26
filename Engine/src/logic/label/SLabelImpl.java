package logic.label;

import java.util.Objects;

public class SLabelImpl implements SLabel {
    private final String label;

    public SLabelImpl(int number) {
        label="L"+number;
    }

    @Override public String getLabelRepresentation() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SLabelImpl other)) return false;
        return Objects.equals(label, other.label);
    }

    @Override public int hashCode() {
        return Objects.hash(label);
    }

    @Override public String toString() {
        return label;
    }

    @Override public boolean isNumberLabel() { return true; }

}

