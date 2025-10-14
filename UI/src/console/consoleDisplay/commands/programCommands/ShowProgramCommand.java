package console.consoleDisplay.commands.programCommands;

import console.consoleDisplay.commands.UiCommand;
import console.consoleDisplay.console.ConsoleIO;
import console.consoleDisplay.instructions.InstructionDTOPrinter;
import console.consoleDisplay.instructions.InstructionListPrinter;

import logic.engineFacade.api.EngineFacade;
import console.consoleDisplay.validation.Validators;
import logic.engineFacade.model.InstructionDTO;

import java.util.List;

public class ShowProgramCommand implements UiCommand {
    private final engineHolder.EngineHolder engineHolder;
    private final ConsoleIO io;

    public ShowProgramCommand(engineHolder.EngineHolder engineHolder, ConsoleIO io) {
        this.engineHolder = engineHolder;
        this.io = io;
    }

    @Override
    public String title() { return "Show program"; }


    @Override
    public void execute() {
        if (!Validators.requireEngineLoaded(engineHolder, io)) return;

        EngineFacade engine = engineHolder.getEngine();
        List<InstructionDTO> rows = engine.getInstructionRows(0);
        String programName = engine.getProgramName();
        List<String> inputs = engine.getInputsUsed(0);
        List<String> labels = engine.getLabelsUsed(0);

        io.println("\n=== Program Info ===");
        InstructionDTOPrinter printer = new InstructionListPrinter(io);
        printer.display(rows, programName, inputs, labels);
    }
}
