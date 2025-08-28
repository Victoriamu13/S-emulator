package commands;

import display.EngineHolder;
import display.console.ConsoleIO;
import display.instructions.ExpansionChainsPrinter;
import display.instructions.InstructionListPrinter;
import display.instructions.InstructionPrinter;
import logic.engineFacade.facade.EngineFacade;
import logic.program.info.ProgramInfo;
import validation.Validators;

public class ExpandProgramCommand implements UiCommand{
    private final EngineHolder engineHolder;
    private final ConsoleIO io;

    public ExpandProgramCommand(EngineHolder engineHolder, ConsoleIO io) {
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

        EngineFacade engine = engineHolder.get();
        int max = engine.getMaxExpansionDegree();
        io.println("\nMax expansion degree = " + max);
        int degree = io.askExpansionDegree(max);

        ProgramInfo expanded = engine.getProgramInfo(degree);

        io.println("\n=== Expanded Program (degree " + degree + ") ===");
        InstructionPrinter printer = (degree == 0)
                ? new InstructionListPrinter()    //regular format
                : new ExpansionChainsPrinter();
        printer.display(expanded);
        io.println("");
    }
}
