package display.instructions;

import display.console.ConsoleIO;
import logic.instructions.info.InstructionInfo;

public class ExpansionChainsPrinter extends AbstractInstructionPrinter {

    public ExpansionChainsPrinter(ConsoleIO io) {
        super(io);
    }

    @Override
    protected String formatInstruction(InstructionInfo inst) {
        return inst.getFullCommand();
    }
}
