package display.console;

import commands.programCommands.ExitCommand;
import commands.UiCommand;

import java.util.List;

public class Menu {
    private final List<UiCommand> commands;
    private final ConsoleIO io;

    public Menu(List<UiCommand> commands, ConsoleIO io) {
        this.commands = commands;
        this.io = io;
    }

    private void printMenu() {
        io.println("\n=== Main Menu ===");
        for (int i = 0; i < commands.size(); i++) {
            UiCommand cmd = commands.get(i);
            io.println((i + 1) + ". " + cmd.title());
        }
        io.println("=================");
    }

    public void runLoop() {
        while (true) {
            printMenu();

            String input = io.askLine("\nChoose option (1-" + commands.size() + "): ");
            try {
                int choice = Integer.parseInt(input.trim());
                if (choice < 1 || choice > commands.size()) {
                    io.println("Invalid choice. Please enter a number between 1 and " + commands.size() + ".");
                    continue;
                }

                UiCommand cmd = commands.get(choice - 1);
                cmd.execute();

                // Exit command - end loop
                if (cmd instanceof ExitCommand exit && exit.shouldExit()) {
                    break;
                }

            } catch (NumberFormatException e) {
                io.println("Invalid input. Please enter a number.");
            }
        }
    }
}
