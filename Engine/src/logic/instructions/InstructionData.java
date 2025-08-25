package logic.instructions;

import java.util.Set;

public enum InstructionData {
    INCREASE("Increase",1,true),
    DECREASE("Decrease",1,true),
    NEUTRAL("Neutral",0,true),
    JUMP_NOT_ZERO("Jump_Not_Zero",2,true),
    ZERO_VARIABLE("Zero_Variable",1,false),
    ASSIGNMENT("Assignment",4,false),
    GOTO_LABEL("Goto_Label",1,false),
    CONSTANT_ASSIGNMENT("Constant_Assignment",2,false),
    JUMP_ZERO("Jump_Zero",2,false),
    JUMP_EQUAL_CONSTANT("Jump_Equal_Constant",2,false),
    JUMP_EQUAL_VARIABLE("Jump_Equal_Variable",2,false);

    private final String name;
    private final int cycles;
    private final boolean basic;

    InstructionData(String name, int cycles,boolean basic) {
        this.name = name;
        this.cycles = cycles;
        this.basic = basic;
    }

    public String getName(){return name;}
    public int cycles() {return cycles;}
}
