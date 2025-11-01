package logic.domain.architecture;

import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.program.SProgram;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ArchitectureAnalyzer {

    ArchitectureAnalyzer(){}

    public static Set<InstructionData> createInstructionsDataSet(SProgram program) {
        EnumSet<InstructionData> instructionsSet = EnumSet.noneOf(InstructionData.class);
        for (SInstruction inst : program.getInstructions()) {
            instructionsSet.add(inst.getData());
        }
        return instructionsSet;
    }

    public static ArchitectureGen minimalCompatibleGen(SProgram program){
        Set<InstructionData> instructionDataSet=createInstructionsDataSet(program);
        for(ArchitectureGen gen : ArchitectureGen.values()){
            if(gen.getSupportedInstructions().containsAll(instructionDataSet)){
                return gen;
            }
        }
        return null;
    }

    public static int countSupported(SProgram program,ArchitectureGen gen){
        int count=0;
        for(SInstruction inst:program.getInstructions()){
            if(gen.supports(inst)) count++;
        }
        return count;
    }

    public static List<InstructionData> unsupportedInstructions(SProgram program,ArchitectureGen gen){
        List<InstructionData> unsupported=new ArrayList<>();
        for(SInstruction inst :program.getInstructions()){
            if(!gen.supports(inst)) unsupported.add(inst.getData());
        }
        return unsupported;
    }

    public static boolean isCompatible(SProgram program, ArchitectureGen gen) {
        return unsupportedInstructions(program, gen).isEmpty();
    }
}
