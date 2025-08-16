package logic.variable;

public enum SVarsType {

    RESULT{
        @Override
        public String getVarRepresentation(int number) {
            return "y";
        }
    },
    INPUT{
        @Override
        public String getVarRepresentation(int number) {
            return "x"+number;
        }
    },
    WORK{
            @Override
            public String getVarRepresentation(int number) {
                return "z"+number;
            }
        };

    public abstract String getVarRepresentation(int number);
}
