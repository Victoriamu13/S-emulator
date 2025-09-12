package consoleDisplay.commands.programCommands;

import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.api.EngineFacadeImpl;
import logic.engineFacade.model.LoadOutcome;

import java.nio.file.Path;

public class LoadProgramCommand implements consoleDisplay.commands.UiCommand {
    private final engineHolder.EngineHolder engineHolder;
    private final consoleDisplay.console.ConsoleIO io;

    public LoadProgramCommand(engineHolder.EngineHolder engineHolder, consoleDisplay.console.ConsoleIO io) {
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
        LoadOutcome res = engine.loadProgram(Path.of(pathStr.trim()));
        if (!res.success()) {
            io.println("Failed to load program from XML file.");
            for (String err : res.errors()) {
                io.println(" - " + err);
            }
            return;
        }

        engineHolder.set(engine);
        io.println("Program loaded successfully: " + engine.getProgramName());
    }
}


