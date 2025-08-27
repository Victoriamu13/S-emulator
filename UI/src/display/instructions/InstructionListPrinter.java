package display.instructions;

import logic.instructions.info.InstructionInfo;

public class InstructionListPrinter extends AbstractInstructionPrinter {

    @Override
    protected String formatInstruction(InstructionInfo inst) {
        String number   = String.format("#%d", inst.getIndex());
        String type     = inst.isSynthetic() ? "S" : "B";
        String labelBox = String.format("[ %-5s ]", inst.getLabelName());
        String command  = inst.getFullCommand();

        if (command.contains(">>>")) {
            return String.format("%s(%s)%s%s", number, type, labelBox, command);
        }
        int cycles = inst.getCycles();
        return String.format("%s(%s)%s%s(%d)", number, type, labelBox, command, cycles);
    }

}
