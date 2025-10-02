package logic.domain.program.info;

import logic.domain.execution.context.InputCollector;
import logic.domain.instructions.info.InstructionInfo;
import logic.domain.instructions.info.InstructionInfoImpl;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.domain.instructions.synthetic.sJumpInst.*;
import logic.domain.instructions.synthetic.sNoJumpInst.AssignmentInst;
import logic.domain.instructions.synthetic.sNoJumpInst.QuoteInst;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;
import logic.domain.variable.SVarsType;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public final class ProgramInfoUtils {
    private ProgramInfoUtils() {}

    // Build InstructionInfo for a single instruction
    public static InstructionInfo toInfo(SInstruction inst, int index,String fullCommandOrNull) {

        boolean synthetic = ProgramInfoImpl.isSynthetic(inst);
        String labelText = inst.getLabel().getLabelRepresentation();
        String command = (fullCommandOrNull != null) ? fullCommandOrNull : ProgramInfoImpl.toCommandText(inst);
        int cycles = inst.cycles();

        String varName = (inst.getVariable() != null)
                ? inst.getVariable().getRepresentation()
                : null;

        return new InstructionInfoImpl(inst.getName(),index, synthetic,varName, labelText, command, cycles);
    }


    // Collect all xN inputs used in program
    public static List<String> inputsUsed(Iterable<SInstruction> instrs) {
        TreeSet<Integer> nums = new TreeSet<>();

        for (SInstruction inst : instrs) {
            SVars var = inst.getVariable();

            // case: instruction has INPUT target
            if (var != null && var.getType() == SVarsType.INPUT) {
                String rep = var.getRepresentation();
               nums.add(Integer.parseInt(rep.substring(1)));

            }

            // case: Assignment from INPUT
            if (inst instanceof AssignmentInst asg) {
                SVars src = asg.getSourceVar();
                if (src != null && src.getType() == SVarsType.INPUT) {
                    String repS = src.getRepresentation();
                    nums.add(Integer.parseInt(repS.substring(1)));
                }
            }

            // case: JumpEqualVariable with INPUT
            if (inst instanceof JumpEqualVariableInst jev) {
                SVars other = jev.getOtherVar();
                if (other != null && other.getType() == SVarsType.INPUT) {
                    String repOt = other.getRepresentation();
                    nums.add(Integer.parseInt(repOt.substring(1)));
                }
            }

            // case: QUOTE args
        if (inst instanceof QuoteInst quote) {
            var argsInputs = InputCollector.collectInputs(quote.getArguments());
            for (String in : argsInputs) {
                if (in.startsWith("x")) nums.add(Integer.parseInt(in.substring(1)));
            }
        }

            // case: JumpEqualFunc args
        if (inst instanceof logic.domain.instructions.synthetic.sJumpInst.JumpEqualFuncInst jef) {
            var argsInputs = InputCollector.collectInputs(jef.getFunctionArgs());
            for (String in : argsInputs) {
                if (in.startsWith("x")) nums.add(Integer.parseInt(in.substring(1)));
            }
        }
    }
        List<String> out = new ArrayList<>(nums.size());
        for (int n : nums) out.add("x" + n);
        return out;
    }

    // Collect all labels used
    public static List<String> labelsUsed(Iterable<SInstruction> instrs) {
        boolean hasExit = false;
        TreeSet<Integer> nums = new TreeSet<>();

        for (SInstruction inst : instrs) {
            SLabel lbl = inst.getLabel();

            if (lbl.isNumberLabel()) {
                nums.add(Integer.parseInt(lbl.getLabelRepresentation().substring(1)));
            }
            if (isExitTarget(inst)) hasExit = true;
        }
        List<String> out = new ArrayList<>(nums.size() + (hasExit ? 1 : 0));
        for (int n : nums) out.add("L" + n);
        if (hasExit) out.add("EXIT");
        return out;
    }

    // Collect variables (y + zN)
    public static List<String> variablesUsed(Iterable<SInstruction> instrs) {
        TreeSet<String> vars = new TreeSet<>();

        for (SInstruction inst : instrs) {
            if (inst instanceof GoToLabelInst) continue;  // goto has no variable
            if (inst.getVariable() != null) {
                if (inst.getVariable().getType() == SVarsType.RESULT) vars.add("y");
                if (inst.getVariable().getType() == SVarsType.WORK) vars.add(inst.getVariable().getRepresentation());
            }
        }
        return new ArrayList<>(vars);
    }

    // Helper: check if instruction jumps to EXIT
    private static boolean isExitTarget(SInstruction inst) {
        return switch (inst) {
            case JumpNotZeroInst jnz -> jnz.getTargetLabel() == SpecialLabels.EXIT;
            case JumpZeroInst jz -> jz.getTargetLabel() == SpecialLabels.EXIT;
            case JumpEqualConstantInst jec -> jec.getTargetLabel() == SpecialLabels.EXIT;
            case JumpEqualVariableInst jev -> jev.getTargetLabel() == SpecialLabels.EXIT;
            case GoToLabelInst go -> go.getTargetLabel() == SpecialLabels.EXIT;
            case JumpEqualFuncInst jef -> jef.getTargetLabel() == SpecialLabels.EXIT;
            default -> false;
        };
    }

}
