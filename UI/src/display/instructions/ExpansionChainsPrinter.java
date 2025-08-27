package display.instructions;

import logic.instructions.info.InstructionInfo;

public class ExpansionChainsPrinter extends AbstractInstructionPrinter {

    @Override
    protected String formatInstruction(InstructionInfo inst) {
        return inst.getFullCommand();
    }
}
