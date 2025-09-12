package consoleDisplay.commands.programCommands;


import consoleDisplay.instructions.ExpansionChainsPrinter;
import consoleDisplay.instructions.InstructionDTOPrinter;
import consoleDisplay.instructions.InstructionListPrinter;
import logic.engineFacade.api.EngineFacade;
import logic.domain.program.info.ProgramInfo;
import consoleDisplay.validation.Validators;
import logic.engineFacade.model.InstructionDTO;

import java.util.List;

public class ExpandProgramCommand implements consoleDisplay.commands.UiCommand {
    private final engineHolder.EngineHolder engineHolder;
    private final consoleDisplay.console.ConsoleIO io;

    public ExpandProgramCommand(engineHolder.EngineHolder engineHolder, consoleDisplay.console.ConsoleIO io) {
        this.engineHolder = engineHolder;
        this.io = io;
    }

    @Override
    public String title() { return "Expand program"; }

    @Override
    public boolean isEnabled() { return engineHolder.hasEngine(); }

    @Override
    public void execute() {
        if (!Validators.requireEngineLoaded(engineHolder, io)) return;

        EngineFacade engine = engineHolder.getEngine();
        int max = engine.getMaxExpansionDegree();
        io.println("\nMax expansion degree = " + max);
        int degree = io.askExpansionDegree(max);

        io.println("\n=== Expanded Program (degree " + degree + ") ===");
        List<InstructionDTO> rows = engine.getInstructionRows(degree);
        String programName = engine.getProgramName();
        List<String> inputs = engine.getInputsUsed(degree);
        List<String> labels = engine.getLabelsUsed(degree);

        InstructionDTOPrinter printer = (degree == 0)
                ? new InstructionListPrinter(io)    //regular format
                : new ExpansionChainsPrinter(io,engine,degree);
        printer.display(rows, programName, inputs, labels);
    }
}
