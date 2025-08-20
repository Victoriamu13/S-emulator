package logic.instructions;

public enum InstructionData {
    INCREASE("Increase",1),
    DECREASE("Decrease",1),
    NO_OP("NoOp",0),
    JUMP_NOT_ZERO("Jump_Not_Zero",3);


    private final String name;
    private final int cycles;

    InstructionData(String name, int cycles) {
        this.name = name;
        this.cycles = cycles;
    }


    public String getName(){
        return name;
    }


    public int cycles() {
        return cycles;
    }


}
