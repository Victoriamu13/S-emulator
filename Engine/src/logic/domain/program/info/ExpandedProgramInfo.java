package logic.domain.program.info;

import logic.domain.expand.expandProgram.ExpanderFactory;
import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.expandProgram.ProgramExpander;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.info.InstructionInfo;
import logic.domain.program.SProgram;

import java.util.*;

import static logic.domain.program.info.ProgramInfoUtils.toInfo;

public class ExpandedProgramInfo implements ProgramInfo {

    private final String name;
    private final List<String> inputs;
    private final List<String> labels;
    private final List<InstructionInfo> instructions;

    private final ExpansionContext ctx;
    private final ProgramExpander expander;

    // Each layer = program at given degree
    private final List<List<InstNode>> layers = new ArrayList<>();

    // Index mapping (InstNode -> row number) per layer
    private final List<Map<InstNode, Integer>> layerIndex = new ArrayList<>();

    public ExpandedProgramInfo(SProgram program, int degree, ProgramExpander expander, ExpansionContext ctx) {
        this.name = program.getName();
        this.ctx = ctx;
        this.expander = expander;

        buildLayers(program, degree);

        List<InstNode> finalLayer = layers.get(layers.size() - 1);
        this.instructions = buildInfosForLayer(finalLayer, layerIndex.get(layers.size() - 1));

        List<SInstruction> expandedList = new ArrayList<>(finalLayer.size());
        for (InstNode n : finalLayer) expandedList.add(n.instruction);
        this.inputs = ProgramInfoUtils.inputsUsed(expandedList);
        this.labels = ProgramInfoUtils.labelsUsed(expandedList);
    }

    @Override public String getName() { return name; }
    @Override public int getNumberOfInstructions() { return instructions.size(); }
    @Override public List<String> getInputsUsed() { return inputs; }
    @Override public List<String> getLabelsUsed() { return labels; }
    @Override public List<InstructionInfo> getInstructions() { return instructions; }

    public List<String> getVariablesUsed() {
        List<InstNode> finalLayer = layers.get(layers.size() - 1);
        List<SInstruction> instrs = new ArrayList<>();
        for (InstNode n : finalLayer) {
            instrs.add(n.instruction);
        }
        return ProgramInfoUtils.variablesUsed(instrs);
    }

    // Build all expansion layers up to given degree
    private void buildLayers(SProgram program, int degree) {
        List<InstNode> layer0 = new ArrayList<>();
        List<SInstruction> src = program.getInstructions();

        // Layer 0 = original program
        for (int i = 0; i < src.size(); i++) {
            layer0.add(new InstNode(src.get(i), null, i + 1));
        }
        layers.add(layer0);
        layerIndex.add(indexLayer(layer0));

        // Expand layers until reaching given degree
        for (int d = 0; d < degree; d++) {
            boolean allBasic = true;
            List<InstNode> next = new ArrayList<>();

            for (InstNode node : layer0) {
                boolean isBasic = (ExpanderFactory.forInstruction(node.instruction) == null);
                if (isBasic) { // Add basic instruction
                    next.add(node);
                } else { // Expand synthetic instruction into children and add them
                    allBasic = false;
                    for (SInstruction child : expander.expandOne(node.instruction)) {
                        InstNode childNode = new InstNode(child, node, node.originIndex);
                        next.add(childNode);
                    }
                }
            }
            // Save next layer and prepare indexes
            layers.add(next);
            layerIndex.add(indexLayer(next));
            layer0 = next;

            if (allBasic) break;
        }
    }

    // Assign index to each node in a layer
    private Map<InstNode, Integer> indexLayer(List<InstNode> layer) {
        Map<InstNode, Integer> map = new IdentityHashMap<>();
        int k = 1;
        for (InstNode n : layer) map.put(n, k++);
        return map;
    }

    // Convert InstNodes of a layer into InstructionInfo
    private List<InstructionInfo> buildInfosForLayer(List<InstNode> layer, Map<InstNode, Integer> idxMap) {
        List<InstNode> ordered = new ArrayList<>(layer);
        ordered.sort(Comparator.comparingInt(idxMap::get));

        List<InstructionInfo> out = new ArrayList<>(ordered.size());
        for (InstNode n : ordered) {
            int idx = idxMap.get(n);
            out.add(toInfo(n.instruction, idx, n.originIndex, null));
        }
        return out;
    }

    // Return expansion for a selected instruction in the current degree
    public List<InstructionInfo> getExpansionForFinalIndex(int degree, int finalIndex) {

        List<InstNode> curLayer = layers.get(degree);
        Map<InstNode, Integer> curIdx = layerIndex.get(degree);

        // Find the selected node by its index in current layer
        InstNode selected = null;
        for (InstNode n : curLayer) {
            if (curIdx.get(n) == finalIndex) {
                selected = n;
                break;
            }
        }
        if (selected == null) return List.of();

        // Case 1: synthetic instruction → expand once //CHECK TWICE EXPANSION
        if (ProgramInfoImpl.isSynthetic(selected.instruction)) {
            List<SInstruction> children = expander.expandOne(selected.instruction);

            List<InstructionInfo> out = new ArrayList<>();
            int k = 1;
            for (SInstruction child : children) {
                out.add(ProgramInfoUtils.toInfo(child, k++, selected.originIndex, null));
            }
            return out;
        }

        // Case 2: basic instruction → return as is
        return List.of(
                ProgramInfoUtils.toInfo(selected.instruction, curIdx.get(selected),
                        selected.originIndex, null)
        );
    }

    // ===== inner class =====
    private static class InstNode {
        final SInstruction instruction;
        final InstNode parent;
        final int originIndex;
        final List<InstNode> children = new ArrayList<>();

        InstNode(SInstruction instruction, InstNode parent, int originIndex) {
            this.instruction = instruction;
            this.parent = parent;
            this.originIndex = originIndex;
            if (parent != null) {
                parent.children.add(this);
            }
        }
    }
}