package logic.label;

public enum SpecialLabels implements SLabel {

    EXIT{
        @Override public String getLabelRepresentation() {
            return "EXIT";
        }
        @Override public boolean isNumberLabel() { return false; }
    },
    EMPTY{
        @Override public String getLabelRepresentation() {
            return "";
        }
        @Override public boolean isNumberLabel() { return false; }
    };

}
