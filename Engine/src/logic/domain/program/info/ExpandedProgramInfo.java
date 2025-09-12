package logic.domain.program.info;

import logic.domain.expand.expandProgram.ExpanderFactory;
import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.expandProgram.ProgramExpander;
import logic.domain.instructions.info.InstructionInfo;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.info.InstructionInfoImpl;
import logic.domain.program.SProgram;

import java.util.*;

import static logic.domain.program.info.ProgramInfoUtils.toInfo;

public class ExpandedProgramInfo implements ProgramInfo {

    private final String name;
    private final List<String> inputs;
    private final List<String> labels;
    private final List<InstructionInfo> instructions;
    private final List<InstNode> finalLayer;

    public ExpandedProgramInfo(SProgram program, int degree, ProgramExpander expander, ExpansionContext ctx) {
        this.name = program.getName();

        this.finalLayer = expandToFinalLayer(program, degree, expander);
        this.instructions = buildFinalInfos(finalLayer);

        List<SInstruction> expandedList = new ArrayList<>(finalLayer.size());
        for (InstNode n : finalLayer) expandedList.add(n.instruction);
        this.inputs = ProgramInfoUtils.inputsUsed(expandedList);
        this.labels = ProgramInfoUtils.labelsUsed(expandedList);
    }

    @Override public String getName() {return name;}
    @Override public int getNumberOfInstructions() {return instructions.size();}
    @Override public List<String> getInputsUsed() {return inputs;}
    @Override public List<String> getLabelsUsed() {return labels;}
    @Override public List<InstructionInfo> getInstructions() {return instructions;}

    private List<InstNode> expandToFinalLayer(SProgram program, int degree, ProgramExpander expander) {
        List<InstNode> cur = new ArrayList<>();
        List<SInstruction> src = program.getInstructions();

        for (int i = 0; i < src.size(); i++) {
            cur.add(new InstNode(src.get(i), null,i + 1));
        }

        for (int d = 0; d < degree; d++) {
            boolean allBasic = true;
            List<InstNode> next = new ArrayList<>();
            for (InstNode node : cur) {
                boolean isBasic = (ExpanderFactory.forInstruction(node.instruction) == null);
                if (isBasic) {
                    //basic instruction
                    next.add(node);
                } else {
                    //synthetic instruction
                    allBasic = false;
                    for (SInstruction child : expander.expandOne(node.instruction)) {
                        next.add(new InstNode(child, node,node.originIndex));
                    }
                }
            }
            cur = next;
            if (allBasic) break;
        }
        int k = 1;
        for (InstNode n : cur) n.finalIndex = k++;
        return cur;
    }


    private List<InstructionInfo> buildFinalInfos(List<InstNode> finalLayer) {
        finalLayer.sort((a, b) -> Integer.compare(a.finalIndex, b.finalIndex));

        List<InstructionInfo> out = new ArrayList<>(finalLayer.size());
        for (InstNode n : finalLayer) {
            out.add(toInfo(n.instruction, n.finalIndex, n.originIndex,null));
        }
        return out;
    }

    public List<InstructionInfo> getExpansionForFinalIndex(int finalIndex) {
        InstNode selected = finalLayer.stream()
                .filter(n -> n.finalIndex == finalIndex)
                .findFirst()
                .orElse(null);
        if (selected == null) return List.of();

        //case basic instruction
        if (selected.children.isEmpty()) {
            boolean synthetic = ProgramInfoImpl.isSynthetic(selected.instruction);
            String label = selected.instruction.getLabel().getLabelRepresentation();
            String command = ProgramInfoImpl.toCommandText(selected.instruction);
            int cycles = selected.instruction.cycles();

            return List.of(new InstructionInfoImpl(
                    selected.finalIndex,
                    selected.originIndex,
                    synthetic,
                    label,
                    command,
                    cycles
            ));
        }

        List<InstructionInfo> out = new ArrayList<>();
        for (InstNode n : selected.children) {
            boolean synthetic = ProgramInfoImpl.isSynthetic(n.instruction);
            String label = n.instruction.getLabel().getLabelRepresentation();
            String command = ProgramInfoImpl.toCommandText(n.instruction);
            int cycles = n.instruction.cycles();

            out.add(new InstructionInfoImpl(
                    n.finalIndex,
                    n.originIndex,
                    synthetic,
                    label,
                    command,
                    cycles
            ));
        }
        return out;
    }


     public boolean isSynthetic() {
           return false;
     }


     public int getCycles() {
           return 0;
     }



    //inner class
    private static class InstNode {
        final SInstruction instruction;
        final InstNode parent;
        final int originIndex;
        int finalIndex;
        final List<InstNode> children = new ArrayList<>();

        InstNode(SInstruction instruction, InstNode parent,int originIndex) {
            this.instruction = instruction;
            this.parent = parent;
            this.originIndex = originIndex;
            if (parent != null) {
                parent.children.add(this);
            }
        }
    }

}
