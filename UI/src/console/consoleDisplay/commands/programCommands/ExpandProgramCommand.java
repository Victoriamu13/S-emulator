package console.consoleDisplay.commands.programCommands;


import console.consoleDisplay.commands.UiCommand;
import console.consoleDisplay.console.ConsoleIO;
import console.consoleDisplay.instructions.ExpansionChainsPrinter;
import console.consoleDisplay.instructions.InstructionDTOPrinter;
import console.consoleDisplay.instructions.InstructionListPrinter;
import logic.engineFacade.api.EngineFacade;
import console.consoleDisplay.validation.Validators;
import logic.engineFacade.model.InstructionDTO;

import java.util.List;

public class ExpandProgramCommand implements UiCommand {
    private final engineHolder.EngineHolder engineHolder;
    private final ConsoleIO io;

    public ExpandProgramCommand(engineHolder.EngineHolder engineHolder, ConsoleIO io) {
        this.engineHolder = engineHolder;
        this.io = io;
    }

    @Override
    public String title() { return "Expand program"; }

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
