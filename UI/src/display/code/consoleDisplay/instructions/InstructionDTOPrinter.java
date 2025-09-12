package consoleDisplay.instructions;

import logic.engineFacade.model.InstructionDTO;

import java.util.List;

public interface InstructionDTOPrinter {
    void display(List<InstructionDTO> instructions, String programName, List<String> inputs, List<String> labels);
}
