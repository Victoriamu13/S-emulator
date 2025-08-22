package logic.instructions;

import java.util.Set;

public enum InstructionData {
    INCREASE("Increase",1),
    DECREASE("Decrease",1),
    NEUTRAL("Neutral",0),
    JUMP_NOT_ZERO("Jump_Not_Zero",2),
    ZERO_VARIABLE("Zero_Variable",1),
    ASSIGMENT("Assignment",4),
    GOTO_LABEL("Goto_Label",1),
    CONSTANT_ASSIGNMENT("Constant_Assignment",2),
    JUMP_ZERO("Jump_Zero",2),
    JUMP_EQUAL_CONSTANT("Jump_Equal_Constant",2),
    JUMP_EQUAL_VARIABLE("Jump_Equal_Variable",2);


    private final String name;
    private final int cycles;

    InstructionData(String name, int cycles) {
        this.name = name;
        this.cycles = cycles;
    }

    public String getName(){return name;}
    public int cycles() {return cycles;}
}
