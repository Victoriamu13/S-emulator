package logic.variable;

import java.util.Objects;

public class SVarsImpl implements SVars {
    SVarsType type;
    int number;

    public SVarsImpl(SVarsType type, int number) {
        this.type = type;
        this.number = number;
    }

    @Override
    public SVarsType getType() {
        return type;
    }

    @Override
    public String getRepresentation() {
        return type.getVarRepresentation(number);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SVarsImpl)) return false;
        SVarsImpl other = (SVarsImpl) o;
        return this.type == other.type && this.number == other.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, number);
    }

}
