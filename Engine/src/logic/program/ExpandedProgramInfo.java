package logic.program;

import logic.expand.expandProgram.ExpansionContext;
import logic.expand.expandProgram.ProgramExpander;
import logic.instructions.InstructionInfo;
import logic.instructions.InstructionInfoImpl;
import logic.instructions.SInstruction;
import logic.label.SLabel;

import java.util.ArrayList;
import java.util.List;

public class ExpandedProgramInfo implements ProgramInfo {

    private final String name;
    private final List<String> inputs;
    private final List<String> labels;
    private final List<InstructionInfo> instructions;

    public ExpandedProgramInfo(SProgram program, int degree, ProgramExpander expander, ExpansionContext ctx) {
        this.name = program.getName();
        this.inputs = new ProgramInfoImpl(program).getInputsUsed();
        this.labels = new ProgramInfoImpl(program).getLabelsUsed();
        this.instructions = getExpandedInstructions(program, degree, expander, ctx);
    }

    @Override public String getName() {return name;}
    @Override public int getNumberOfInstructions() {return instructions.size();}
    @Override public List<String> getInputsUsed() {return inputs;}
    @Override public List<String> getLabelsUsed() {return labels;}
    @Override public List<InstructionInfo> getInstructions() {return instructions;}

    private List<InstructionInfo> getExpandedInstructions(SProgram program, int degree, ProgramExpander expander, ExpansionContext ctx) {
        List<InstNode> cur = new ArrayList<>();
        for (SInstruction inst : program.getInstructions()) {
            cur.add(new InstNode(inst, null));
        }

        //expand instructions
        for (int d = 0; d < degree; d++) {
            boolean allBasic = true;
            List<InstNode> next = new ArrayList<>();

            for (InstNode node : cur) {
                List<SInstruction> expanded = expander.expandOne(node.instruction);
                if (expanded.size() == 1 && expanded.get(0) == node.instruction) {
                    next.add(node);
                } else {
                    allBasic = false;
                    for (SInstruction child : expanded) {
                        next.add(new InstNode(child, node));
                    }
                }
            }
            cur = next;
            if (allBasic) break;
        }
        //build instructions
        List<InstructionInfo> infos = new ArrayList<>();
        int idx = 1;
        for (InstNode n : cur) {
            SLabel lbl = n.instruction.getLabel();
            String label = (lbl == null) ? "" : lbl.getLabelRepresentation();
            String main = ProgramInfoImpl.toCommandText(n.instruction);
            String trail = buildTrail(n.parent);
            String fullCommand = trail.isEmpty() ? main : main + "  <<<   " + trail;
            infos.add(new InstructionInfoImpl(idx++, true, label, fullCommand, n.instruction.cycles()));
        }
        return infos;
    }

    private String buildTrail(InstNode parent) {
        if (parent == null) return "";
        List<String> parts = new ArrayList<>();
        InstNode p = parent;
        while (p != null) {
            parts.add(ProgramInfoImpl.toCommandText(p.instruction));
            p = p.parent;
        }
        return String.join("  <<<   ", parts);
    }

    //inner class
    private static class InstNode {
        final SInstruction instruction;
        final InstNode parent;

        InstNode(SInstruction instruction, InstNode parent) {
            this.instruction = instruction;
            this.parent = parent;
        }
    }

}
