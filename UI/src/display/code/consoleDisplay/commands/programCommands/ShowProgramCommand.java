package consoleDisplay.commands.programCommands;

import consoleDisplay.instructions.InstructionDTOPrinter;
import consoleDisplay.instructions.InstructionListPrinter;

import logic.engineFacade.api.EngineFacade;
import logic.domain.program.info.ProgramInfo;
import consoleDisplay.validation.Validators;
import logic.engineFacade.model.InstructionDTO;

import java.util.List;

public class ShowProgramCommand implements consoleDisplay.commands.UiCommand {
    private final engineHolder.EngineHolder engineHolder;
    private final consoleDisplay.console.ConsoleIO io;

    public ShowProgramCommand(engineHolder.EngineHolder engineHolder, consoleDisplay.console.ConsoleIO io) {
        this.engineHolder = engineHolder;
        this.io = io;
    }

    @Override
    public String title() { return "Show program"; }

    @Override
    public boolean isEnabled() { return engineHolder.hasEngine(); }

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
