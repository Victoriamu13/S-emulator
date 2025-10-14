package console.consoleDisplay.commands.programCommands;


import console.consoleDisplay.commands.UiCommand;
import console.consoleDisplay.console.ConsoleIO;

public class ExitCommand implements UiCommand {
    private final ConsoleIO io;
    private boolean shouldExit = false;

    public ExitCommand(ConsoleIO io) {
        this.io = io;
    }

    @Override
    public String title() { return "Exit"; }


    @Override
    public void execute() {
        io.println("Exiting program...");
        shouldExit = true;
    }

    public boolean shouldExit() {
        return shouldExit;
    }
}
