package logic.expand.expandProgram;

import logic.expand.instructionsExpanded.InstructionExpander;
import logic.expand.instructionsExpanded.jumpExpandedInst.GotoLabelExpander;
import logic.expand.instructionsExpanded.jumpExpandedInst.JumpEqlConstExpander;
import logic.expand.instructionsExpanded.jumpExpandedInst.JumpEqlVarExpander;
import logic.expand.instructionsExpanded.jumpExpandedInst.JumpZeroExpander;
import logic.expand.instructionsExpanded.noJumpExpandedInstr.AssignmentExpander;
import logic.expand.instructionsExpanded.noJumpExpandedInstr.ConstAssignmentExpander;
import logic.expand.instructionsExpanded.noJumpExpandedInstr.ZeroVariableExpander;
import logic.instructions.SInstruction;

public class ExpanderFactory {

    public static InstructionExpander forInstruction(SInstruction ins) {
        String n = ins.getName();
        return switch (n) {
            case "ZERO_VARIABLE" -> new ZeroVariableExpander();
            case "ASSIGNMENT" -> new AssignmentExpander();
            case "CONSTANT_ASSIGNMENT" -> new ConstAssignmentExpander();
            case "JUMP_ZERO" -> new JumpZeroExpander();
            case "JUMP_EQUAL_CONSTANT" -> new JumpEqlConstExpander();
            case "JUMP_EQUAL_VARIABLE" -> new JumpEqlVarExpander();
            case "GOTO_LABEL" -> new GotoLabelExpander();
            default -> null; // case basic instruction
        };
    }
}
