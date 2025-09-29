package logic.domain.program.info;

import logic.domain.execution.context.InputCollector;
import logic.domain.instructions.info.InstructionInfo;
import logic.domain.instructions.info.InstructionInfoImpl;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.domain.instructions.synthetic.sJumpInst.*;
import logic.domain.instructions.synthetic.sNoJumpInst.AssignmentInst;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;
import logic.domain.variable.SVarsType;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public final class ProgramInfoUtils {
    private ProgramInfoUtils() {}

    // Build InstructionInfo for a single instruction.
    public static InstructionInfo toInfo(SInstruction inst, int index,String fullCommandOrNull) {

        boolean synthetic = ProgramInfoImpl.isSynthetic(inst);
        String labelText = inst.getLabel().getLabelRepresentation();
        String command = (fullCommandOrNull != null) ? fullCommandOrNull : ProgramInfoImpl.toCommandText(inst);
        int cycles = inst.cycles();

        String varName = null;
        if (inst.getVariable() != null) {
            varName = inst.getVariable().getRepresentation();
        }

        return new InstructionInfoImpl(inst.getName(),index, synthetic,varName, labelText, command, cycles);
    }


    // ---- Instruction set aggregations ----

    public static List<String> inputsUsed(Iterable<SInstruction> instrs) {
        TreeSet<Integer> nums = new TreeSet<>();

        for (SInstruction inst : instrs) {
            SVars var = inst.getVariable();

            if (var != null && var.getType() == SVarsType.INPUT) {
                String rep = var.getRepresentation();
               nums.add(Integer.parseInt(rep.substring(1)));
            }

            //  case AssignmentInst
            if (inst instanceof AssignmentInst asg) {
                SVars src = asg.getSourceVar();
                if (src != null && src.getType() == SVarsType.INPUT) {
                    String repS = src.getRepresentation();
                    nums.add(Integer.parseInt(repS.substring(1)));
                }
            }

            //  case JumpEqualVariableInst
            if (inst instanceof JumpEqualVariableInst jev) {
                SVars other = jev.getOtherVar();
                if (other != null && other.getType() == SVarsType.INPUT) {
                    String repOt = other.getRepresentation();
                    nums.add(Integer.parseInt(repOt.substring(1)));
                }
            }

        if (inst instanceof logic.domain.instructions.synthetic.sNoJumpInst.QuoteInst q) {
            var argsInputs = InputCollector.collectInputs(q.getArguments());
            for (String in : argsInputs) {
                if (in.startsWith("x")) {
                    nums.add(Integer.parseInt(in.substring(1)));
                }
            }
        }

        // ⭐ חדש – JumpEqualFuncInst
        if (inst instanceof logic.domain.instructions.synthetic.sJumpInst.JumpEqualFuncInst jef) {
            var argsInputs = InputCollector.collectInputs(jef.getFunctionArgs());
            for (String in : argsInputs) {
                if (in.startsWith("x")) {
                    nums.add(Integer.parseInt(in.substring(1)));
                }
            }
        }
    }
        List<String> out = new ArrayList<>(nums.size());
        for (int n : nums) out.add("x" + n);
        return out;
    }


    public static List<String> labelsUsed(Iterable<SInstruction> instrs) {
        boolean hasExit = false;
        TreeSet<Integer> nums = new TreeSet<>();

        for (SInstruction inst : instrs) {
            SLabel lbl = inst.getLabel();

            if (lbl.isNumberLabel()) {
                String rep = lbl.getLabelRepresentation();
                int n = Integer.parseInt(rep.substring(1));
                nums.add(n);
            }

            if (isExitTarget(inst)) {
                hasExit = true;
            }
        }
        List<String> out = new ArrayList<>(nums.size() + (hasExit ? 1 : 0));
        for (int n : nums) out.add("L" + n);
        if (hasExit) out.add("EXIT");
        return out;
    }

    public static List<String> variablesUsed(Iterable<SInstruction> instrs) {
        TreeSet<String> vars = new TreeSet<>();

        for (SInstruction inst : instrs) {
            if (inst instanceof GoToLabelInst) {
                continue;
            }else {
                SVars var = inst.getVariable();
                if (var != null) {
                    if (var.getType() == SVarsType.RESULT) {
                        vars.add("y");
                    } else if (var.getType() == SVarsType.WORK) {
                        vars.add(var.getRepresentation()); // zN
                    }
                }
            }
        }
        return new ArrayList<>(vars);
    }

    private static boolean isExitTarget(SInstruction inst) {
        if (inst instanceof JumpNotZeroInst jnz) {
            return jnz.getTargetLabel() == SpecialLabels.EXIT;
        }
        if (inst instanceof JumpZeroInst jz) {
            return jz.getTargetLabel() == SpecialLabels.EXIT;
        }
        if (inst instanceof JumpEqualConstantInst jec) {
            return jec.getTargetLabel() == SpecialLabels.EXIT;
        }
        if (inst instanceof JumpEqualVariableInst jev) {
            return jev.getTargetLabel() == SpecialLabels.EXIT;
        }
        if (inst instanceof GoToLabelInst go) {
            return go.getTargetLabel() == SpecialLabels.EXIT;
        }
        if(inst instanceof JumpEqualFuncInst jef){
            return jef.getTargetLabel() == SpecialLabels.EXIT;
        }
        return false;
    }

}
