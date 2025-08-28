package commands;

import display.EngineHolder;
import display.console.ConsoleIO;
import display.instructions.InstructionListPrinter;
import display.instructions.InstructionPrinter;
import logic.engineFacade.facade.EngineFacade;
import logic.program.info.ProgramInfo;
import validation.Validators;

public class ShowProgramCommand implements UiCommand{
    private final EngineHolder engineHolder;
    private final ConsoleIO io;

    public ShowProgramCommand(EngineHolder engineHolder, ConsoleIO io) {
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

        EngineFacade engine = engineHolder.get();
        ProgramInfo info = engine.getProgramInfo(0);

        io.println("\n=== Program Info ===");
        InstructionPrinter printer = new InstructionListPrinter();
        printer.display(info);
        io.println("");
    }
}
