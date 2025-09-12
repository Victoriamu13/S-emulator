package consoleDisplay.commands.system;

import consoleDisplay.commands.UiCommand;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.SystemState;
import consoleDisplay.validation.Validators;

import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SaveSystemStateCommand implements UiCommand {
    private final engineHolder.EngineHolder holder;
    private final consoleDisplay.console.ConsoleIO io;

    public SaveSystemStateCommand(engineHolder.EngineHolder holder, consoleDisplay.console.ConsoleIO io) {
        this.holder = holder;
        this.io = io;
    }

    @Override public String title() { return "Save system state"; }
    @Override public boolean isEnabled() { return true; }

    @Override
    public void execute() {
        if (!Validators.requireEngineLoaded(holder, io)) {
            return;
        }
        String base = io.askLine("Enter full path (without extension) to save state: ");
        if (base == null || base.isBlank()) {
            io.println("No path provided.");
            return;
        }

        try {
            Path savePath = Path.of(base.trim() + ".semu");
            Path parent = savePath.getParent();

            if (parent != null && !Files.exists(parent)) {
                io.println("Error: Directory does not exist -> " + parent.toAbsolutePath());
                return;
            }

            EngineFacade eng = holder.getEngine();
            if (eng == null) {
                io.println("Error: No program loaded, cannot save state.");
                return;
            }

            try (ObjectOutputStream oos =
                         new ObjectOutputStream(Files.newOutputStream(savePath))) {

                String xml = eng.getLoadedXmlPath();
                oos.writeObject(new SystemState(xml, holder.history()));
                io.println("System state saved to: " + savePath.toAbsolutePath());
            }

        } catch (Exception e) {
            io.println("Failed to save state: " + e.getMessage());
        }
    }
}
