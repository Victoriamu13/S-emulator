package commands.programCommands;

import commands.UiCommand;
import display.EngineHolder;
import display.console.ConsoleIO;
import logic.engineFacade.facade.EngineFacade;
import logic.engineFacade.facade.EngineFacadeImpl;
import logic.io.xml.load.LoadResult;

import java.nio.file.Path;

public class LoadProgramCommand implements UiCommand {
    private final EngineHolder engineHolder;
    private final ConsoleIO io;

    public LoadProgramCommand(EngineHolder engineHolder, ConsoleIO io) {
        this.engineHolder = engineHolder;
        this.io = io;
    }

    @Override
    public String title() { return "Load program from XML"; }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public void execute() {
        String pathStr = io.askLine("\nEnter path to XML file: ");
        if (pathStr == null || pathStr.isBlank()) {
            io.println("No path provided.");
            return;
        }

        EngineFacade engine = new EngineFacadeImpl();
        LoadResult res = engine.loadProgram(Path.of(pathStr.trim()));
        if (!res.success) {
            io.println("Failed to load program from XML file.");
            for (String err : res.errors) {
                io.println(" - " + err);
            }
            return;
        }

        engineHolder.set(engine);
        io.println("Program loaded successfully: " + res.program.getName());
    }
}


