package console.consoleDisplay.instructions;

import console.consoleDisplay.console.ConsoleIO;
import logic.engineFacade.model.InstructionDTO;

public class InstructionListPrinter extends AbstractInstructionDTOPrinter {

    public InstructionListPrinter(ConsoleIO io) {
        super(io);
    }

    @Override
    protected String formatInstruction(InstructionDTO inst) {
        return formatInstructionLine(inst);
    }
}