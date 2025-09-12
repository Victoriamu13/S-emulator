package consoleDisplay.instructions;

import consoleDisplay.console.ConsoleIO;
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