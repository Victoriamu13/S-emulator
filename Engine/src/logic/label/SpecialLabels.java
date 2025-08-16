package logic.label;

public enum SpecialLabels implements SLabel {

    EXIT{
        @Override
        public String getLabelRepresentation() {
            return "EXIT";
        }
    },
    EMPTY{
        @Override
        public String getLabelRepresentation() {
            return "";
        }
    };

}
