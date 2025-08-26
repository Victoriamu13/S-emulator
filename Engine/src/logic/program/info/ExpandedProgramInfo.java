package logic.program.info;

import logic.expand.expandProgram.ExpansionContext;
import logic.expand.expandProgram.ProgramExpander;
import logic.instructions.info.InstructionInfo;
import logic.instructions.SInstruction;
import logic.program.SProgram;

import java.util.*;

public class ExpandedProgramInfo implements ProgramInfo {

    private final String name;
    private final List<String> inputs;
    private final List<String> labels;
    private final List<InstructionInfo> instructions;

    public ExpandedProgramInfo(SProgram program, int degree, ProgramExpander expander, ExpansionContext ctx) {
        this.name = program.getName();

        List<InstNode> finalLayer = expandToFinalLayer(program, degree, expander);
        this.instructions = buildClusterInfos(program, finalLayer);

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
                List<SInstruction> expanded = expander.expandOne(node.instruction);
                if (expanded.size() == 1 && expanded.getFirst() == node.instruction) {
                    //basic instruction
                    next.add(node);
                } else {
                    //synthetic instruction
                    allBasic = false;
                    for (SInstruction child : expanded) {
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


    private List<InstructionInfo> buildClusterInfos(SProgram program, List<InstNode> finalLayer) {
        Map<Integer, List<InstNode>> byOrigin = new LinkedHashMap<>();
        int originalCount = program.getInstructions().size();

        for (int i = 1; i <= originalCount; i++) {
            byOrigin.put(i, new ArrayList<>());
        }
        for (InstNode n : finalLayer) {
            byOrigin.get(n.originIndex).add(n);
        }
        List<InstructionInfo> out = new ArrayList<>(byOrigin.size());
        for (int origin = 1; origin <= originalCount; origin++) {
            List<InstNode> seq = byOrigin.get(origin);
            String chain  = joinSegmentsReversed(seq);  //build string of all commands
            InstNode leftMost = seq.get(seq.size() - 1);
            out.add(ProgramInfoUtils.toInfo(leftMost.instruction, origin,chain ));
        }
        return out;
    }

    private static String joinSegmentsReversed(List<InstNode> seq) {
        List<String> parts = new ArrayList<>();
        for (int i = seq.size() - 1; i >= 0; i--) {
            parts.add(formatSegment(seq.get(i)));
        }
        return String.join("  >>>  ", parts);
    }


    private static String formatSegment(InstNode n) {
        String number    = "#" + n.finalIndex;
        String type    = ProgramInfoImpl.isSynthetic(n.instruction) ? "S" : "B";
        String label   = n.instruction.getLabel().getLabelRepresentation();
        String labelBox= String.format("[ %-5s ]", label);
        String command = ProgramInfoImpl.toCommandText(n.instruction);
        int cycles     = n.instruction.cycles();
        return String.format("%s(%s)%s%s(%d)", number, type, labelBox, command, cycles);
    }

    //inner class
    private static class InstNode {
        final SInstruction instruction;
        final InstNode parent;
        final int originIndex;
        int finalIndex;

        InstNode(SInstruction instruction, InstNode parent,int originIndex) {
            this.instruction = instruction;
            this.parent = parent;
            this.originIndex = originIndex;
        }
    }

}
