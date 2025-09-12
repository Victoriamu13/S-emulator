package consoleDisplay.commands.programCommands;


public class ExitCommand implements consoleDisplay.commands.UiCommand {
    private final consoleDisplay.console.ConsoleIO io;
    private boolean shouldExit = false;

    public ExitCommand(consoleDisplay.console.ConsoleIO io) {
        this.io = io;
    }

    @Override
    public String title() { return "Exit"; }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public void execute() {
        io.println("Exiting program...");
        shouldExit = true;
    }

    public boolean shouldExit() {
        return shouldExit;
    }
}
