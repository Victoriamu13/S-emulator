package logic.domain.instructions.data;

public enum ArgumentData {
    JNZ_LABEL("JNZLabel"),
    GOTO_LABEL("gotoLabel"),
    ASSIGNED_VARIABLE("assignedVariable"),
    CONSTANT_VALUE("constantValue"),
    JZ_LABEL("JZLabel"),
    JE_CONSTANT_LABEL("JEConstantLabel"),
    JE_VARIABLE_LABEL("JEVariableLabel"),
    VARIABLE_NAME("variableName"),
    FUNCTION_NAME("functionName"),
    FUNCTION_ARGUMENTS("functionArguments"),
    JE_FUNCTION_LABEL("JEFunctionLabel");

    private final String name;

    ArgumentData(String name){this.name = name;}

    public String getName() {return name;}

    public static ArgumentData fromString(String s) {
        if (s==null) return null;
        for(ArgumentData a : values()) {
            if(a.getName().equalsIgnoreCase(s.trim())) return a;
        }
        return null;
    }

    public static boolean knownArgument(String s){
        return fromString(s)!=null;
    }
}
