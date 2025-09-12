package consoleDisplay.commands;

public interface UiCommand {
    String title();
    boolean isEnabled();
    void execute();
}
