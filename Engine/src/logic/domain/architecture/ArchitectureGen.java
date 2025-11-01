package logic.domain.architecture;


import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.instructions.info.InstructionInfo;

import java.util.EnumSet;

public enum ArchitectureGen {
    I(5, EnumSet.of(InstructionData.NEUTRAL,
            InstructionData.INCREASE,
            InstructionData.DECREASE,
            InstructionData.JUMP_NOT_ZERO)),
    II(100,EnumSet.of(InstructionData.ZERO_VARIABLE,
            InstructionData.CONSTANT_ASSIGNMENT,
            InstructionData.GOTO_LABEL)),
    III(500,EnumSet.of(InstructionData.ASSIGNMENT,
            InstructionData.JUMP_ZERO,
            InstructionData.JUMP_EQUAL_CONSTANT,
            InstructionData.JUMP_EQUAL_VARIABLE)),
    IV(1000,EnumSet.of(InstructionData.QUOTE,
            InstructionData.JUMP_EQUAL_FUNCTION));

    private final int baseCost;
    private final EnumSet<InstructionData> instructions;

    ArchitectureGen(int baseCost, EnumSet<InstructionData> instructions) {
        this.baseCost = baseCost;
        this.instructions = instructions;
    }

    public int getBaseCost(){return baseCost;}
    public EnumSet<InstructionData> getInstructions(){return instructions;}

    public EnumSet<InstructionData> getSupportedInstructions(){
        EnumSet<InstructionData> genInstructions = EnumSet.noneOf(InstructionData.class); //create empty group
        for (ArchitectureGen gen : ArchitectureGen.values()) { // check all groups
            genInstructions.addAll(gen.instructions);
            if (gen == this) break; //stop at this architecture
        }
        return genInstructions;
    }

    public boolean supports(SInstruction inst) {
        return  getSupportedInstructions().contains(inst.getData());
    }

    public boolean supports(InstructionInfo info) {
        if (info == null) return false;
        InstructionData data = InstructionData.fromStringToData(info.getName());
        return getSupportedInstructions().contains(data);
    }

}