package logic.domain.architecture;

import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.program.SProgram;

import java.util.*;

public class ArchitectureAnalyzer {

    ArchitectureAnalyzer(){}

    public static Set<InstructionData> createInstructionsDataSet(SProgram program) {
        EnumSet<InstructionData> instructionsSet = EnumSet.noneOf(InstructionData.class);
        for (SInstruction inst : program.getInstructions()) {
            instructionsSet.add(inst.getData());
        }
        return instructionsSet;
    }

    //Counts how many instructions in the program are SUPPORTED by each architecture.
    public static Map<ArchitectureGen, Integer> countSupportedByArchitecture(SProgram program) {
        Map<ArchitectureGen, Integer> result = new LinkedHashMap<>();

        //Init number instructions in every architecture set
        for (ArchitectureGen gen : ArchitectureGen.values()) {
            result.put(gen, 0);
        }

        // For each architecture, count how many instructions it supports
        for (ArchitectureGen gen : ArchitectureGen.values()) {
            int count = 0;
            for (SInstruction inst : program.getInstructions()) {
                if (gen.getSupportedInstructions().contains(inst.getData())) count++;
            }
            result.put(gen, count);
        }
        return result;
    }

    //  Counts how many instructions require each architecture as their minimal generation.
    public static Map<ArchitectureGen, Integer> countFromArchitecture(SProgram program) {
        Map<ArchitectureGen, Integer> result = new LinkedHashMap<>();

        for (ArchitectureGen gen : ArchitectureGen.values()) {
            result.put(gen, 0);  //Init number instructions in every architecture set
        }
        for (SInstruction inst : program.getInstructions()) {
            ArchitectureGen requiredGen = minCompatibleArchitecture(EnumSet.of(inst.getData()));
            if (requiredGen != null) {
                result.put(requiredGen, result.get(requiredGen) + 1);
            }
        }
        return result;
    }

    public static ArchitectureGen minCompatibleArchitecture(Set<InstructionData> singleSet){
        for(ArchitectureGen gen : ArchitectureGen.values()){
            if(gen.getSupportedInstructions().containsAll(singleSet)){
                return gen;
            }
        }
        return null;
    }

}
