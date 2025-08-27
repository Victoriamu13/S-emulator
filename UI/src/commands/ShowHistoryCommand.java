package commands;

import display.EngineHolder;
import display.console.ConsoleIO;
import logic.engineFacade.runHistory.RunHistory;
import logic.engineFacade.runHistory.RunRecord;

public class ShowHistoryCommand implements UiCommand{
    private final EngineHolder engineHolder;
    private final ConsoleIO io;

    public ShowHistoryCommand(EngineHolder engineHolder, ConsoleIO io) {
        this.engineHolder = engineHolder;
        this.io = io;
    }

    @Override
    public String title() { return "Show run history"; }

    @Override
    public boolean isEnabled() { return engineHolder.hasEngine(); }

    @Override
    public void execute() {
        if (!isEnabled()) {
            io.println("Error: no valid program is loaded. Please load a program file first.");
            return;
        }
        RunHistory history = engineHolder.history();
        if (history.isEmpty()) {
            io.println("\nNo runs yet.\n");
            return;
        }

        io.println("\n=== Run History ===\n");
        io.printf("%-6s %-8s %-28s %-10s %-10s%n", "#Run", "Degree", "Inputs", "Y value", "Cycles");
        io.println("---------------------------------------------------------------");

        for (RunRecord r : history.records()) {
            io.printf("%-6d %-8d %-28s %-10d %-10d%n", r.runNo(), r.degree(), toCsv(r.inputs()), r.yValue(), r.cycles());
        }
        io.println("\n");
    }


    //create String from numbers array - For example: [1,2,3] -> 1,2,3
    private static String toCsv(long[] arr) {

        if (arr == null || arr.length == 0) return "";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(arr[i]);
        }
        return sb.toString();
    }
}
